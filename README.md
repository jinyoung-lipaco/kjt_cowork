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
npm run prisma:seed
npm run dev
```

## Docker 서버 실행(친구 테스트용)

```bash
cd /Users/elbert.kim/Desktop/kjt_cowork
docker compose up -d --build
```

- API 확인: `http://<PC_IP_OR_DOMAIN>:4000/api/health`
- 앱에서 사용할 API URL 예시: `http://<PC_IP_OR_DOMAIN>:4000/api/`
- 외부 네트워크 테스트 시 공유기/방화벽에서 `4000` 포트 개방이 필요합니다.

기본 헬스체크:
- `GET http://localhost:4000/api/health`

## 1차 API
- `POST /api/auth/sign-up`
- `POST /api/auth/sign-in`
- `POST /api/auth/refresh`
- `POST /api/auth/social/google`
- `POST /api/auth/social/kakao`
- `GET /api/posts`
- `GET /api/posts/:postId`
- `POST /api/posts`
- `POST /api/posts/comments`
- `GET /api/users/:userId/profile-summary`
- `PATCH /api/users/:userId/profile`
- `GET /api/standards/polls`
- `GET /api/standards/polls/:pollId`
- `POST /api/standards/polls`
- `POST /api/standards/polls/:pollId/vote`
- `GET /api/standards/approved-items`
- `GET /api/standards/approved-items/:itemId`
- `POST /api/standards/approved-items`

## 다음 우선순위
1. 닉네임 정책 고도화(금칙어 사전 확장/관리 도구)
2. 인증 보안 고도화(리프레시 토큰 로테이션, 로그아웃/폐기)
3. 관리자 기능(신고 처리/차단) 구현
