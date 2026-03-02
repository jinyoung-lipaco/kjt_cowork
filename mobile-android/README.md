# Android 앱 개발 계획

## 기본값
- 앱명: 소행성
- 패키지명: com.jinyoung.sohangseong
- 최소 SDK: 26
- 타깃 SDK: 최신 안정 버전

## 권장 스택
- Kotlin
- Jetpack Compose
- Hilt
- Retrofit + OkHttp
- Room (오프라인 캐시)
- Firebase Messaging

## 개발 순서
1. 앱 셸(네비게이션/테마) 구축
2. 인증(이메일/카카오/구글) 연결
3. 커뮤니티/스탠다드/프로필 화면 연동
4. 푸시/딥링크/에러 로깅 적용
5. QA 및 Play 배포

## 참고
- 현재 저장소의 `prototype.html`은 UI/기획 기준서 역할로 유지
- 실서비스 앱은 네이티브로 재구현
