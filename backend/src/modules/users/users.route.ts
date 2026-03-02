import type { FastifyInstance } from "fastify";
import { z } from "zod";
import { prisma } from "../../lib/prisma.js";

export async function userRoutes(app: FastifyInstance) {
  app.get("/users/:userId/profile-summary", async (request, reply) => {
    const params = z.object({ userId: z.string().min(1) }).parse(request.params);

    const user = await prisma.user.findUnique({
      where: { id: params.userId },
      select: {
        id: true,
        email: true,
        nickname: true,
        tier: true,
        createdAt: true,
        _count: {
          select: {
            posts: true,
            comments: true,
            voteAnswers: true
          }
        }
      }
    });

    if (!user) {
      return reply.code(404).send({ message: "사용자를 찾을 수 없습니다." });
    }

    return {
      id: user.id,
      email: user.email,
      nickname: user.nickname,
      tier: user.tier,
      createdAt: user.createdAt,
      stats: {
        postCount: user._count.posts,
        commentCount: user._count.comments,
        voteCount: user._count.voteAnswers
      }
    };
  });
}
