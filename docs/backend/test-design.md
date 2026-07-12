# MyCPT 테스트 설계 문서

**문서 버전**: v0.11
**작성일**: '26.07.12.
**작성자**: 김유신

---

## 변경 이력

| 버전  | 변경 내용                                                                                                                                                                                                                                                                                                                                                                    | 날짜       |
| ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| v0.1  | 초안 작성. 레이어별 전략, Auth/User 도메인 테스트 케이스 ID 체계 수립.                                                                                                                                                                                                                                                                                                       | '26.05.26. |
| v0.2  | Result/Assessment 도메인 테스트 케이스 추가. JpaTestSupport 기반 슬라이스 테스트 전략 확정.                                                                                                                                                                                                                                                                                  | '26.06.09. |
| v0.3  | 예외 처리 체계 통합 반영 (BusinessException/ErrorCode). ScoringService 경계값 케이스 보강.                                                                                                                                                                                                                                                                                   | '26.06.13. |
| v0.4  | `DiscResultRepository` → `DiscTestRepository` 이름 변경. Test ID `ST-DiscResultRepo-*` → `ST-DiscTestRepo-*` 전면 변경. `ResultService` 저장 행위 설명 `disc_results` → `disc_tests` 수정. `AssessmentService` 평정 제출 상황 설명 `DiscResult` → `DiscTest` 수정.                                                                                                           | '26.06.15. |
| v0.5  | §8~§11 누락된 Test Code 링크 4건 추가(Statistics/PeerCode/Colleague/Notification Service). `ColleagueV1Controller` ST 테스트 미작성 상태 명시(TODO).                                                                                                                                                                                                                         | '26.06.20. |
| v0.6  | §10 Chemistry 도메인 테스트 케이스 추가 (UT 7건, ST 6건, IT 5건).                                                                                                                                                                                                                                                                                                            | '26.06.24. |
| v0.7  | §10 Chemistry 도메인 테스트 케이스 전면 재작성. ChemistryCacheLockTx 트랜잭션 격리 IT 추가 (Self-invocation 해결 검증). ChemistryCache 엔티티 UT 상태전이 케이스 교체 (create/refresh → startGenerating/complete/refresh). Lazy Caching IT 3건 + Duplication Defense IT 2건 추가. ChemistryCacheStatus 신규 enum UT 추가.                                                    | '26.06.25. |
| v0.8  | §10 Chemistry 도메인 전면 재작성. ChemistryTxHelper(구 ChemistryCacheLockTx) 네이밍 변경 반영. `ChemistryReport.create(cacheId)` 시그니처 변경 반영 (생성 시점 cacheId 주입). `completeReport()` ChemistryTxHelper 이관 반영. ChemistryTxHelper IT 케이스 3건으로 확장. Lazy Caching IT 3건 + Duplication Defense IT 2건 확정.                                               | '26.06.25. |
| v0.9  | §10 Chemistry 도메인: 캐시만료경계 케이스 제거 (구현 세부사항 검증으로 IT 목적 부적합). 동시요청 2개 케이스 제거 (3개 케이스가 포함 관계). @TransactionalEventListener AFTER_COMMIT 검증 섹션 신규 추가.                                                                                                                                                                     | '26.06.28. |
| v0.10 | §13(스케줄러·도메인 비결합)/§14(통합 테스트 시나리오) 섹션 신설. §10 Chemistry: Subscriber Timeout IT, Concurrency Load Test IT(@RepeatedTest 30), ChemistryCacheRecoveryScheduler IT 3건, recover() 이벤트파라미터매칭 IT 추가. §13: ExpiredDataCleanupScheduler IT 추가. — 07.01~07.03 작업분 지연 반영                                                                    | '26.07.03. |
| v0.11 | 회원 탈퇴 테스트 케이스 추가 — §5 User(UserService UT 5건 + UserV1Controller ST 2건), §6 Result(DiscTestRepository 3건), §7 Assessment(AssessmentTokenRepository 신규 1건), §9 Colleague(PeerCodeRepository 신규 1건), §10 Chemistry(ChemistryReportRepository 1건), §12 Coin(CoinTransactionRepository 신규 1건), §14 통합 테스트(UserWithdraw 6건 — 상대방 관점 4건 포함). | '26.07.12. |

---

## 목차

- [1. 테스트 전략](#1-테스트-전략)
- [2. 테스트 ID 체계](#2-테스트-id-체계)
- [3. 도구 및 환경](#3-도구-및-환경)
- [4. Auth 도메인](#4-auth-도메인)
- [5. User 도메인](#5-user-도메인)
- [6. Result 도메인](#6-result-도메인)
- [7. Assessment 도메인](#7-assessment-도메인)
- [8. Statistics 도메인](#8-statistics-도메인)
- [9. Colleague 도메인](#9-colleague-도메인)
- [10. Chemistry 도메인](#10-chemistry-도메인)
- [11. Notification 도메인](#11-notification-도메인)
- [12. Coin 도메인](#12-coin-도메인)
- [13. 스케줄러 (도메인 비결합)](#13-스케줄러-도메인-비결합)
- [14. 통합 테스트 시나리오](#14-통합-테스트-시나리오)

---

## 1. 테스트 전략

### 핵심 원칙

- 구현(메서드 내부 단계)이 아닌 **행위(입력 → 출력)**를 검증한다
- 모든 도메인에 UT/ST/IT를 다 쓰지 않는다. 검증의 핵심이 무엇인지에 따라 필요한 종류만 선택한다
- DB 저장 여부 및 실제 ID 값 검증은 ST가 아닌 IT에서 수행한다
- 비즈니스 예외는 모두 `BusinessException`으로 래핑하며, 예외 검증 시 `getErrorCode()`로 의미를 확인한다

### 테스트 종류 선택 기준

| 질문                                       | 테스트 종류 |
| ------------------------------------------ | ----------- |
| HTTP 계약(상태코드, 인증 분기)이 핵심인가? | ST          |
| 순수 비즈니스 로직인가?                    | UT          |
| DB/Redis 실제 연동이 검증의 핵심인가?      | ST or IT    |
| 여러 도메인이 연결된 전체 흐름인가?        | IT          |

### 레이어별 도구

| 종류 | 어노테이션                      | 도구                           | 작성 시점        |
| ---- | ------------------------------- | ------------------------------ | ---------------- |
| UT   | `@ExtendWith(MockitoExtension)` | JUnit 5 + Mockito              | 기능 구현과 동시 |
| ST   | `@WebMvcTest`                   | MockMvc + spring-security-test | 기능 구현과 동시 |
| ST   | `@DataJpaTest`                  | Testcontainers (MySQL) + JPA   | 기능 구현과 동시 |
| IT   | `@SpringBootTest`               | Testcontainers (MySQL, Redis)  | 4주차 QA 단계    |

### 예외 검증 패턴

모든 비즈니스 예외는 `BusinessException`으로 통합되었으므로 아래 패턴을 사용한다.

```java
// 기본 패턴 — ErrorCode만 검증하면 충분한 경우
assertThatThrownBy(() -> sut().method(args))
    .isInstanceOf(BusinessException.class)
    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.FORBIDDEN));

// 메시지까지 검증이 필요한 경우 (ScoringService처럼 상세 값 포함)
assertThatThrownBy(() -> sut().method(args))
    .isInstanceOf(BusinessException.class)
    .satisfies(e -> {
        BusinessException be = (BusinessException) e;
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_SCORE);
        assertThat(be.getMessage()).contains("49");
    });
```

### 자동화 제외 영역 (Swagger UI 수동 테스트)

- 카카오 OAuth2 전체 플로우 (리다이렉트 → 콜백 → JWT 쿠키 발급)
- SSE 연결 및 실시간 푸시 수신
- 카카오 연결 해제 (회원 탈퇴)

---

## 2. 테스트 ID 체계

### 형식

```
[테스트종류]-[클래스명축약]-[행위]-[상황]

예시:
UT-OAuth2UserSvc-신규회원가입-성공
ST-AuthController-사용자인증-미인증접근
IT-AuthFlow-로그인후JWT쿠키발급-성공
```

### 테스트 종류 코드

| 코드 | 종류            | 어노테이션                      |
| ---- | --------------- | ------------------------------- |
| UT   | 단위 테스트     | `@ExtendWith(MockitoExtension)` |
| ST   | 슬라이스 테스트 | `@WebMvcTest` / `@DataJpaTest`  |
| IT   | 통합 테스트     | `@SpringBootTest`               |

### 규칙

- **행위**: 테스트 대상 기능 (명사구)
- **상황**: 성공 / 실패 조건 또는 엣지 케이스
- 단일 케이스면 상황 생략 가능

---

## 3. 도구 및 환경

| 항목                  | 내용                                                                             |
| --------------------- | -------------------------------------------------------------------------------- |
| 테스트 프레임워크     | JUnit 5                                                                          |
| Mock 라이브러리       | Mockito                                                                          |
| ST(MVC) 베이스 클래스 | `MvcTestSupport` — `@WebMvcTest` + Security 설정 + `authenticated()` 헬퍼        |
| ST(JPA) 베이스 클래스 | `JpaTestSupport` — `@DataJpaTest` + Testcontainers MySQL + datasource 오버라이드 |
| IT 베이스 클래스      | `IntegrationTestSupport` — `@SpringBootTest` + Testcontainers MySQL/Redis        |
| IT DB                 | Testcontainers (MySQL 8.0)                                                       |
| IT Redis              | Testcontainers (Redis 7.0)                                                       |
| 커버리지 측정         | JaCoCo (4주차 QA 단계 적용)                                                      |
| 수동 테스트           | Swagger UI (`/swagger-ui`)                                                       |

---

## 4. Auth 도메인

### CustomOAuth2UserService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/auth/service/CustomOAuth2UserServiceTest.java)]

> `loadUser()`는 내부에서 `super.loadUser()`(카카오 HTTP 호출)를 수행하므로
> 비즈니스 로직이 분리된 `findOrCreateUser()`를 직접 테스트한다.

| Test ID                                          | 행위                                       | 상황                                                                                          |
| ------------------------------------------------ | ------------------------------------------ | --------------------------------------------------------------------------------------------- |
| UT-OAuth2UserSvc-회원가입-성공                   | 신규 회원 첫 로그인 시 User 저장           | `findByKakaoId()` 결과 없음. `save()` 1회 호출, kakaoId/nickname/profileImageUrl/coins=3 검증 |
| UT-OAuth2UserSvc-기존회원로그인-성공             | 기존 회원 재로그인 시 조회만 수행          | `findByKakaoId()` 결과 있음. `save()` 0회 호출 검증                                           |
| UT-OAuth2UserSvc-properties누락시기본값적용-성공 | 카카오 응답 properties 누락 시 기본값 처리 | nickname="닉네임", profileImageUrl=null 검증                                                  |

### AuthV1Controller (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/auth/controller/AuthV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. `MvcTestSupport` 상속.

| Test ID                       | 행위                       | 상황                      |
| ----------------------------- | -------------------------- | ------------------------- |
| ST-AuthCtrl-내정보조회-성공   | 인증된 사용자 GET /auth/me | 200 + 응답 바디 필드 검증 |
| ST-AuthCtrl-내정보조회-미인증 | 미인증 GET /auth/me        | 401                       |

---

## 5. User 도메인

### User 엔티티 (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/user/entity/UserTest.java)]

| Test ID               | 행위              | 상황                                                                                            |
| --------------------- | ----------------- | ----------------------------------------------------------------------------------------------- |
| `UT-User-탈퇴-익명화` | `withdraw()` 호출 | `kakaoId`/`birthYear`/`gender` → null, `nickname`/`profileImageUrl` 유지, `deletedAt` 세팅 검증 |

---

### UserV1Controller (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/user/controller/UserV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. `MvcTestSupport` 상속.

| Test ID                             | 행위                                        | 상황                                      |
| ----------------------------------- | ------------------------------------------- | ----------------------------------------- |
| ST-UserCtrl-프로필수정-성공         | 인증된 사용자 PATCH /users/me               | 200 + 응답 바디 3개 필드 검증             |
| ST-UserCtrl-프로필수정-미인증       | 미인증 PATCH /users/me                      | 401                                       |
| ST-UserCtrl-탈퇴-성공               | 인증된 사용자 DELETE /users/me              | 200 + `accessToken` 쿠키 `Max-Age=0` 검증 |
| ST-UserCtrl-탈퇴-미인증             | 미인증 DELETE /users/me                     | 401                                       |
| ST-UserCtrl-탈퇴전카운트조회-성공   | 인증된 사용자 GET /users/me/withdrawal-info | 200 + 3개 필드 검증                       |
| ST-UserCtrl-탈퇴전카운트조회-미인증 | 미인증 GET /users/me/withdrawal-info        | 401                                       |

---

### UserService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/user/service/UserServiceTest.java)]

| Test ID                          | 행위                              | 상황                                                                                                        |
| -------------------------------- | --------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| UT-UserSvc-탈퇴-성공             | 정상 탈퇴                         | 각 repository 삭제 메서드 1회씩 호출, `kakaoUnlinkClient.unlink()` 호출, `user.withdraw()` 후 `save()` 검증 |
| UT-UserSvc-탈퇴-동료관계전체삭제 | 동료 N명 보유 상태 탈퇴           | `notificationService.deleteColleagueNotifications()` N회 호출, `colleagueRepository.deleteAll()` 호출 검증  |
| UT-UserSvc-탈퇴-카카오ID이미없음 | `kakaoId=null` 상태(방어)         | `kakaoUnlinkClient.unlink()` 미호출 검증                                                                    |
| UT-UserSvc-탈퇴-카카오unlink실패 | `kakaoUnlinkClient.unlink()` 예외 | 예외 그대로 전파, `userRepository.save()` 미호출 검증                                                       |
| UT-UserSvc-탈퇴전카운트조회-성공 | `getWithdrawalInfo()`             | `resultCount`/`chemistryCount`/`colleagueCount` 필드 매핑 검증                                              |

---

## 6. Result 도메인

### ScoringService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/result/service/ScoringServiceTest.java)]

| Test ID                                   | 행위                            | 상황                                                              |
| ----------------------------------------- | ------------------------------- | ----------------------------------------------------------------- |
| UT-ScoringSvc-버킷정규화-성공             | 정상 원점수 입력 시 버킷값 반환 | D=32, I=10, S=-4, C=-14 → 버킷 3,2,2,1 검증                       |
| UT-ScoringSvc-버킷정규화-최솟값최댓값혼합 | 최솟값/최댓값 경계 혼합 입력    | D=-24(버킷1), C=48(버킷3) 검증                                    |
| UT-ScoringSvc-합계검증-실패               | D+I+S+C ≠ 24                    | `BusinessException(INVALID_SCORE)`, 메시지에 "합계는 24여야" 포함 |
| UT-ScoringSvc-범위초과-상한               | 개별 원점수 48 초과             | `BusinessException(INVALID_SCORE)`, 메시지에 "49" 포함            |
| UT-ScoringSvc-범위초과-하한               | 개별 원점수 -24 미만            | `BusinessException(INVALID_SCORE)`, 메시지에 "-25" 포함           |
| UT-ScoringSvc-toBucket-경계값             | 버킷 전환점 전수 검증           | 3구간 하한/상한 6개 케이스 (-24→1, -5→1, -4→2, 11→2, 12→3, 48→3)  |

### DiscCacheService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/result/service/DiscCacheServiceTest.java)]

| Test ID                                          | 행위                               | 상황                                                                        |
| ------------------------------------------------ | ---------------------------------- | --------------------------------------------------------------------------- |
| UT-DiscCacheSvc-보고서생성-행누락예외            | 초기화 스크립트 미실행으로 행 없음 | `findById` empty → `IllegalStateException`, LLM 미호출                      |
| UT-DiscCacheSvc-보고서생성-미생성                | 사전 삽입 행이지만 report=NULL     | LLM 1회 호출 → `save` 1회 → 보고서 반환                                     |
| UT-DiscCacheSvc-보고서생성-캐시HIT유효           | DB에 유효한 캐시 존재              | `created_at` 10일 전, ttl=365일 → LLM 미호출, `save` 없음, 기존 보고서 반환 |
| UT-DiscCacheSvc-보고서생성-캐시HIT유효경계값분석 | TTL 당일 만료 경계 검증            | `created_at` 364일 전, ttl=365일 → 유효 처리, LLM 미호출                    |
| UT-DiscCacheSvc-보고서생성-캐시HIT만료           | DB에 만료된 캐시 존재              | `created_at` 366일 전, ttl=365일 → LLM 1회 호출, `save` 1회, 새 보고서 반환 |

### ResultService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/result/service/ResultServiceTest.java)]

| Test ID                              | 행위                      | 상황                                                         |
| ------------------------------------ | ------------------------- | ------------------------------------------------------------ |
| UT-ResultSvc-저장-성공               | tests + disc_tests INSERT | `testRepository.save()` 1회, `discTestRepository.save()` 1회 |
| UT-ResultSvc-저장-원점수오류         | 원점수 합계 불일치        | `BusinessException(INVALID_SCORE)`, save 미호출              |
| UT-ResultSvc-이력조회-성공           | 커서 없이 최신 목록 조회  | size+1 조회 후 hasNext 판단, results 반환                    |
| UT-ResultSvc-이력조회-다음페이지존재 | size+1개 조회 결과        | hasNext=true, nextCursor 설정 검증                           |
| UT-ResultSvc-이력조회-마지막페이지   | size 이하 조회 결과       | hasNext=false, nextCursor=null 검증                          |
| UT-ResultSvc-이력조회-raterType필터  | raterType=SELF 필터       | raterType 파라미터 리포지토리로 올바르게 전달 검증           |
| UT-ResultSvc-상세조회-성공           | 본인 결과 조회            | ResultDetailResponse 반환, CacheService 1회 호출 검증        |
| UT-ResultSvc-상세조회-권한없음       | 타인 결과 조회 시도       | `BusinessException(FORBIDDEN)`, CacheService 미호출          |
| UT-ResultSvc-상세조회-존재하지않는ID | 없는 testId 조회          | `BusinessException(NOT_FOUND)`                               |

### DiscTestRepository (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/result/repository/DiscTestRepositoryTest.java)]

> `@DataJpaTest` + Testcontainers MySQL (`JpaTestSupport` 상속).
> `@Sql("/sql/disc_cache_seed.sql")` 로 disc_cache 복합 FK 제약 해소.
> `@DataJpaTest` 기본 동작인 트랜잭션 롤백으로 테스트 간 데이터 격리.

| Test ID                                       | 행위                              | 상황                                                                         |
| --------------------------------------------- | --------------------------------- | ---------------------------------------------------------------------------- |
| ST-DiscTestRepo-커서페이지네이션-성공         | cursor=null 조회                  | 최신순 id DESC 정렬, 저장한 3개 전체 반환 검증                               |
| ST-DiscTestRepo-커서페이지네이션-커서검증     | cursor=N으로 조회                 | id < N인 결과만 반환 검증                                                    |
| ST-DiscTestRepo-raterType필터-SELF            | raterType=SELF 필터               | SELF 결과만 반환, OTHER 미포함 검증                                          |
| ST-DiscTestRepo-raterType필터-null            | raterType=null                    | SELF/OTHER 모두 반환 검증                                                    |
| ST-DiscTestRepo-상세조회-JoinFetch            | findByTestIdWithDetail            | test 연관관계 LAZY 정상 로드 (`LazyInitializationException` 미발생) 검증     |
| ST-DiscTestRepo-상세조회-존재하지않는ID       | 없는 testId 조회                  | `Optional.empty()` 반환 검증                                                 |
| ST-DiscTestRepo-사용자별전체조회-성공         | `findAllByUserId()`               | 본인 SELF+OTHER 결과 전체 반환, 타인 결과 미포함                             |
| ST-DiscTestRepo-사용자별카운트-성공           | `countByUserId()`                 | 정확한 건수 반환                                                             |
| ST-DiscTestRepo-사용자별삭제-부모자식모두삭제 | `deleteAll(findAllByUserId(...))` | `tests`/`disc_tests` 양쪽 테이블 모두 삭제 확인 — JOINED 삭제 순서 핵심 검증 |

---

## 7. Assessment 도메인

### AssessmentService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/assessment/service/AssessmentServiceTest.java)]

| Test ID                                  | 행위                       | 상황                                                    |
| ---------------------------------------- | -------------------------- | ------------------------------------------------------- |
| UT-AssessmentSvc-토큰생성-성공           | 회원이 타인 평정 링크 생성 | 32자 토큰 생성, expiresAt = now+7일 이후 검증, save 1회 |
| UT-AssessmentSvc-링크접속-성공           | 유효한 토큰으로 접속       | subjectNickname 반환 검증                               |
| UT-AssessmentSvc-링크접속-토큰없음       | 존재하지 않는 토큰         | `BusinessException(NOT_FOUND)`                          |
| UT-AssessmentSvc-링크접속-이미사용된토큰 | used=TRUE 토큰             | `BusinessException(TOKEN_USED)`                         |
| UT-AssessmentSvc-링크접속-만료된토큰     | expiresAt < now            | `BusinessException(EXPIRED_CODE)`                       |
| UT-AssessmentSvc-평정제출-성공           | 유효한 토큰으로 제출       | DiscTest save 1회, token.isUsed()=true                  |
| UT-AssessmentSvc-평정제출-이미사용된토큰 | used=TRUE 토큰             | `BusinessException(TOKEN_USED)`, DiscTest save 미호출   |

---

### AssessmentTokenRepository (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/assessment/repository/AssessmentTokenRepositoryTest.java)]

| Test ID                                  | 행위                  | 상황      |
| ---------------------------------------- | --------------------- | --------- |
| ST-AssessmentTokenRepo-대상자별삭제-성공 | `deleteBySubjectId()` | 삭제 검증 |

---

## 8. Statistics 도메인

### StatisticsService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/statistics/service/StatisticsServiceTest.java)]

| Test ID                              | 행위                                    | 상황                                                        |
| ------------------------------------ | --------------------------------------- | ----------------------------------------------------------- |
| UT-StatisticsSvc-비교조회-검사없음   | 본인 SELF 검사 이력 없음                | `my.buckets = null`, `average` 집계는 계속 진행             |
| UT-StatisticsSvc-비교조회-생년미입력 | `birthYear = null`                      | `average = null` 즉시 반환                                  |
| UT-StatisticsSvc-비교조회-성별미입력 | `gender = null`                         | `average = null` 즉시 반환                                  |
| UT-StatisticsSvc-비교조회-성별N      | `gender = N`                            | `average = null` 즉시 반환                                  |
| UT-StatisticsSvc-비교조회-샘플없음   | 나이대/성별 집계 결과 `sampleCount = 0` | `average = null` 반환                                       |
| UT-StatisticsSvc-비교조회-성공       | 모든 조건 충족                          | `average.ageGroupLabel`, `sampleCount > 0` 검증             |
| UT-StatisticsSvc-추이조회-결과없음   | `days` 기간 내 SELF 검사 없음           | `summary.count = 0`, `summary.average = null`, `trend = []` |
| UT-StatisticsSvc-추이조회-성공       | 검사 이력 있음                          | `entries` 수 일치, `summary.average` 산술 평균 정확성       |

---

## 9. Colleague 도메인

### PeerCodeService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/colleague/service/PeerCodeServiceTest.java)]

| Test ID                        | 행위                          | 상황                                            |
| ------------------------------ | ----------------------------- | ----------------------------------------------- |
| UT-PeerCodeSvc-코드조회-행없음 | 코드 행이 없을 때 getOrCreate | 신규 `PeerCode` save 1회, 코드/만료일 반환      |
| UT-PeerCodeSvc-코드조회-유효   | 기존 유효 코드 존재           | save 미호출, 기존 코드 그대로 반환              |
| UT-PeerCodeSvc-코드조회-만료   | 기존 만료 코드 존재           | `refresh()` 후 save 1회, 새 코드 반환           |
| UT-PeerCodeSvc-코드갱신-성공   | refresh() 호출                | save 1회, 새 코드 반환                          |
| UT-PeerCodeSvc-코드갱신-행없음 | 코드 행 없을 때 refresh       | 신규 생성 후 refresh, save 1회 (방어 로직 검증) |

### PeerCodeRepository (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/colleague/repository/PeerCodeRepositoryTest.java)]

| Test ID                           | 행위               | 상황      |
| --------------------------------- | ------------------ | --------- |
| ST-PeerCodeRepo-사용자별삭제-성공 | `deleteByUserId()` | 삭제 검증 |

---

### ColleagueRepository (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/colleague/repository/ColleagueRepositoryTest.java)]

> 기존에 `ColleagueRepository` 전용 슬라이스 테스트 파일이 없어 `countByUserId()`만 우선 신규 작성.
> `findAllByUserId`/`findByPair`/`existsByPair`는 현재 `ColleagueServiceTest`(UT, Mock)로만 커버되는 상태 — 별도 보강 필요 시 논의.

| Test ID                              | 행위              | 상황                                         |
| ------------------------------------ | ----------------- | -------------------------------------------- |
| ST-ColleagueRepo-사용자별카운트-성공 | `countByUserId()` | userA/userB 양쪽 역할 모두 카운트되는지 검증 |

---

### ColleagueService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/colleague/service/ColleagueServiceTest.java)]

| Test ID                                 | 행위                           | 상황                                                                            |
| --------------------------------------- | ------------------------------ | ------------------------------------------------------------------------------- |
| UT-ColleagueSvc-초대정보조회-성공       | 유효한 코드로 초대자 정보 조회 | `InviteInfoResponse` 반환 (inviter 닉네임/이미지 포함)                          |
| UT-ColleagueSvc-초대정보조회-코드없음   | 존재하지 않는 코드             | `BusinessException(NOT_FOUND)`                                                  |
| UT-ColleagueSvc-초대정보조회-만료코드   | `isExpired()` = true           | `BusinessException(EXPIRED_CODE)`                                               |
| UT-ColleagueSvc-초대정보조회-자기초대   | 초대자 == 요청자               | `BusinessException(SELF_INVITE)`                                                |
| UT-ColleagueSvc-초대정보조회-비회원     | myUserId=null (비회원)         | 자기초대 검증 스킵, `InviteInfoResponse` 정상 반환                              |
| UT-ColleagueSvc-동료등록-성공           | 유효한 코드로 동료 등록        | `Colleague` save 1회, `sendColleagueNotification` 1회, `ColleagueResponse` 반환 |
| UT-ColleagueSvc-동료등록-코드없음       | 존재하지 않는 코드             | `BusinessException(NOT_FOUND)`                                                  |
| UT-ColleagueSvc-동료등록-만료코드       | `isExpired()` = true           | `BusinessException(EXPIRED_CODE)`                                               |
| UT-ColleagueSvc-동료등록-자기초대       | 초대자 == 요청자               | `BusinessException(SELF_INVITE)`                                                |
| UT-ColleagueSvc-동료등록-이미동료       | `existsByPair()` = true        | `BusinessException(ALREADY_COLLEAGUE)`, save 미호출                             |
| UT-ColleagueSvc-동료목록조회-성공       | 동료 목록 조회                 | `ColleagueListResponse` 반환, 상대방 정보 포함                                  |
| UT-ColleagueSvc-동료프로필조회-성공     | 동료 관계인 상대 조회          | `ColleagueResponse` 반환                                                        |
| UT-ColleagueSvc-동료프로필조회-동료아님 | 동료 관계 없음                 | `BusinessException(FORBIDDEN)`                                                  |
| UT-ColleagueSvc-동료삭제-성공           | 동료 관계 삭제                 | `deleteColleagueNotifications` 1회 호출 후 `delete` 1회 호출                    |
| UT-ColleagueSvc-동료삭제-동료아님       | 동료 관계 없음                 | `BusinessException(FORBIDDEN)`, delete 미호출                                   |

### ColleagueV1Controller (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/colleague/controller/ColleagueV1ControllerTest.java)]

> `@WebMvcTest(ColleagueV1Controller.class)` 슬라이스 테스트. `MvcTestSupport` 상속.
> `PeerCodeService`, `ColleagueService`는 `@MockitoBean`으로 대체.
> 비즈니스 예외 분기는 `ColleagueServiceTest`(UT)에서 전담 검증. ST는 인증 분기만 다룬다.

| Test ID                                  | 행위                                           | 상황                                   |
| ---------------------------------------- | ---------------------------------------------- | -------------------------------------- |
| ST-ColleagueCtrl-코드조회-성공           | 인증된 사용자 GET /peer-code                   | 200 + 응답 바디 필드 검증              |
| ST-ColleagueCtrl-코드조회-미인증         | 미인증 GET /peer-code                          | 401                                    |
| ST-ColleagueCtrl-코드갱신-성공           | 인증된 사용자 POST /peer-code/refresh          | 200 + 응답 바디 필드 검증              |
| ST-ColleagueCtrl-코드갱신-미인증         | 미인증 POST /peer-code/refresh                 | 401                                    |
| ST-ColleagueCtrl-초대정보조회-성공       | 인증된 사용자 GET /colleagues/invite/{code}    | 200 + 응답 바디 필드 검증              |
| ST-ColleagueCtrl-초대정보조회-비회원성공 | 미인증 GET /colleagues/invite/{code}           | 200                                    |
| ST-ColleagueCtrl-동료등록-성공           | 인증된 사용자 POST /colleagues                 | 201 + 응답 바디 필드 검증              |
| ST-ColleagueCtrl-동료등록-미인증         | 미인증 POST /colleagues                        | 401                                    |
| ST-ColleagueCtrl-목록조회-성공           | 인증된 사용자 GET /colleagues                  | 200 + 응답 바디 `colleagues` 배열 검증 |
| ST-ColleagueCtrl-목록조회-미인증         | 미인증 GET /colleagues                         | 401                                    |
| ST-ColleagueCtrl-프로필조회-성공         | 인증된 사용자 GET /colleagues/{colleagueId}    | 200 + 응답 바디 필드 검증              |
| ST-ColleagueCtrl-프로필조회-미인증       | 미인증 GET /colleagues/{colleagueId}           | 401                                    |
| ST-ColleagueCtrl-삭제-성공               | 인증된 사용자 DELETE /colleagues/{colleagueId} | 200                                    |
| ST-ColleagueCtrl-삭제-미인증             | 미인증 DELETE /colleagues/{colleagueId}        | 401                                    |

---

## 10. Chemistry 도메인

### ChemistryReport 엔티티 (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/entity/ChemistryReportTest.java)]

| Test ID                              | 행위                                                 | 상황                                                              |
| ------------------------------------ | ---------------------------------------------------- | ----------------------------------------------------------------- |
| UT-ChemistryReport-상태전이-create   | `create(requester, partner, testType, cacheId)` 호출 | `status=GENERATING`, `cacheId` 생성 시점에 세팅, `createdAt` 세팅 |
| UT-ChemistryReport-상태전이-complete | `complete(cacheId)` 호출                             | `status=READY` 전이                                               |
| UT-ChemistryReport-상태전이-fail     | `fail()` 호출                                        | `status=ERROR`                                                    |

---

### ChemistryCacheId (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/entity/ChemistryCacheIdTest.java)]

| Test ID                             | 행위                                   | 상황                                |
| ----------------------------------- | -------------------------------------- | ----------------------------------- |
| UT-ChemistryCacheId-동등성-동일버킷 | 8축 값 동일 시 `equals()`/`hashCode()` | 동등                                |
| UT-ChemistryCacheId-동등성-다른버킷 | requester/partner 축 순서 교환         | 불일치 — A→B와 B→A는 별도 캐시 항목 |

---

### ChemistryCache 엔티티 (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/entity/ChemistryCacheTest.java)]

| Test ID                                    | 행위                         | 상황                                                      |
| ------------------------------------------ | ---------------------------- | --------------------------------------------------------- |
| UT-ChemistryCache-상태전이-startGenerating | `startGenerating()` 호출     | `status=GENERATING`                                       |
| UT-ChemistryCache-상태전이-complete        | `complete(report, now)` 호출 | `status=READY`, `report` 세팅, `createdAt` 세팅           |
| UT-ChemistryCache-상태전이-refresh         | `refresh()` 호출             | `status=GENERATING` 리셋, `report=null`, `createdAt=null` |

> **`ChemistryCacheService` / `ChemistryReportProcessor` / `ChemistryTxHelper` UT 제외 이유**
> 락 획득 → 발행자/구독자 분기 → Pub/Sub 발행 → SSE push까지의 흐름은 실제 MySQL 락 + Redis가 있는 IT 환경에서만 의미 있게 검증 가능하다.
> Self-invocation 해결 여부(트랜잭션 격리)도 별도 트랜잭션이 실제 커밋됐는지를 외부 커넥션에서 확인해야 하므로 IT가 필수다.

---

### ChemistryV1Controller (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/controller/ChemistryV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. `MvcTestSupport` 상속.

| Test ID                          | 행위                          | 상황                                                      |
| -------------------------------- | ----------------------------- | --------------------------------------------------------- |
| ST-ChemistryCtrl-발행-성공       | `POST /chemistry-reports`     | 인증됨 → `202 Accepted`                                   |
| ST-ChemistryCtrl-발행-미인증     | `POST /chemistry-reports`     | 미인증 → `401`                                            |
| ST-ChemistryCtrl-목록조회-성공   | `GET /chemistry-reports`      | 인증됨 → `200` + `reports` 배열 반환 (`myRole` 필드 포함) |
| ST-ChemistryCtrl-목록조회-미인증 | `GET /chemistry-reports`      | 미인증 → `401`                                            |
| ST-ChemistryCtrl-상세조회-성공   | `GET /chemistry-reports/{id}` | 인증됨 → `200` + `reportId`, `status` 반환                |
| ST-ChemistryCtrl-상세조회-미인증 | `GET /chemistry-reports/{id}` | 미인증 → `401`                                            |

---

### ChemistryCacheRepository / ChemistryReportRepository (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/repository/ChemistryRepositoryTest.java)]

> `@DataJpaTest` + Testcontainers MySQL (`JpaTestSupport` 상속).
> `chemistry_cache` 6,561행 시드 `@Sql("/sql/chemistry_cache_seed.sql")`로 주입.

| Test ID                                      | 행위                       | 상황                                                                      |
| -------------------------------------------- | -------------------------- | ------------------------------------------------------------------------- |
| ST-ChemistryCacheRepo-락-획득                | `findByIdWithLock()`       | `status=NULL` 행 → `PESSIMISTIC_WRITE` 락 획득, `status=NULL` 반환값 확인 |
| ST-ChemistryReportRepo-커서-ERROR필터        | `findByUserIdWithCursor()` | READY + ERROR 혼재 → ERROR 제외, READY만 반환                             |
| ST-ChemistryReportRepo-커서-partnerId필터    | `findByUserIdWithCursor()` | A↔B, A↔C 보고서 혼재 → `partnerId=B` 필터 시 B 관련만 반환                |
| ST-ChemistryReportRepo-커서-페이지네이션     | `findByUserIdWithCursor()` | 3건 삽입 후 중간 ID를 cursor로 → `id < cursor` 인 행만 반환               |
| `ST-ChemistryReportRepo-사용자별카운트-성공` | `countByUserId()`          | requester/partner 양쪽 역할 모두 카운트되는지 검증                        |

---

### ChemistryTxHelper 트랜잭션 격리 (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryTxHelperTest.java)]

> `IntegrationTestSupport` 상속. `@SpringBootTest` + Testcontainers MySQL/Redis.
> Self-invocation 문제를 `ChemistryTxHelper` 빈 분리로 해결했음을 검증.
> 핵심: 메서드 반환 직후 **별도 트랜잭션(새 커넥션)**에서 변경이 즉시 보이는가.
> 자가 호출이었다면 REQUIRES_NEW가 무시되어 외부 트랜잭션 커밋 전까지 변경이 보이지 않는다.

| Test ID                                                    | 행위                                                                                      | 상황                                                                           |
| ---------------------------------------------------------- | ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| IT-ChemistryTxHelper-트랜잭션격리-발행자GENERATING즉시반영 | `TransactionTemplate` 외부 트랜잭션 내 `acquireLockAndDecideRole()` 호출 → 외부 강제 롤백 | 외부 롤백 후 DB 조회 시 `status=GENERATING` 유지 — REQUIRES_NEW 독립 커밋 증명 |
| IT-ChemistryTxHelper-트랜잭션격리-완료캐시READY즉시반영    | `TransactionTemplate` 외부 트랜잭션 내 `saveCompletedCache()` 호출 → 외부 강제 롤백       | 외부 롤백 후 DB 조회 시 `chemistry_cache.status=READY` + `report` 유지         |
| IT-ChemistryTxHelper-트랜잭션격리-완료보고서READY즉시반영  | `TransactionTemplate` 외부 트랜잭션 내 `completeReport()` 호출 → 외부 강제 롤백           | 외부 롤백 후 DB 조회 시 `chemistry_reports.status=READY` 유지                  |

---

### ChemistryCacheService — Lazy Caching (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheServiceTest.java)]

> `IntegrationTestSupport` 상속.
> `AnthropicLlmClient` `@MockitoBean` — 실제 LLM 호출 없이 고정 문자열 반환.
> `chemistry_cache` 6,561행 시드 `@Sql("/sql/chemistry_cache_seed.sql")`로 주입.

| Test ID                                      | 행위                        | 상황                                                                                                                       |
| -------------------------------------------- | --------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| IT-ChemistryCacheSvc-캐시미스-LLM호출후READY | `process(reportId, rb, pb)` | `chemistry_cache.status=NULL` → LLM 1회 호출 → `chemistry_cache.status=READY`, `chemistry_reports.status=READY`            |
| IT-ChemistryCacheSvc-캐시히트유효-LLM미호출  | `process(reportId, rb, pb)` | `chemistry_cache.status=READY` + `createdAt` 유효(10일 전) → LLM 0회 호출, `chemistry_reports.status=READY`                |
| IT-ChemistryCacheSvc-캐시만료-LLM재호출      | `process(reportId, rb, pb)` | `chemistry_cache.status=READY` + `createdAt` 366일 전 → LLM 1회 호출, 캐시 `report` 갱신, `chemistry_reports.status=READY` |

---

### ChemistryCacheService — Duplication Defense (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheServiceTest.java)]

> `IntegrationTestSupport` 상속.
> `AnthropicLlmClient` `@MockitoBean` — `Thread.sleep(300)` 딜레이로 경쟁 구간 확보.
> `CountDownLatch(1)`로 두 스레드 동시 출발.

| Test ID                                                    | 행위                             | 상황                                                                                                                                            |
| ---------------------------------------------------------- | -------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| IT-ChemistryCacheSvc-동시요청3개-LLM단일호출\_3건모두READY | `process()` 3개 스레드 동시 실행 | 동일 버킷 조합 → LLM 1회 호출, `chemistry_reports` 3건 모두 `status=READY`. 구독자 2명으로 `CopyOnWriteArrayList` 다중 `add()` 안전성 검증 포함 |
| IT-ChemistryCacheSvc-다른버킷동시요청-LLM각각호출          | `process()` 2개 스레드 동시 실행 | 서로 다른 버킷 조합 → LLM 2회 호출                                                                                                              |

---

### ChemistryCacheService — @TransactionalEventListener AFTER_COMMIT 트리거 보장 (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheServiceTest.java)]

> `IntegrationTestSupport` 상속.
> `ChemistryService.issue()` 전체 흐름(코인 차감 → INSERT → 이벤트 발행)을 실제로 태워 검증.
> 커밋 전 `@Async` 트리거 문제(트랜잭션 커밋 전 별도 스레드가 먼저 실행되는 타이밍 버그)를
> `@TransactionalEventListener(AFTER_COMMIT)`으로 해소했음을 증명.
> `@Async` 완료까지 최대 10초 폴링 대기(100ms 간격).

| Test ID                                          | 행위                                            | 상황                                                                                                                       |
| ------------------------------------------------ | ----------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| IT-ChemistryCacheSvc-AFTER_COMMIT-커밋후처리실행 | `issue(requesterId, partnerId)` 정상 흐름       | 코인 차감 + INSERT 커밋 후 `@Async` `process()` 실행 → `chemistry_reports.status=READY`, LLM 1회 호출                      |
| IT-ChemistryCacheSvc-AFTER_COMMIT-롤백시미실행   | 코인 0개로 `issue()` 호출 → `deduct()`에서 예외 | 트랜잭션 롤백 → `AFTER_COMMIT` 이벤트 미발행 → `process()` 미실행 → LLM 0회 호출, `chemistry_reports` 행 자체가 생성 안 됨 |

---

### ChemistryReportProcessor — Recover 시그니처 수정 검증 (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheServiceTest.java)]

> `IntegrationTestSupport` 상속.
> `handle()`을 직접 호출 — `process()` 직접 호출은 `@Retryable`/`@Recover` 프록시를 우회하므로 이 버그를 검증할 수 없음.
> `@Async`라 즉시 반환하므로 폴링 헬퍼(`awaitUntil`)로 결과 대기.

| Test ID                                                | 행위                            | 상황                                                                                                                       |
| ------------------------------------------------------ | ------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| IT-ChemistryReportProcessor-recover-이벤트파라미터매칭 | `handle(event)` 3회 재시도 소진 | `recover(Exception e, ChemistryReportIssuedEvent event)` 정상 바인딩 → `chemistry_reports.status=ERROR`, LLM 3회 호출 확인 |

---

### ChemistryCacheService — Subscriber Timeout (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheServiceTest.java)]

> `IntegrationTestSupport` 상속.
> `@TestPropertySource(properties = "chemistry.subscriber-wait-timeout-seconds=2")`로 타임아웃을 2초로 단축.
> `SseService` `@MockitoSpyBean`으로 감시 — 뒤늦은 발행 성공 시 이미 이탈한 구독자에게 SSE가 안 나가는지까지 검증.

| Test ID                                                     | 행위                                          | 상황                                                                                                                     |
| ----------------------------------------------------------- | --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| IT-ChemistryCacheSvc-구독자타임아웃-ERROR전이및대기자맵정리 | 발행자 LLM 호출이 4초(타임아웃보다 길게) 소요 | 구독자는 1초 뒤 `chemistry_reports.status=ERROR`. 4초 뒤 발행자 성공해도 구독자는 SSE 미수신 (waitingMap 자가 정리 증명) |

---

### ChemistryCacheService — Concurrency Load Test (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheConcurrencyLoadTest.java)]

> `IntegrationTestSupport` 상속.
> Duplication Defense와 동일 기법(`Thread.sleep(300)` + `CountDownLatch`)을 `@RepeatedTest(30)`으로 반복.
> 동시성 문제는 결정적으로 재현되지 않으므로 단일 실행이 아닌 반복 시행으로 신뢰도 확보. 회차마다 독립된 버킷 조합 사용.

| Test ID                                       | 행위                                                       | 상황                                                                  |
| --------------------------------------------- | ---------------------------------------------------------- | --------------------------------------------------------------------- |
| IT-ChemistryCacheSvc-동시요청부하-LLM단일호출 | `process()` 5스레드 동시 실행 × 30회 반복(`@RepeatedTest`) | 매 회차 동일 버킷 조합 → LLM 정확히 1회 호출, 5건 모두 `status=READY` |

---

### ChemistryCacheRecoveryScheduler (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryCacheRecoverySchedulerTest.java)]

> `IntegrationTestSupport` 상속. 별도 파일 — 대상 빈이 `ChemistryCacheService`가 아닌 `ChemistryCacheRecoveryScheduler`.
> `EntityTestSupport.setField()`로 `updated_at`을 과거 시각으로 강제 세팅 — 정상 비즈니스 메서드 조합만으로는 스테일 상태를 재현할 수 없어 리플렉션 픽스처 사용.
> `recoverStaleGeneratingCaches()`를 직접 호출 (`@Scheduled` 트리거 대기 없음).

| Test ID                                                                  | 행위                             | 상황                                                                                                                           |
| ------------------------------------------------------------------------ | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| IT-ChemistryCacheRecoveryScheduler-스테일복구-READY전이및ERROR보고서교정 | `recoverStaleGeneratingCaches()` | `status=GENERATING` + `updated_at` 11분 전 → LLM 1회 재호출 → `READY`. 같은 `cacheId`의 `ERROR` 보고서도 조용히 `READY`로 교정 |
| IT-ChemistryCacheRecoveryScheduler-스테일복구-임계값미달                 | 동일                             | `updated_at` 10분 미만(방금 GENERATING 진입) → LLM 미호출, `status` 그대로 `GENERATING`                                        |
| IT-ChemistryCacheRecoveryScheduler-스테일복구-재시도중상태유지           | 동일                             | LLM 호출 도중(`claimForRetry()` 커밋 직후) 조회해도 `status=GENERATING` 유지, `NULL` 미노출                                    |

---

## 11. Notification 도메인

### NotificationService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/notification/service/NotificationServiceTest.java)]

| Test ID                              | 행위                             | 상황                                                                   |
| ------------------------------------ | -------------------------------- | ---------------------------------------------------------------------- |
| UT-NotificationSvc-동료알림전송-성공 | 동료 등록 알림 생성              | `ColleagueNotification` save 1회                                       |
| UT-NotificationSvc-동료알림삭제-성공 | 동료 관계 삭제 시 관련 알림 제거 | `findAllByColleague` 결과 `deleteAll` 1회 호출                         |
| UT-NotificationSvc-알림목록조회-성공 | 내 알림 목록 조회                | `NotificationListResponse` 반환, 항목 수 / `type` / `referenceId` 검증 |
| UT-NotificationSvc-알림삭제-성공     | 본인 알림 삭제                   | `delete` 1회 호출                                                      |
| UT-NotificationSvc-알림삭제-없는ID   | 존재하지 않는 알림               | `BusinessException(NOT_FOUND)`                                         |
| UT-NotificationSvc-알림삭제-권한없음 | 타인 알림 삭제 시도              | `BusinessException(FORBIDDEN)`, delete 미호출                          |

---

### NotificationV1Controller (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/notification/controller/NotificationV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. `MvcTestSupport` 상속.

| Test ID                             | 행위                                     | 상황                                                            |
| ----------------------------------- | ---------------------------------------- | --------------------------------------------------------------- |
| ST-NotificationCtrl-목록조회-성공   | 인증된 사용자 GET /notifications         | 200 + 응답 바디 `notificationId`/`type`/`referenceId` 필드 검증 |
| ST-NotificationCtrl-목록조회-미인증 | 미인증 GET /notifications                | 401                                                             |
| ST-NotificationCtrl-삭제-성공       | 인증된 사용자 DELETE /notifications/{id} | 200                                                             |
| ST-NotificationCtrl-삭제-미인증     | 미인증 DELETE /notifications/{id}        | 401                                                             |

---

## 12. Coin 도메인

### CoinV1Controller (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/coin/controller/CoinV1ControllerTest.java)]

> `@WebMvcTest(CoinV1Controller.class)` 슬라이스 테스트. `MvcTestSupport` 상속.
> `CoinService`는 `@MockitoBean`으로 대체.
> 비즈니스 로직 분기는 `CoinServiceTest`(UT)에서 전담 검증. ST는 인증 분기만 다룬다.

| Test ID                     | 행위                             | 상황                                      |
| --------------------------- | -------------------------------- | ----------------------------------------- |
| ST-CoinCtrl-잔액조회-성공   | 인증된 사용자 GET /coins         | 200 + 응답 바디 `coins`/`nextCoinAt` 검증 |
| ST-CoinCtrl-잔액조회-미인증 | 미인증 GET /coins                | 401                                       |
| ST-CoinCtrl-이력조회-성공   | 인증된 사용자 GET /coins/history | 200 + 응답 바디 `history` 배열 검증       |
| ST-CoinCtrl-이력조회-미인증 | 미인증 GET /coins/history        | 401                                       |

---

### CoinService (UT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/coin/service/CoinServiceTest.java)]

| Test ID                                | 행위                              | 상황                                                      |
| -------------------------------------- | --------------------------------- | --------------------------------------------------------- |
| UT-CoinSvc-가입보너스-성공             | recordSignupBonus 호출            | coin_transactions에 SIGNUP +3 기록, balanceAfter=3        |
| UT-CoinSvc-잔액조회-충전없음           | nextCoinAt이 미래 시각            | 충전 미발생, 기존 coins 그대로 반환                       |
| UT-CoinSvc-잔액조회-충전대기없음       | nextCoinAt = null                 | 충전 로직 스킵, 기존 coins 그대로 반환                    |
| UT-CoinSvc-잔액조회-온디맨드충전-1`    | nextCoinAt이 1일 전 도래, coins=1 | coins=2로 충전, nextCoinAt이 1일 뒤로 이동(시분초 보존)   |
| UT-CoinSvc-잔액조회-온디맨드충전-캡적` | nextCoinAt이 5일 전 도래, coins=1 | chargeable=5지만 캡 적용되어 coins=3, nextCoinAt=null     |
| UT-CoinSvc-차감-3미만최초설정          | coins=3 → 차감 후 coins=2         | nextCoinAt이 null에서 now+24h로 세팅                      |
| UT-CoinSvc-차감-타이머기설정           | coins=2, nextCoinAt 이미 세팅됨   | 추가 차감해도 기존 nextCoinAt 값 변경 없음                |
| UT-CoinSvc-차감-3이상유지시미세팅      | coins=5(이벤트 보유) → 차감 후 4  | 여전히 3 이상이므로 nextCoinAt 세팅 안 됨                 |
| UT-CoinSvc-차감-잔액부족               | coins=0                           | `BusinessException(INSUFFICIENT_COINS)`, 차감/이력 미발생 |
| UT-CoinSvc-이력조회-성공               | 이력 5건 중 size=3 요청           | 3건 반환, hasNext=true, nextCursor=4번째 항목 id          |
| UT-CoinSvc-이력조회-마지막페이지       | 남은 이력이 size보다 적음         | hasNext=false, nextCursor=null                            |

---

### CoinTransactionRepository (ST)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/coin/repository/CoinTransactionRepositoryTest.java)]

| Test ID                         | 행위               | 상황                                                |
| ------------------------------- | ------------------ | --------------------------------------------------- |
| ST-CoinTxRepo-사용자별삭제-성공 | `deleteByUserId()` | 해당 유저 트랜잭션 전체 삭제, 타 유저 데이터 미영향 |

---

## 13. 스케줄러 (도메인 비결합)

> `@Scheduled`가 유일한 진입점인 컴포넌트 중, 특정 도메인의 내부 구현(엔티티 상태·이벤트·헬퍼)에
> 깊게 결합되지 않고 리포지토리를 얕게 호출하는 수준의 범용 운영 작업만 여기 속한다.
> 반대로 도메인 내부 로직에 깊게 결합된 스케줄러(예: `ChemistryCacheRecoveryScheduler`)는
> 트리거 방식과 무관하게 해당 도메인 섹션에 남는다.

### ExpiredDataCleanupScheduler (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/batch/ExpiredDataCleanupSchedulerTest.java)]

> `IntegrationTestSupport` 상속. Spring Batch 미사용 — 순수 `@Scheduled` 메서드(`cleanup()`) 직접 호출 검증.

| Test ID                                                      | 행위        | 상황                                                                                                |
| ------------------------------------------------------------ | ----------- | --------------------------------------------------------------------------------------------------- |
| IT-ExpiredDataCleanupScheduler-만료데이터삭제-유효데이터보존 | `cleanup()` | 만료된 `peer_codes`/`assessment_tokens` 행과 유효한 행을 함께 시드 → 만료 행만 삭제, 유효 행은 보존 |

## 14. 통합 테스트 시나리오

### UserWithdraw (IT)

[[Test Code](../../backend/src/test/java/com/mycpt/backend/domain/user/service/UserWithdrawIntegrationTest.java)]

> `IntegrationTestSupport` 상속. `KakaoUnlinkClient`만 `@MockitoBean`, 나머지 전부 실제 DB.

| Test ID                                         | 행위                                                      | 상황                                                                                                             |
| ----------------------------------------------- | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| IT-UserWithdraw-전체흐름-성공                   | 검사이력+코인이력+동료관계+케미보고서 보유 상태로 탈퇴    | `tests`~`colleagues` 전부 삭제, `chemistry_reports`는 유지, `users` 행은 존재하되 `kakaoId=null`·`nickname` 유지 |
| IT-UserWithdraw-상대방동료목록조회-자동제외     | A 탈퇴 후 B가 `GET /colleagues` 조회                      | B의 목록에 A가 더 이상 없음 — 별도 필터링 코드 없이 관계 삭제만으로 처리되는지가 핵심                            |
| IT-UserWithdraw-상대방동료상세조회-FORBIDDEN    | A 탈퇴 후 B가 `GET /colleagues/{A}` 조회                  | A row는 존재(익명화)하므로 NOT_FOUND 아닌 FORBIDDEN                                                              |
| IT-UserWithdraw-상대방케미보고서조회-닉네임유지 | 기존 보고서 존재 상태에서 A 탈퇴 후 B가 상세 조회         | `requesterNickname`에 A의 원래 닉네임 그대로 반환                                                                |
| IT-UserWithdraw-상대방케미발행시도-FORBIDDEN    | A 탈퇴 후 B가 A를 대상으로 `POST /chemistry-reports` 시도 | `existsByPair()`가 false → FORBIDDEN                                                                             |
| IT-UserWithdraw-카카오unlink실패-전체롤백       | `kakaoUnlinkClient.unlink()`가 예외 던짐                  | 트랜잭션 롤백 — `tests` 등 다른 테이블 삭제 안 됨 확인                                                           |
