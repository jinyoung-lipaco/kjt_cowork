# 소행성 (kjt_cowork)

실서비스 개발을 위한 저장소입니다.  
기존 HTML 프로토타입을 기준으로 백엔드/API와 안드로이드 앱을 단계적으로 구축합니다.

## 현재 구성
- `prototype.html`: 화면/기획 기준 프로토타입
- `index.html`: 프로토타입 진입 페이지
- `backend/`: TypeScript + Fastify + Prisma 기반 API 서버
- `mobile-android/`: Android 네이티브 앱 개발 계획
- `docs/`: 요구사항 및 아키텍처 문서

## 백엔드 빠른 시작

```bash
cd backend
npm install
cp .env.example .env
npm run prisma:generate
npm run prisma:migrate
npm run dev
```

기본 헬스체크:
- `GET http://localhost:4000/api/health`

## 1차 API
- `POST /api/auth/sign-up`
- `POST /api/auth/sign-in`
- `POST /api/auth/social/google`
- `POST /api/auth/social/kakao`
- `GET /api/posts`
- `POST /api/posts`
- `POST /api/posts/comments`
- `GET /api/standards/polls`
- `GET /api/standards/polls/:pollId`
- `POST /api/standards/polls`
- `POST /api/standards/polls/:pollId/vote`
- `GET /api/standards/approved-items`
- `GET /api/standards/approved-items/:itemId`
- `POST /api/standards/approved-items`

## 다음 우선순위
1. 소셜 로그인(카카오/구글) 추가
2. 표준 투표/인정템 API 설계 및 구현
3. 관리자 기능(신고 처리/차단) 구현
4. Android 앱 Compose 베이스 프로젝트 생성
