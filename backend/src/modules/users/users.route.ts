import type { FastifyInstance } from "fastify";
import { z } from "zod";
import { prisma } from "../../lib/prisma.js";

export async function userRoutes(app: FastifyInstance) {
  app.patch("/users/:userId/profile", async (request, reply) => {
    const params = z.object({ userId: z.string().min(1) }).parse(request.params);
    const body = z.object({ nickname: z.string().trim().min(2).max(20) }).parse(request.body);

    const user = await prisma.user.findUnique({
      where: { id: params.userId },
      select: { id: true }
    });

    if (!user) {
      return reply.code(404).send({ message: "사용자를 찾을 수 없습니다." });
    }

    const updated = await prisma.user.update({
      where: { id: params.userId },
      data: { nickname: body.nickname },
      select: { id: true, nickname: true, tier: true, updatedAt: true }
    });

    return updated;
  });

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

    const [recentPosts, recentComments, recentVotes] = await Promise.all([
      prisma.post.findMany({
        where: { authorId: params.userId },
        orderBy: { createdAt: "desc" },
        take: 5,
        select: {
          id: true,
          title: true,
          createdAt: true
        }
      }),
      prisma.comment.findMany({
        where: { authorId: params.userId },
        orderBy: { createdAt: "desc" },
        take: 5,
        select: {
          id: true,
          body: true,
          postId: true,
          createdAt: true
        }
      }),
      prisma.voteAnswer.findMany({
        where: { userId: params.userId },
        orderBy: { createdAt: "desc" },
        take: 5,
        select: {
          id: true,
          pollId: true,
          createdAt: true,
          poll: { select: { title: true } },
          option: { select: { label: true } }
        }
      })
    ]);

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
      },
      activity: {
        recentPosts,
        recentComments,
        recentVotes: recentVotes.map((vote) => ({
          id: vote.id,
          pollId: vote.pollId,
          pollTitle: vote.poll.title,
          optionLabel: vote.option.label,
          createdAt: vote.createdAt
        }))
      }
    };
  });
}
