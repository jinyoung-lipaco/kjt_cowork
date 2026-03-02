import type { FastifyInstance } from "fastify";
import { PollStatus } from "@prisma/client";
import { z } from "zod";
import { prisma } from "../../lib/prisma.js";

const listPollsQuerySchema = z.object({
  status: z.nativeEnum(PollStatus).optional()
});

const createPollSchema = z.object({
  title: z.string().min(1).max(120),
  description: z.string().max(2000).optional(),
  closesAt: z.string().datetime().optional(),
  options: z.array(z.string().min(1).max(80)).min(2).max(10)
});

const voteSchema = z.object({
  userId: z.string().min(1),
  optionId: z.string().min(1)
});

const createApprovedItemSchema = z.object({
  category: z.string().min(1).max(60),
  name: z.string().min(1).max(140),
  brand: z.string().max(80).optional(),
  safetyScore: z.number().int().min(0).max(100),
  priceText: z.string().max(60).optional(),
  criteriaText: z.string().max(1000).optional()
});

export async function standardRoutes(app: FastifyInstance) {
  app.get("/standards/polls", async (request) => {
    const query = listPollsQuerySchema.parse(request.query);
    const polls = await prisma.votePoll.findMany({
      where: query.status ? { status: query.status } : undefined,
      orderBy: { createdAt: "desc" },
      include: {
        options: { orderBy: { sortOrder: "asc" } },
        _count: { select: { answers: true } }
      }
    });

    return polls.map((poll) => ({
      id: poll.id,
      title: poll.title,
      description: poll.description,
      status: poll.status,
      closesAt: poll.closesAt,
      createdAt: poll.createdAt,
      options: poll.options,
      participantCount: poll._count.answers
    }));
  });

  app.get("/standards/polls/:pollId", async (request, reply) => {
    const params = z.object({ pollId: z.string().min(1) }).parse(request.params);
    const poll = await prisma.votePoll.findUnique({
      where: { id: params.pollId },
      include: {
        options: {
          orderBy: { sortOrder: "asc" },
          include: { _count: { select: { answers: true } } }
        },
        _count: { select: { answers: true } }
      }
    });

    if (!poll) {
      return reply.code(404).send({ message: "투표를 찾을 수 없습니다." });
    }

    return {
      id: poll.id,
      title: poll.title,
      description: poll.description,
      status: poll.status,
      closesAt: poll.closesAt,
      totalVotes: poll._count.answers,
      options: poll.options.map((option) => ({
        id: option.id,
        label: option.label,
        sortOrder: option.sortOrder,
        voteCount: option._count.answers
      }))
    };
  });

  app.post("/standards/polls", async (request) => {
    const body = createPollSchema.parse(request.body);
    const poll = await prisma.votePoll.create({
      data: {
        title: body.title,
        description: body.description,
        closesAt: body.closesAt ? new Date(body.closesAt) : undefined,
        options: {
          create: body.options.map((label, idx) => ({ label, sortOrder: idx }))
        }
      },
      include: { options: true }
    });
    return poll;
  });

  app.post("/standards/polls/:pollId/vote", async (request, reply) => {
    const params = z.object({ pollId: z.string().min(1) }).parse(request.params);
    const body = voteSchema.parse(request.body);

    const option = await prisma.voteOption.findUnique({ where: { id: body.optionId } });
    if (!option || option.pollId !== params.pollId) {
      return reply.code(400).send({ message: "선택한 옵션이 유효하지 않습니다." });
    }

    const existing = await prisma.voteAnswer.findUnique({
      where: { pollId_userId: { pollId: params.pollId, userId: body.userId } }
    });

    if (existing) {
      await prisma.voteAnswer.update({
        where: { id: existing.id },
        data: { optionId: body.optionId }
      });
      return { updated: true };
    }

    await prisma.voteAnswer.create({
      data: {
        pollId: params.pollId,
        optionId: body.optionId,
        userId: body.userId
      }
    });
    return { updated: false };
  });

  app.get("/standards/approved-items", async () => {
    return prisma.approvedItem.findMany({ orderBy: { createdAt: "desc" } });
  });

  app.get("/standards/approved-items/:itemId", async (request, reply) => {
    const params = z.object({ itemId: z.string().min(1) }).parse(request.params);
    const item = await prisma.approvedItem.findUnique({ where: { id: params.itemId } });
    if (!item) {
      return reply.code(404).send({ message: "인정템을 찾을 수 없습니다." });
    }
    return item;
  });

  app.post("/standards/approved-items", async (request) => {
    const body = createApprovedItemSchema.parse(request.body);
    return prisma.approvedItem.create({ data: body });
  });
}
