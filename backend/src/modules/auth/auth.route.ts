import bcrypt from "bcryptjs";
import { OAuth2Client } from "google-auth-library";
import jwt from "jsonwebtoken";
import type { FastifyInstance, FastifyReply } from "fastify";
import { AuthProvider } from "@prisma/client";
import { z } from "zod";
import { prisma } from "../../lib/prisma.js";
import { env } from "../../config/env.js";
import {
  buildAvailableNickname,
  isNicknameTaken,
  validateNicknamePolicy
} from "../users/nickname.policy.js";

const signUpSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  nickname: z.string().min(2).max(20)
});

const signInSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
});

const refreshSchema = z.object({
  refreshToken: z.string().min(1)
});

const googleSocialSchema = z.object({
  idToken: z.string().min(1),
  nickname: z.string().min(2).max(20).optional()
});

const kakaoSocialSchema = z.object({
  accessToken: z.string().min(1),
  nickname: z.string().min(2).max(20).optional()
});

const googleClient = env.GOOGLE_CLIENT_ID ? new OAuth2Client(env.GOOGLE_CLIENT_ID) : null;

function buildTokens(reply: FastifyReply, userId: string) {
  const refreshToken = jwt.sign({ sub: userId, type: "refresh" }, env.JWT_REFRESH_SECRET, {
    expiresIn: "30d"
  });
  return Promise.all([
    reply.jwtSign({ sub: userId, type: "access" }, { expiresIn: "15m" }),
    Promise.resolve(refreshToken)
  ]);
}

async function findOrCreateSocialUser(params: {
  provider: AuthProvider;
  providerUserId: string;
  email?: string | null;
  nickname?: string | null;
}) {
  const existingIdentity = await prisma.authIdentity.findUnique({
    where: {
      provider_providerUserId: {
        provider: params.provider,
        providerUserId: params.providerUserId
      }
    },
    include: { user: true }
  });

  if (existingIdentity) {
    return existingIdentity.user;
  }

  const normalizedEmail =
    params.email && params.email.trim().length > 0
      ? params.email.toLowerCase()
      : `${params.provider.toLowerCase()}_${params.providerUserId}@social.sohangseong.local`;

  const nickname = await buildAvailableNickname(params.nickname, params.providerUserId);

  const existingByEmail = await prisma.user.findUnique({ where: { email: normalizedEmail } });

  if (existingByEmail) {
    await prisma.authIdentity.create({
      data: {
        userId: existingByEmail.id,
        provider: params.provider,
        providerUserId: params.providerUserId
      }
    });
    return existingByEmail;
  }

  return prisma.user.create({
    data: {
      email: normalizedEmail,
      nickname,
      identities: {
        create: {
          provider: params.provider,
          providerUserId: params.providerUserId
        }
      }
    }
  });
}

export async function authRoutes(app: FastifyInstance) {
  app.post("/auth/sign-up", async (request, reply) => {
    const body = signUpSchema.parse(request.body);
    const exists = await prisma.user.findUnique({ where: { email: body.email } });
    if (exists) {
      return reply.code(409).send({ message: "이미 가입된 이메일입니다." });
    }

    const nicknameError = validateNicknamePolicy(body.nickname);
    if (nicknameError) {
      return reply.code(400).send({ message: nicknameError });
    }

    const nicknameTaken = await isNicknameTaken(body.nickname);
    if (nicknameTaken) {
      return reply.code(409).send({ message: "이미 사용 중인 닉네임입니다." });
    }

    const passwordHash = await bcrypt.hash(body.password, 10);
    const user = await prisma.user.create({
      data: {
        email: body.email,
        nickname: body.nickname,
        passwordHash
      }
    });

    return { id: user.id, email: user.email, nickname: user.nickname };
  });

  app.post("/auth/sign-in", async (request, reply) => {
    const body = signInSchema.parse(request.body);
    const user = await prisma.user.findUnique({ where: { email: body.email } });
    if (!user || !user.passwordHash) {
      return reply.code(401).send({ message: "이메일 또는 비밀번호가 올바르지 않습니다." });
    }

    const matched = await bcrypt.compare(body.password, user.passwordHash);
    if (!matched) {
      return reply.code(401).send({ message: "이메일 또는 비밀번호가 올바르지 않습니다." });
    }

    const [accessToken, refreshToken] = await buildTokens(reply, user.id);

    return {
      user: { id: user.id, email: user.email, nickname: user.nickname, tier: user.tier },
      accessToken,
      refreshToken
    };
  });

  app.post("/auth/refresh", async (request, reply) => {
    const body = refreshSchema.parse(request.body);

    let payload: { sub?: string; type?: string };
    try {
      payload = jwt.verify(body.refreshToken, env.JWT_REFRESH_SECRET) as {
        sub?: string;
        type?: string;
      };
    } catch {
      return reply.code(401).send({ message: "유효하지 않은 리프레시 토큰입니다." });
    }

    if (payload.type !== "refresh" || !payload.sub) {
      return reply.code(401).send({ message: "유효하지 않은 리프레시 토큰입니다." });
    }

    const user = await prisma.user.findUnique({ where: { id: payload.sub } });
    if (!user) {
      return reply.code(401).send({ message: "사용자를 찾을 수 없습니다." });
    }

    const [accessToken, refreshToken] = await buildTokens(reply, user.id);
    return {
      user: { id: user.id, email: user.email, nickname: user.nickname, tier: user.tier },
      accessToken,
      refreshToken
    };
  });

  app.post("/auth/social/google", async (request, reply) => {
    const body = googleSocialSchema.parse(request.body);
    if (!googleClient || !env.GOOGLE_CLIENT_ID) {
      return reply.code(500).send({ message: "GOOGLE_CLIENT_ID가 설정되지 않았습니다." });
    }

    const ticket = await googleClient.verifyIdToken({
      idToken: body.idToken,
      audience: env.GOOGLE_CLIENT_ID
    });

    const payload = ticket.getPayload();
    if (!payload || !payload.sub) {
      return reply.code(401).send({ message: "유효하지 않은 구글 토큰입니다." });
    }

    const user = await findOrCreateSocialUser({
      provider: AuthProvider.GOOGLE,
      providerUserId: payload.sub,
      email: payload.email,
      nickname: body.nickname ?? payload.name
    });
    const [accessToken, refreshToken] = await buildTokens(reply, user.id);

    return {
      user: { id: user.id, email: user.email, nickname: user.nickname, tier: user.tier },
      accessToken,
      refreshToken
    };
  });

  app.post("/auth/social/kakao", async (request, reply) => {
    const body = kakaoSocialSchema.parse(request.body);
    const kakaoResponse = await fetch("https://kapi.kakao.com/v2/user/me", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${body.accessToken}`,
        "Content-Type": "application/x-www-form-urlencoded;charset=utf-8"
      }
    });

    if (!kakaoResponse.ok) {
      return reply.code(401).send({ message: "유효하지 않은 카카오 토큰입니다." });
    }

    const profile = (await kakaoResponse.json()) as {
      id: number;
      kakao_account?: {
        email?: string;
        profile?: { nickname?: string };
      };
    };

    if (!profile.id) {
      return reply.code(401).send({ message: "카카오 사용자 정보를 확인할 수 없습니다." });
    }

    const user = await findOrCreateSocialUser({
      provider: AuthProvider.KAKAO,
      providerUserId: String(profile.id),
      email: profile.kakao_account?.email,
      nickname: body.nickname ?? profile.kakao_account?.profile?.nickname
    });
    const [accessToken, refreshToken] = await buildTokens(reply, user.id);

    return {
      user: { id: user.id, email: user.email, nickname: user.nickname, tier: user.tier },
      accessToken,
      refreshToken
    };
  });
}
