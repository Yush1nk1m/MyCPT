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

### @RequestMapping 클래스 레벨 prefix 규칙

컨트롤러가 다루는 리소스가 **단일 prefix로 묶이는지 여부**에 따라 클래스 레벨 `@RequestMapping`을 다르게 잡는다.

- **단일 리소스 도메인** — 컨트롤러가 다루는 엔드포인트가 전부 같은 prefix 아래 있을 때, 클래스 레벨에 전체 경로를 명시하고 메서드에는 나머지 경로만 작성한다.
  - 예: `AssessmentV1Controller` → `@RequestMapping("/api/v1/assessments")`, 메서드는 `@PostMapping`, `@GetMapping("/{token}")` 등
  - 예: `CoinV1Controller` → `@RequestMapping("/api/v1/coins")`, 메서드는 `@GetMapping`, `@GetMapping("/history")`

- **다중 리소스 도메인** — 한 컨트롤러가 서로 다른 prefix를 가진 리소스 여러 개를 함께 다룰 때(예: `/peer-code`와 `/colleagues`처럼 별개 리소스), 클래스 레벨은 `@RequestMapping("/api/v1")`까지만 잡고 메서드마다 풀 경로를 직접 명시한다.
  - 예: `ColleagueV1Controller` → `@RequestMapping("/api/v1")`, 메서드는 `@GetMapping("/peer-code")`, `@PostMapping("/colleagues")`, `@GetMapping("/colleagues/{colleagueId}")` 등

판단 기준: 새 컨트롤러 작성 시, 그 컨트롤러의 모든 엔드포인트가 공통 prefix 하나로 묶이면 단일 리소스, 둘 이상의 서로 다른 리소스 명사를 다루면 다중 리소스로 본다.

### OAuth2 로그인 후 리다이렉트 화이트리스트

로그인 완료 후 이동 경로는 `SecurityConfig`의 `ALLOWED_REDIRECTS` Set으로 관리한다.

현재 허용 경로:

- `/` — 일반 로그인
- `/save-result` — 비회원 검사 후 로그인 시 결과 자동 저장

새 경로 추가 시 `SecurityConfig.ALLOWED_REDIRECTS`에만 추가하면 된다.
프론트에서 `redirect` 파라미터로 임의 경로를 넘겨도 화이트리스트에 없으면 `/`로 이동한다.

## 2. 프로필 이미지 저장 방식

- profile_image_url 컬럼에 항상 Full URL 저장
- 카카오 기본 이미지: 카카오가 제공하는 URL 그대로 저장
- S3 업로드 후: S3 키가 아닌 조합된 Full URL 저장
- 프론트엔드에서 URL 조합 로직 불필요
