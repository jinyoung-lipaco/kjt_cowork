import { env } from "./config/env.js";
import { buildApp } from "./app.js";

async function bootstrap() {
  const app = buildApp();
  await app.listen({ port: env.PORT, host: "0.0.0.0" });
}

bootstrap().catch((error) => {
  console.error(error);
  process.exit(1);
});
