# Android 앱 (초기 구현)

## 현재 구현 상태
- Jetpack Compose 기반 앱 셸 생성
- 이메일 로그인 API 연동 (`POST /api/auth/sign-in`)
- 카카오 로그인 SDK 연동 + 백엔드 연동 (`POST /api/auth/social/kakao`)
- 구글 로그인 SDK 연동 + 백엔드 연동 (`POST /api/auth/social/google`)
- 401 응답 시 토큰 재발급 자동 처리 (`POST /api/auth/refresh`)
- 로그인 성공 후 메인 3탭 화면 구성 (커뮤니티/스탠다드/프로필)
- 메인 탭 생성/투표 액션 API 연결 (`POST /api/posts`, `POST /api/standards/polls/:pollId/vote`)
- 커뮤니티 댓글 작성/조회 UX 연결 (`POST /api/posts/comments`)
- 프로필 탭 사용자 상세 API 연동 (`GET /api/users/:userId/profile-summary`)
- 소셜 로그인 실패 UX 개선(취소/네트워크/권한 메시지 분기)
- 재발급 실패 시 세션 만료 안내 후 로그인 화면 복귀 강화
- 프로필 가입일/최근 활동 히스토리 표시 확장
- 탭별 로딩/빈 상태 UI 개선
- 커뮤니티/스탠다드 목록 ↔ 상세 화면 분리
- 투표 결과 차트/비율 UI 추가
- 오프라인 감지형 재시도 UX 강화(에러 카드 + 재시도 버튼)
- 프로필 탭 닉네임 수정 API 연동 (`PATCH /api/users/:userId/profile`)
- 로그인/메인 화면 공통 스낵바 스타일 및 지속시간 세분화
- 프로필 편집 확장(닉네임/소개/관심 카테고리 저장)
- Access/Refresh 토큰 로컬 저장 (`DataStore`)
- 로그아웃(토큰 삭제) 동작 연결

## 기본값
- 앱명: 소행성
- 패키지명: `com.jinyoung.sohangseong`
- 최소 SDK: 26
- 타깃 SDK: 35

## 주요 파일
- `app/src/main/java/com/jinyoung/sohangseong/MainActivity.kt`
- `app/src/main/java/com/jinyoung/sohangseong/ui/auth/LoginScreen.kt`
- `app/src/main/java/com/jinyoung/sohangseong/ui/auth/LoginViewModel.kt`
- `app/src/main/java/com/jinyoung/sohangseong/data/network/*`
- `app/src/main/java/com/jinyoung/sohangseong/data/store/TokenStore.kt`

## API 연결 기준
- 기본 Base URL: `http://10.0.2.2:4000/api/` (에뮬레이터 로컬)
- 친구 테스트용 APK 빌드 시 Base URL 오버라이드:
  - `./gradlew :app:assembleRelease -PAPI_BASE_URL=http://<PC_IP_OR_DOMAIN>:4000/api/`

## 소셜 키 설정
- 파일: `app/src/main/res/values/strings.xml`
- 아래 값을 실제 키로 교체
  - `google_web_client_id`
  - `kakao_native_app_key`
- 백엔드(`backend/.env`)에도 `GOOGLE_CLIENT_ID`를 같은 구글 웹 클라이언트 ID로 맞춰야 합니다.

## 실행 순서
1. `backend` 서버 실행 (`npm run dev`)
2. Android Studio에서 `mobile-android` 열기
3. Gradle Sync 후 에뮬레이터 실행
4. 시드 계정으로 로그인 테스트
   - `seedmom@sohangseong.dev` / `test1234!`
   - `starmom@sohangseong.dev` / `test1234!`

## Docker 백엔드 + APK 빌드
1. 서버 실행:
   - `cd /Users/elbert.kim/Desktop/kjt_cowork`
   - `docker compose up -d --build`
2. 친구용 APK 빌드:
   - `cd /Users/elbert.kim/Desktop/kjt_cowork/mobile-android`
   - `./gradlew :app:assembleRelease -PAPI_BASE_URL=http://<PC_IP_OR_DOMAIN>:4000/api/`
3. 산출물:
   - `app/build/outputs/apk/release/app-release.apk`

## 다음 단계
1. 스낵바 액션 패턴 삭제 플로우로 확장(게시글/댓글)
2. 프로필 관심 카테고리 선택 UI 칩화
3. APK 빌드 파이프라인 정식화(gradlew + assembleDebug)
