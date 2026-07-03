# MyCPT 유지보수 가이드

**문서 버전**: v0.2
**작성일**: '26.07.03.
**작성자**: 김유신

## 변경 이력

| 버전 | 변경 내용                                                                                                   | 날짜       |
| ---- | ----------------------------------------------------------------------------------------------------------- | ---------- |
| v0.1 | 초안 작성 (API 버저닝, 컨트롤러 네이밍 규칙, 프로필 이미지 저장 방식)                                       | '26.05.29. |
| v0.2 | "3. 케미 캐시 실패 복구 구조" 섹션 추가 (동기 즉시 리셋 vs 배치 지연 복구 2계층 서술) — 버전 헤더 소급 추가 | '26.07.03. |

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

### OAuth2 로그인 후 리다이렉트 처리

로그인 완료 후 이동 경로는 고정 화이트리스트가 아니라 `SecurityConfig.isSafeRedirect()`의
상대 경로 검증을 통과하면 어떤 경로든 허용한다.

```java
private static boolean isSafeRedirect(String path) {
    if (path == null || path.isBlank()) return false;
    if (path.startsWith("http") || path.startsWith("//")) return false;
    return path.startsWith("/");
}
```

**동작 방식**

1. 프론트에서 `GET /api/v1/auth/kakao?returnTo=<경로>` 호출
2. `AuthV1Controller.kakaoLogin()`이 `returnTo`를 `oauth2_redirect` 쿠키에 저장 (5분 TTL — OAuth 리다이렉트 체인 중 쿼리 파라미터가 소실되므로 쿠키로 우회)
3. 로그인 성공 후 `successHandler`가 쿠키값을 꺼내 `isSafeRedirect()` 검증
4. 통과 시 해당 경로로, 실패 시(절대 URL·프로토콜 상대 URL·빈 값 등 오픈 리다이렉트 위험 패턴) `/`로 리다이렉트

**허용 기준**: `/`로 시작하는 상대 경로면 전부 허용 (`http`, `//` 시작만 차단).
동적 라우트(`/invite/[code]`, `/colleagues/[id]` 등)도 별도 등록 없이 그대로 사용 가능.
새 경로 추가 시 코드 수정 불필요 — 프론트에서 `returnTo`만 올바르게 넘기면 된다.

## 2. 프로필 이미지 저장 방식

- profile_image_url 컬럼에 항상 Full URL 저장
- 카카오 기본 이미지: 카카오가 제공하는 URL 그대로 저장
- S3 업로드 후: S3 키가 아닌 조합된 Full URL 저장
- 프론트엔드에서 URL 조합 로직 불필요

## 3. 케미 캐시 실패 복구 구조

케미 캐시(`chemistry_cache`)는 발행자 경로 실패에 대해 **동기 즉시 복구**와 **배치 지연 복구** 두 계층으로 대응한다. 서로 다른 실패 시나리오를 커버하므로 하나로 통합하지 않는다.

### 계층 1 — 동기 즉시 리셋 (`ChemistryTxHelper.resetCacheOnPublishFailure`)

- 발행자가 `llmClient.complete()` 호출 중 예외를 던지면, `ChemistryCacheService.generateAsPublisher()`가 이를 잡아 캐시를 즉시 `GENERATING → NULL`로 되돌린다.
- try-catch를 `llmClient.complete()` 호출에만 narrow하게 잡는 이유: 이후 로직(`saveCompletedCache`, `publishReady`)에서 발생한 예외까지 캐시를 리셋하면 이미 완료된 LLM 응답이 유실된다.
- 리셋 직후 예외를 다시 던지므로(`throw e`), 실제 재시도/실패 처리는 호출자인 `ChemistryReportProcessor`의 `@Retryable`이 담당한다.

### 계층 2 — 배치 지연 복구 (`ChemistryCacheRecoveryScheduler`, 10분 주기)

- 계층 1이 실행되지 못하는 경우(서버 프로세스 강제 종료, `@Async` 스레드 자체가 죽는 경우 등)를 대비한 안전망.
- `chemistry_cache.status='GENERATING' AND updated_at < NOW() - 10분` 조건으로 스테일 행 탐지 후 재시도.
- **주의**: 이 배치는 캐시 상태를 `NULL`로 되돌리지 않고 `GENERATING`을 유지한 채 `updated_at`만 갱신한다. 재시도 착수와 동시에 실유저 요청이 끼어들어 별도 발행자가 되는 경쟁을 막기 위함 — DB 근거는 `database-design.md` §5 참조.

### 참고 — 구독자 타임아웃과의 구분

`chemistry.subscriber-wait-timeout-seconds`(기본 300초)는 구독자가 발행자의 완료를 기다리는 대기 한도이며, 위 두 복구 계층과는 별개 타이머다. 구독자가 타임아웃되면 즉시 `ERROR`로 전이하고 재시도하지 않는다 — 캐시 자체의 복구는 여전히 계층 1/2에 달려 있으므로, 이후 재조회 시 복구된 캐시를 받을 수 있다.
