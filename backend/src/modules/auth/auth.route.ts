import bcrypt from "bcryptjs";
import type { FastifyInstance } from "fastify";
import { z } from "zod";
import { prisma } from "../../lib/prisma.js";

const signUpSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  nickname: z.string().min(2).max(24)
});

const signInSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
});

export async function authRoutes(app: FastifyInstance) {
  app.post("/auth/sign-up", async (request, reply) => {
    const body = signUpSchema.parse(request.body);
    const exists = await prisma.user.findUnique({ where: { email: body.email } });
    if (exists) {
      return reply.code(409).send({ message: "이미 가입된 이메일입니다." });
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

    const accessToken = await reply.jwtSign(
      { sub: user.id, type: "access" },
      { expiresIn: "15m" }
    );
    const refreshToken = await reply.jwtSign(
      { sub: user.id, type: "refresh" },
      { expiresIn: "30d" }
    );

    return {
      user: { id: user.id, email: user.email, nickname: user.nickname, tier: user.tier },
      accessToken,
      refreshToken
    };
  });
}
