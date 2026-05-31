# MyCPT 유지보수 가이드

## 1. API 버저닝

### SecurityConfig 경로 하드코딩 문제

- 현재 SecurityConfig의 permitAll 경로에 /api/v1/이 하드코딩되어 있음
- V2 도입 시 아래 경로를 수동으로 추가 필요:
  /api/v2/questions
  /api/v2/results/score
  /api/v2/assessments/_
  /api/v2/assessments/_/submit
  /api/v2/auth/kakao
- 근본 해결: ApiPaths.java 상수 클래스로 분리 후 SecurityConfig에서 참조 권장

### 컨트롤러 네이밍 규칙

- 인터페이스: {도메인}Api.java (예: AuthApi.java)
- V1 구현체: {도메인}V1Controller.java (예: AuthV1Controller.java)
- V2 추가 시: {도메인}V2Controller implements {도메인}Api

## 2. 프로필 이미지 저장 방식

- profile_image_url 컬럼에 항상 Full URL 저장
- 카카오 기본 이미지: 카카오가 제공하는 URL 그대로 저장
- S3 업로드 후: S3 키가 아닌 조합된 Full URL 저장
- 프론트엔드에서 URL 조합 로직 불필요
