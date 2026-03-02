import cors from "@fastify/cors";
import jwt from "@fastify/jwt";
import Fastify from "fastify";
import { env } from "./config/env.js";
import { authRoutes } from "./modules/auth/auth.route.js";
import { healthRoutes } from "./modules/health/health.route.js";
import { postRoutes } from "./modules/posts/posts.route.js";

export function buildApp() {
  const app = Fastify({ logger: true });

  app.register(cors, { origin: true });
  app.register(jwt, { secret: env.JWT_ACCESS_SECRET });

  app.register(healthRoutes, { prefix: "/api" });
  app.register(authRoutes, { prefix: "/api" });
  app.register(postRoutes, { prefix: "/api" });

  return app;
}
