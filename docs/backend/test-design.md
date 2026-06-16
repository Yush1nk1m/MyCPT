# MyCPT 테스트 설계 문서

**문서 버전**: v0.4
**작성일**: '26.06.13.
**작성자**: 김유신

---

## 변경 이력

| 버전 | 변경 내용                                                                                                                                                                                                                                                          | 날짜       |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------- |
| v0.1 | 초안 작성. 레이어별 전략, Auth/User 도메인 테스트 케이스 ID 체계 수립.                                                                                                                                                                                             | '26.05.26. |
| v0.2 | Result/Assessment 도메인 테스트 케이스 추가. JpaTestSupport 기반 슬라이스 테스트 전략 확정.                                                                                                                                                                        | '26.06.09. |
| v0.3 | 예외 처리 체계 통합 반영 (BusinessException/ErrorCode). ScoringService 경계값 케이스 보강.                                                                                                                                                                         | '26.06.13. |
| v0.4 | `DiscResultRepository` → `DiscTestRepository` 이름 변경. Test ID `ST-DiscResultRepo-*` → `ST-DiscTestRepo-*` 전면 변경. `ResultService` 저장 행위 설명 `disc_results` → `disc_tests` 수정. `AssessmentService` 평정 제출 상황 설명 `DiscResult` → `DiscTest` 수정. | '26.06.15. |

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
- [13. 통합 테스트 시나리오](#13-통합-테스트-시나리오)

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

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/auth/service/CustomOAuth2UserServiceTest.java)]

> `loadUser()`는 내부에서 `super.loadUser()`(카카오 HTTP 호출)를 수행하므로
> 비즈니스 로직이 분리된 `findOrCreateUser()`를 직접 테스트한다.

| Test ID                                          | 행위                                       | 상황                                                                                          |
| ------------------------------------------------ | ------------------------------------------ | --------------------------------------------------------------------------------------------- |
| UT-OAuth2UserSvc-회원가입-성공                   | 신규 회원 첫 로그인 시 User 저장           | `findByKakaoId()` 결과 없음. `save()` 1회 호출, kakaoId/nickname/profileImageUrl/coins=3 검증 |
| UT-OAuth2UserSvc-기존회원로그인-성공             | 기존 회원 재로그인 시 조회만 수행          | `findByKakaoId()` 결과 있음. `save()` 0회 호출 검증                                           |
| UT-OAuth2UserSvc-properties누락시기본값적용-성공 | 카카오 응답 properties 누락 시 기본값 처리 | nickname="닉네임", profileImageUrl=null 검증                                                  |

### AuthV1Controller (ST)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/auth/controller/AuthV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. `MvcTestSupport` 상속.

| Test ID                       | 행위                       | 상황                      |
| ----------------------------- | -------------------------- | ------------------------- |
| ST-AuthCtrl-내정보조회-성공   | 인증된 사용자 GET /auth/me | 200 + 응답 바디 필드 검증 |
| ST-AuthCtrl-내정보조회-미인증 | 미인증 GET /auth/me        | 401                       |

---

## 5. User 도메인

### UserV1Controller (ST)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/user/controller/UserV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. `MvcTestSupport` 상속.

| Test ID                       | 행위                          | 상황                          |
| ----------------------------- | ----------------------------- | ----------------------------- |
| ST-UserCtrl-프로필수정-성공   | 인증된 사용자 PATCH /users/me | 200 + 응답 바디 3개 필드 검증 |
| ST-UserCtrl-프로필수정-미인증 | 미인증 PATCH /users/me        | 401                           |

---

## 6. Result 도메인

### ScoringService (UT)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/service/ScoringServiceTest.java)]

| Test ID                                       | 행위                            | 상황                                                              |
| --------------------------------------------- | ------------------------------- | ----------------------------------------------------------------- |
| UT-ScoringService-버킷정규화-성공             | 정상 원점수 입력 시 버킷값 반환 | D=32, I=10, S=-4, C=-14 → 버킷 3,2,2,1 검증                       |
| UT-ScoringService-버킷정규화-최솟값최댓값혼합 | 최솟값/최댓값 경계 혼합 입력    | D=-24(버킷1), C=48(버킷3) 검증                                    |
| UT-ScoringService-합계검증-실패               | D+I+S+C ≠ 24                    | `BusinessException(INVALID_SCORE)`, 메시지에 "합계는 24여야" 포함 |
| UT-ScoringService-범위초과-상한               | 개별 원점수 48 초과             | `BusinessException(INVALID_SCORE)`, 메시지에 "49" 포함            |
| UT-ScoringService-범위초과-하한               | 개별 원점수 -24 미만            | `BusinessException(INVALID_SCORE)`, 메시지에 "-25" 포함           |
| UT-ScoringService-toBucket-경계값             | 버킷 전환점 전수 검증           | 3구간 하한/상한 6개 케이스 (-24→1, -5→1, -4→2, 11→2, 12→3, 48→3)  |

### CacheService (UT)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/service/CacheServiceTest.java)]

| Test ID                                          | 행위                               | 상황                                                                        |
| ------------------------------------------------ | ---------------------------------- | --------------------------------------------------------------------------- |
| UT-CacheService-보고서생성-행누락예외            | 초기화 스크립트 미실행으로 행 없음 | `findById` empty → `IllegalStateException`, LLM 미호출                      |
| UT-CacheService-보고서생성-미생성                | 사전 삽입 행이지만 report=NULL     | LLM 1회 호출 → `save` 1회 → 보고서 반환                                     |
| UT-CacheService-보고서생성-캐시HIT유효           | DB에 유효한 캐시 존재              | `created_at` 10일 전, ttl=365일 → LLM 미호출, `save` 없음, 기존 보고서 반환 |
| UT-CacheService-보고서생성-캐시HIT유효경계값분석 | TTL 당일 만료 경계 검증            | `created_at` 364일 전, ttl=365일 → 유효 처리, LLM 미호출                    |
| UT-CacheService-보고서생성-캐시HIT만료           | DB에 만료된 캐시 존재              | `created_at` 366일 전, ttl=365일 → LLM 1회 호출, `save` 1회, 새 보고서 반환 |

### ResultService (UT)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/service/ResultServiceTest.java)]

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

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/repository/DiscTestRepositoryTest.java)]

> `@DataJpaTest` + Testcontainers MySQL (`JpaTestSupport` 상속).
> `@Sql("/sql/disc_cache_seed.sql")` 로 disc_cache 복합 FK 제약 해소.
> `@DataJpaTest` 기본 동작인 트랜잭션 롤백으로 테스트 간 데이터 격리.

| Test ID                                   | 행위                   | 상황                                                                     |
| ----------------------------------------- | ---------------------- | ------------------------------------------------------------------------ |
| ST-DiscTestRepo-커서페이지네이션-성공     | cursor=null 조회       | 최신순 id DESC 정렬, 저장한 3개 전체 반환 검증                           |
| ST-DiscTestRepo-커서페이지네이션-커서검증 | cursor=N으로 조회      | id < N인 결과만 반환 검증                                                |
| ST-DiscTestRepo-raterType필터-SELF        | raterType=SELF 필터    | SELF 결과만 반환, OTHER 미포함 검증                                      |
| ST-DiscTestRepo-raterType필터-null        | raterType=null         | SELF/OTHER 모두 반환 검증                                                |
| ST-DiscTestRepo-상세조회-JoinFetch        | findByTestIdWithDetail | test 연관관계 LAZY 정상 로드 (`LazyInitializationException` 미발생) 검증 |
| ST-DiscTestRepo-상세조회-존재하지않는ID   | 없는 testId 조회       | `Optional.empty()` 반환 검증                                             |

---

## 7. Assessment 도메인

### AssessmentService (UT)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/assessment/service/AssessmentServiceTest.java)]

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

## 8. Statistics 도메인

### StatisticsService (UT)

#### comparison()

| Test ID                                | 행위                                    | 상황                                            |
| -------------------------------------- | --------------------------------------- | ----------------------------------------------- |
| `UT-StatisticsSvc-비교조회-검사없음`   | 본인 SELF 검사 이력 없음                | `my.buckets = null`, `average` 집계는 계속 진행 |
| `UT-StatisticsSvc-비교조회-생년미입력` | `birthYear = null`                      | `average = null` 즉시 반환                      |
| `UT-StatisticsSvc-비교조회-성별미입력` | `gender = null`                         | `average = null` 즉시 반환                      |
| `UT-StatisticsSvc-비교조회-성별N`      | `gender = N`                            | `average = null` 즉시 반환                      |
| `UT-StatisticsSvc-비교조회-샘플없음`   | 나이대/성별 집계 결과 `sampleCount = 0` | `average = null` 반환                           |
| `UT-StatisticsSvc-비교조회-성공`       | 모든 조건 충족                          | `average.ageGroupLabel`, `sampleCount > 0` 검증 |

#### trend()

| Test ID                              | 행위                          | 상황                                                        |
| ------------------------------------ | ----------------------------- | ----------------------------------------------------------- |
| `UT-StatisticsSvc-추이조회-결과없음` | `days` 기간 내 SELF 검사 없음 | `summary.count = 0`, `summary.average = null`, `trend = []` |
| `UT-StatisticsSvc-추이조회-성공`     | 검사 이력 있음                | `entries` 수 일치, `summary.average` 산술 평균 정확성       |

---

## 9. Colleague 도메인

### PeerCodeService (UT)

| Test ID                          | 행위                          | 상황                                            |
| -------------------------------- | ----------------------------- | ----------------------------------------------- |
| `UT-PeerCodeSvc-코드조회-행없음` | 코드 행이 없을 때 getOrCreate | 신규 `PeerCode` save 1회, 코드/만료일 반환      |
| `UT-PeerCodeSvc-코드조회-유효`   | 기존 유효 코드 존재           | save 미호출, 기존 코드 그대로 반환              |
| `UT-PeerCodeSvc-코드조회-만료`   | 기존 만료 코드 존재           | `refresh()` 후 save 1회, 새 코드 반환           |
| `UT-PeerCodeSvc-코드갱신-성공`   | refresh() 호출                | save 1회, 새 코드 반환                          |
| `UT-PeerCodeSvc-코드갱신-행없음` | 코드 행 없을 때 refresh       | 신규 생성 후 refresh, save 1회 (방어 로직 검증) |

### ColleagueService (UT)

| Test ID                                   | 행위                           | 상황                                                                            |
| ----------------------------------------- | ------------------------------ | ------------------------------------------------------------------------------- |
| `UT-ColleagueSvc-초대정보조회-성공`       | 유효한 코드로 초대자 정보 조회 | `InviteInfoResponse` 반환 (inviter 닉네임/이미지 포함)                          |
| `UT-ColleagueSvc-초대정보조회-코드없음`   | 존재하지 않는 코드             | `BusinessException(NOT_FOUND)`                                                  |
| `UT-ColleagueSvc-초대정보조회-만료코드`   | `isExpired()` = true           | `BusinessException(EXPIRED_CODE)`                                               |
| `UT-ColleagueSvc-초대정보조회-자기초대`   | 초대자 == 요청자               | `BusinessException(SELF_INVITE)`                                                |
| `UT-ColleagueSvc-동료등록-성공`           | 유효한 코드로 동료 등록        | `Colleague` save 1회, `sendColleagueNotification` 1회, `ColleagueResponse` 반환 |
| `UT-ColleagueSvc-동료등록-코드없음`       | 존재하지 않는 코드             | `BusinessException(NOT_FOUND)`                                                  |
| `UT-ColleagueSvc-동료등록-만료코드`       | `isExpired()` = true           | `BusinessException(EXPIRED_CODE)`                                               |
| `UT-ColleagueSvc-동료등록-자기초대`       | 초대자 == 요청자               | `BusinessException(SELF_INVITE)`                                                |
| `UT-ColleagueSvc-동료등록-이미동료`       | `existsByPair()` = true        | `BusinessException(ALREADY_COLLEAGUE)`, save 미호출                             |
| `UT-ColleagueSvc-동료목록조회-성공`       | 동료 목록 조회                 | `ColleagueListResponse` 반환, 상대방 정보 포함                                  |
| `UT-ColleagueSvc-동료프로필조회-성공`     | 동료 관계인 상대 조회          | `ColleagueResponse` 반환                                                        |
| `UT-ColleagueSvc-동료프로필조회-동료아님` | 동료 관계 없음                 | `BusinessException(FORBIDDEN)`                                                  |
| `UT-ColleagueSvc-동료삭제-성공`           | 동료 관계 삭제                 | `delete` 1회 호출                                                               |
| `UT-ColleagueSvc-동료삭제-동료아님`       | 동료 관계 없음                 | `BusinessException(FORBIDDEN)`, delete 미호출                                   |

---

## 10. Chemistry 도메인

_4주차 구현 시 작성_

---

## 11. Notification 도메인

### NotificationService (UT)

| Test ID                                | 행위                | 상황                                          |
| -------------------------------------- | ------------------- | --------------------------------------------- |
| `UT-NotificationSvc-동료알림전송-성공` | 동료 등록 알림 생성 | `ColleagueNotification` save 1회              |
| `UT-NotificationSvc-알림목록조회-성공` | 내 알림 목록 조회   | `NotificationListResponse` 반환, 항목 수 일치 |
| `UT-NotificationSvc-알림삭제-성공`     | 본인 알림 삭제      | `delete` 1회 호출                             |
| `UT-NotificationSvc-알림삭제-없는ID`   | 존재하지 않는 알림  | `BusinessException(NOT_FOUND)`                |
| `UT-NotificationSvc-알림삭제-권한없음` | 타인 알림 삭제 시도 | `BusinessException(FORBIDDEN)`, delete 미호출 |

---

## 12. Coin 도메인

_4주차 구현 시 작성_

---

## 13. 통합 테스트 시나리오

_4주차 QA 단계 작성_
