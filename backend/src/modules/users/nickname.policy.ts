import { prisma } from "../../lib/prisma.js";

const DEFAULT_NICKNAME = "소행성맘";
const MAX_NICKNAME_LENGTH = 20;
const BLOCKED_WORDS = [
  "admin",
  "administrator",
  "master",
  "official",
  "staff",
  "gm",
  "관리자",
  "운영자",
  "스태프"
];

function normalizeForPolicy(value: string) {
  return value.toLowerCase().replace(/[\s_-]/g, "");
}

export function validateNicknamePolicy(nickname: string): string | null {
  const trimmed = nickname.trim();
  if (trimmed.length < 2 || trimmed.length > MAX_NICKNAME_LENGTH) {
    return `닉네임은 2~${MAX_NICKNAME_LENGTH}자여야 합니다.`;
  }

  const normalized = normalizeForPolicy(trimmed);
  const hasBlockedWord = BLOCKED_WORDS.some((blocked) => normalized.includes(normalizeForPolicy(blocked)));
  if (hasBlockedWord) {
    return "사용할 수 없는 닉네임입니다.";
  }

  return null;
}

export async function isNicknameTaken(nickname: string, excludeUserId?: string) {
  const existing = await prisma.user.findFirst({
    where: {
      nickname: { equals: nickname.trim(), mode: "insensitive" },
      ...(excludeUserId ? { id: { not: excludeUserId } } : {})
    },
    select: { id: true }
  });
  return Boolean(existing);
}

export async function buildAvailableNickname(preferredNickname?: string | null, seed?: string) {
  const base = preferredNickname?.trim() || DEFAULT_NICKNAME;
  const policyError = validateNicknamePolicy(base);
  const safeBase = policyError ? DEFAULT_NICKNAME : base;

  if (!(await isNicknameTaken(safeBase))) {
    return safeBase;
  }

  const seedDigits = (seed ?? "").replace(/\D/g, "").slice(-4);
  const numberedBase = safeBase.slice(0, Math.max(1, MAX_NICKNAME_LENGTH - 5));

  if (seedDigits.length > 0) {
    const candidate = `${numberedBase}${seedDigits}`.slice(0, MAX_NICKNAME_LENGTH);
    if (!(await isNicknameTaken(candidate))) {
      return candidate;
    }
  }

  for (let idx = 1; idx <= 9999; idx += 1) {
    const suffix = String(idx).padStart(4, "0");
    const candidate = `${numberedBase}${suffix}`.slice(0, MAX_NICKNAME_LENGTH);
    if (!(await isNicknameTaken(candidate))) {
      return candidate;
    }
  }

  return `${DEFAULT_NICKNAME}${Date.now().toString().slice(-4)}`.slice(0, MAX_NICKNAME_LENGTH);
}
