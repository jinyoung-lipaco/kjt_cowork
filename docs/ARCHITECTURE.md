# 소행성 아키텍처 v1

## 구성
- 모바일 앱: Android (Kotlin + Jetpack Compose)
- API 서버: Node.js + TypeScript + Fastify
- DB: PostgreSQL
- ORM: Prisma
- 인증: JWT Access + Refresh Token
- 파일 저장: S3 호환 스토리지
- 푸시: Firebase Cloud Messaging

## 백엔드 모듈
- auth: 로그인/토큰/세션
- users: 유저 프로필/차단
- posts: 게시글/댓글/리액션
- standards: 투표/인정템
- reports: 신고/처리
- admin: 운영자 기능

## 데이터 핵심 엔티티
- User
- AuthIdentity (provider 연결)
- Post, Comment, Reaction
- VotePoll, VoteOption, VoteAnswer
- ApprovedItem
- Report, BlockRelation

## 배포
- API: Docker 컨테이너
- DB: 관리형 PostgreSQL 권장
- CI/CD: GitHub Actions
- 앱 배포: Play Console (AAB)
