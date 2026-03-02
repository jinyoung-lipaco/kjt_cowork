import bcrypt from "bcryptjs";
import { AuthProvider, PrismaClient, UserTier } from "@prisma/client";

const prisma = new PrismaClient();

async function seedUsers() {
  const defaultPasswordHash = await bcrypt.hash("test1234!", 10);

  const seedMom = await prisma.user.upsert({
    where: { email: "seedmom@sohangseong.dev" },
    update: { nickname: "씨앗맘", tier: UserTier.SEED_MOM },
    create: {
      email: "seedmom@sohangseong.dev",
      nickname: "씨앗맘",
      tier: UserTier.SEED_MOM,
      passwordHash: defaultPasswordHash
    }
  });

  const starMom = await prisma.user.upsert({
    where: { email: "starmom@sohangseong.dev" },
    update: { nickname: "별맘", tier: UserTier.STAR_MOM },
    create: {
      email: "starmom@sohangseong.dev",
      nickname: "별맘",
      tier: UserTier.STAR_MOM,
      passwordHash: defaultPasswordHash
    }
  });

  await prisma.authIdentity.upsert({
    where: {
      provider_providerUserId: {
        provider: AuthProvider.GOOGLE,
        providerUserId: "google-seed-user-001"
      }
    },
    update: { userId: seedMom.id },
    create: {
      userId: seedMom.id,
      provider: AuthProvider.GOOGLE,
      providerUserId: "google-seed-user-001"
    }
  });

  await prisma.authIdentity.upsert({
    where: {
      provider_providerUserId: {
        provider: AuthProvider.KAKAO,
        providerUserId: "kakao-seed-user-001"
      }
    },
    update: { userId: starMom.id },
    create: {
      userId: starMom.id,
      provider: AuthProvider.KAKAO,
      providerUserId: "kakao-seed-user-001"
    }
  });

  return { seedMom, starMom };
}

async function seedStandards() {
  await prisma.voteAnswer.deleteMany();
  await prisma.voteOption.deleteMany();
  await prisma.votePoll.deleteMany();
  await prisma.approvedItem.deleteMany();

  await prisma.votePoll.create({
    data: {
      title: "유아 로션 향료 기준 강화",
      description: "합성 향료 사용 기준을 강화할지 투표합니다.",
      status: "LIVE",
      closesAt: new Date(Date.now() + 1000 * 60 * 60 * 24 * 5),
      options: {
        create: [
          { label: "전면 제한", sortOrder: 1 },
          { label: "조건부 허용", sortOrder: 2 },
          { label: "현행 유지", sortOrder: 3 }
        ]
      }
    }
  });

  await prisma.votePoll.create({
    data: {
      title: "어린이 치약 불소 함량 가이드",
      description: "연령별 권장 함량 가이드를 소행성 기준으로 확정합니다.",
      status: "LIVE",
      closesAt: new Date(Date.now() + 1000 * 60 * 60 * 24 * 7),
      options: {
        create: [
          { label: "연령별 상세 기준", sortOrder: 1 },
          { label: "단일 기준", sortOrder: 2 }
        ]
      }
    }
  });

  await prisma.approvedItem.createMany({
    data: [
      {
        category: "스킨케어",
        name: "소행성 베이비 수딩로션",
        brand: "소행성랩",
        safetyScore: 96,
        priceText: "18,900원",
        criteriaText: "향료/색소 무첨가, 민감성 테스트 완료"
      },
      {
        category: "세정",
        name: "소행성 버블 바디워시",
        brand: "소행성랩",
        safetyScore: 93,
        priceText: "15,900원",
        criteriaText: "EWG 그린 등급 원료 중심 구성"
      },
      {
        category: "구강",
        name: "키즈 저불소 치약",
        brand: "스마일키즈",
        safetyScore: 91,
        priceText: "6,900원",
        criteriaText: "연령별 권장 함량 범위 충족"
      }
    ]
  });
}

async function seedCommunity(seedMomId: string, starMomId: string) {
  await prisma.comment.deleteMany();
  await prisma.post.deleteMany();

  const post = await prisma.post.create({
    data: {
      title: "이번 주 기준 투표 참여 부탁드려요",
      body: "향료 기준 강화 안건이 올라왔어요. 의견 남겨주세요!",
      authorId: seedMomId
    }
  });

  await prisma.comment.createMany({
    data: [
      { postId: post.id, authorId: starMomId, body: "조건부 허용안이 현실적일 것 같아요." },
      { postId: post.id, authorId: seedMomId, body: "투표 마감 전까지 꼭 참여 부탁드려요 🙏" }
    ]
  });
}

async function main() {
  const { seedMom, starMom } = await seedUsers();
  await seedStandards();
  await seedCommunity(seedMom.id, starMom.id);
}

main()
  .then(async () => {
    await prisma.$disconnect();
  })
  .catch(async (error) => {
    console.error(error);
    await prisma.$disconnect();
    process.exit(1);
  });
