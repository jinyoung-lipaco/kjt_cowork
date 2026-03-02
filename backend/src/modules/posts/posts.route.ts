import type { FastifyInstance } from "fastify";
import { z } from "zod";
import { prisma } from "../../lib/prisma.js";

const createPostSchema = z.object({
  title: z.string().min(1).max(120),
  body: z.string().min(1).max(5000),
  authorId: z.string().min(1)
});

const createCommentSchema = z.object({
  postId: z.string().min(1),
  authorId: z.string().min(1),
  body: z.string().min(1).max(2000)
});

export async function postRoutes(app: FastifyInstance) {
  app.get("/posts", async () => {
    return prisma.post.findMany({
      orderBy: { createdAt: "desc" },
      include: { author: true, comments: true }
    });
  });

  app.get("/posts/:postId", async (request, reply) => {
    const params = z.object({ postId: z.string().min(1) }).parse(request.params);
    const post = await prisma.post.findUnique({
      where: { id: params.postId },
      include: {
        author: { select: { id: true, nickname: true, tier: true } },
        comments: {
          orderBy: { createdAt: "asc" },
          include: { author: { select: { id: true, nickname: true, tier: true } } }
        }
      }
    });

    if (!post) {
      return reply.code(404).send({ message: "게시글을 찾을 수 없습니다." });
    }

    return post;
  });

  app.post("/posts", async (request) => {
    const body = createPostSchema.parse(request.body);
    return prisma.post.create({ data: body });
  });

  app.post("/posts/comments", async (request) => {
    const body = createCommentSchema.parse(request.body);
    return prisma.comment.create({ data: body });
  });
}
