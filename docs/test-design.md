# MyCPT 테스트 설계 문서

**문서 버전**: v0.1
**작성일**: '26.05.29.
**작성자**: 김유신
**연관 문서**: api-design.md v0.3 / architecture-design.md v0.1

---

## 목차

1. [테스트 전략](#1-테스트-전략)
2. [테스트 ID 체계](#2-테스트-id-체계)
3. [도구 및 환경](#3-도구-및-환경)
4. [Auth 도메인](#4-auth-도메인)
5. [User 도메인](#5-user-도메인)
6. [Result 도메인](#6-result-도메인)
7. [Assessment 도메인](#7-assessment-도메인)
8. [Statistics 도메인](#8-statistics-도메인)
9. [Colleague 도메인](#9-colleague-도메인)
10. [Chemistry 도메인](#10-chemistry-도메인)
11. [Notification 도메인](#11-notification-도메인)
12. [Coin 도메인](#12-coin-도메인)
13. [통합 테스트 시나리오](#13-통합-테스트-시나리오)

---

## 1. 테스트 전략

### 레이어별 전략

| 레이어                     | 도구                               | 목적                                                    | 작성 시점         |
| -------------------------- | ---------------------------------- | ------------------------------------------------------- | ----------------- |
| Service 단위 테스트        | JUnit 5 + Mockito                  | 핵심 비즈니스 로직 검증. 외부 의존성 Mock 처리          | 기능 구현과 동시  |
| Controller 슬라이스 테스트 | @WebMvcTest + Spring Security Test | 인증 분기, 요청/응답 스펙 검증. Security 필터 체인 포함 | 기능 구현과 동시  |
| 통합 테스트                | @SpringBootTest + Testcontainers   | 전체 흐름 검증. 실제 DB/Redis 연동                      | 4주차 QA 단계     |
| 수동 테스트                | Swagger UI (`/swagger-ui`)         | OAuth2 플로우, SSE 연결 등 자동화 불가 영역             | 기능 구현 후 즉시 |

### 자동화 제외 영역

아래 항목은 자동화 테스트 대신 Swagger UI 수동 테스트로 커버한다.

- 카카오 OAuth2 전체 플로우 (리다이렉트 → 콜백 → 세션 발급)
- SSE 연결 및 실시간 푸시 수신
- 카카오 연결 해제 (회원 탈퇴)

---

## 2. 테스트 ID 체계

### 형식

```
{도메인}{레이어}-{순번}

예시:
AUS-01 → Auth 도메인, Unit Service 테스트, 1번
AUC-01 → Auth 도메인, Unit Controller (슬라이스) 테스트, 1번
AIT-01 → Auth 도메인, Integration Test, 1번
```

### 도메인 코드

| 코드 | 도메인       |
| ---- | ------------ |
| A    | Auth         |
| U    | User         |
| R    | Result       |
| AS   | Assessment   |
| ST   | Statistics   |
| CL   | Colleague    |
| CH   | Chemistry    |
| N    | Notification |
| CO   | Coin         |

### 레이어 코드

| 코드 | 레이어                     |
| ---- | -------------------------- |
| US   | Unit Service               |
| UC   | Unit Controller (슬라이스) |
| IT   | Integration Test           |

---

## 3. 도구 및 환경

| 항목              | 내용                                                 |
| ----------------- | ---------------------------------------------------- |
| 테스트 프레임워크 | JUnit 5                                              |
| Mock 라이브러리   | Mockito                                              |
| 슬라이스 테스트   | @WebMvcTest + spring-security-test                   |
| OAuth2 인증 주입  | `SecurityMockMvcRequestPostProcessors.oauth2Login()` |
| 통합 테스트 DB    | Testcontainers (MySQL)                               |
| 통합 테스트 Redis | Testcontainers (Redis)                               |
| 커버리지 측정     | JaCoCo (4주차 QA 단계 적용)                          |
| 수동 테스트       | Swagger UI (`/swagger-ui`)                           |

---

## 4. Auth 도메인

### CustomOAuth2UserService (AUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/auth/service/CustomOAuth2UserServiceTest.java)]

> `loadUser()`는 내부에서 `super.loadUser()`(카카오 HTTP 호출)를 수행하므로
> 비즈니스 로직이 분리된 `findOrCreateUser()`를 직접 테스트한다.
> 테스트 종류: Mockito 단위 테스트.

| Test ID | Test Name                           | Description                                                                   |
| ------- | ----------------------------------- | ----------------------------------------------------------------------------- |
| AUS-01  | 신규 회원 첫 로그인 시 가입 처리    | `save()` 1회 호출 검증. kakaoId, nickname, profileImageUrl, coins=3 필드 검증 |
| AUS-02  | 기존 회원 재로그인 시 save() 미호출 | `findByKakaoId()` 조회만 수행. `save()` 0회 호출 검증                         |
| AUS-03  | properties null 시 기본값 처리      | nickname="카카오사용자", profileImageUrl=null 반환 검증                       |

### AuthV1Controller (AUC)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/auth/controller/AuthV1ControllerTest.java)]

> `@WebMvcTest` 슬라이스 테스트. Spring Security 필터 체인 포함.
> `oauth2Login()`으로 `UserPrincipal`을 실제 주입해 인증 상태를 구성한다.
> `@WithMockUser` 대신 `oauth2Login()`을 사용해 `UserPrincipal` 타입 안전성을 보장한다.

| Test ID | Test Name                         | Description                                                                                |
| ------- | --------------------------------- | ------------------------------------------------------------------------------------------ |
| AUC-01  | 인증된 사용자 /auth/me 200 응답   | `oauth2Login()` 인증 상태, 상태코드 200 검증                                               |
| AUC-02  | 미인증 사용자 /auth/me 401 응답   | 인증 없이 호출. 401 + `{"code":"UNAUTHORIZED"}` 응답 바디 검증                             |
| AUC-03  | /auth/me 응답 바디 전체 필드 검증 | userId, nickname, profileImageUrl, coins, nextCoinAt, birthYear, gender 7개 필드 존재 검증 |

---

## 5. User 도메인

### UserService (UUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/user/service/UserServiceTest.java)]

_3주차 구현 시 작성_

| Test ID | Test Name                   | Description                  |
| ------- | --------------------------- | ---------------------------- |
| UUS-01  | 닉네임 수정 성공            | 1~30자 닉네임 정상 수정 검증 |
| UUS-02  | 닉네임 30자 초과 시 예외    | 31자 입력 시 400 에러 검증   |
| UUS-03  | 프로필 이미지 URL 수정 성공 | S3 Full URL 저장 검증        |
| UUS-04  | 회원 탈퇴 시 세션 무효화    | 탈퇴 후 세션 삭제 검증       |

### UserV1Controller (UUC)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/user/controller/UserV1ControllerTest.java)]

_3주차 구현 시 작성_

| Test ID | Test Name                     | Description                                 |
| ------- | ----------------------------- | ------------------------------------------- |
| UUC-01  | 미인증 사용자 프로필 수정 401 | 인증 없이 PATCH /users/me 호출 시 401 검증  |
| UUC-02  | 미인증 사용자 회원 탈퇴 401   | 인증 없이 DELETE /users/me 호출 시 401 검증 |

---

## 6. Result 도메인

### ScoringService (RUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/service/ScoringServiceTest.java)]

_2주차 구현 시 작성_

| Test ID | Test Name                      | Description                                      |
| ------- | ------------------------------ | ------------------------------------------------ |
| RUS-01  | DISC 원점수 정상 채점          | 24문항 응답 기반 D/I/S/C 원점수 산출 정확성 검증 |
| RUS-02  | 버킷값 정규화 정상 동작        | 원점수 → 1~9 버킷값 변환 검증                    |
| RUS-03  | most와 least 동일 선택 시 예외 | 동일 optionId 입력 시 400 에러 검증              |
| RUS-04  | 문항 수 24개 미달 시 예외      | 23개 이하 응답 시 400 에러 검증                  |

### CacheService (RUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/service/CacheServiceTest.java)]

_2주차 구현 시 작성_

| Test ID | Test Name                     | Description                                         |
| ------- | ----------------------------- | --------------------------------------------------- |
| RUS-05  | 캐시 히트 시 LLM 미호출       | disc_cache 존재 시 LlmService 호출 0회 검증         |
| RUS-06  | 캐시 미스 시 LLM 호출 후 저장 | disc_cache 없을 때 LlmService 1회 호출 및 저장 검증 |

### ResultV1Controller (RUC)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/result/controller/ResultV1ControllerTest.java)]

_2주차 구현 시 작성_

| Test ID | Test Name               | Description                                |
| ------- | ----------------------- | ------------------------------------------ |
| RUC-01  | 비회원 채점 요청 200    | 미인증 POST /results/score 정상 응답 검증  |
| RUC-02  | 비회원 결과 저장 401    | 미인증 POST /results 호출 시 401 검증      |
| RUC-03  | 타인 결과 상세 조회 403 | 본인 소유가 아닌 resultId 조회 시 403 검증 |
| RUC-04  | 존재하지 않는 결과 404  | 없는 resultId 조회 시 404 검증             |

---

## 7. Assessment 도메인

### AssessmentService (ASUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/assessment/service/AssessmentServiceTest.java)]

_2주차 구현 시 작성_

| Test ID | Test Name                            | Description                                       |
| ------- | ------------------------------------ | ------------------------------------------------- |
| ASUS-01 | 일회용 토큰 생성 정상 동작           | 32자리 토큰, 만료 7일 검증                        |
| ASUS-02 | 사용된 토큰 접속 차단                | used=TRUE 토큰 접속 시 TOKEN_USED 에러 검증       |
| ASUS-03 | 만료된 토큰 접속 차단                | expires_at 초과 토큰 시 EXPIRED_CODE 에러 검증    |
| ASUS-04 | 평정 제출 시 토큰 used 처리          | submit 후 used=TRUE 전환 검증                     |
| ASUS-05 | 평정 제출 시 tests/disc_results 저장 | 단일 트랜잭션 내 tests + disc_results INSERT 검증 |

---

## 8. Statistics 도메인

### StatisticsService (STUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/statistics/service/StatisticsServiceTest.java)]

_3주차 구현 시 작성_

| Test ID | Test Name                     | Description                                |
| ------- | ----------------------------- | ------------------------------------------ |
| STUS-01 | 나이대/성별 평균 정상 산출    | 동일 나이대/성별 그룹 평균값 계산 검증     |
| STUS-02 | 생년 미입력 시 average null   | birthYear=null 회원 average=null 반환 검증 |
| STUS-03 | 검사 이력 없을 시 latest null | 결과 없는 회원 latest=null 반환 검증       |
| STUS-04 | 변화 추이 기간 필터 정상 동작 | days 파라미터 기준 날짜 범위 필터 검증     |

---

## 9. Colleague 도메인

### PeerCodeService (CLUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/colleague/service/PeerCodeServiceTest.java)]

_3주차 구현 시 작성_

| Test ID | Test Name                   | Description                        |
| ------- | --------------------------- | ---------------------------------- |
| CLUS-01 | 동료 코드 신규 생성         | 대문자+숫자 8자리, 만료 7일 검증   |
| CLUS-02 | 만료 코드 온디맨드 리프레시 | 만료 코드 조회 시 자동 재생성 검증 |

### ColleagueService (CLUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/colleague/service/ColleagueServiceTest.java)]

_3주차 구현 시 작성_

| Test ID | Test Name                          | Description                                         |
| ------- | ---------------------------------- | --------------------------------------------------- |
| CLUS-03 | 본인 초대 코드 등록 시 SELF_INVITE | 자기 자신 코드 사용 시 에러 검증                    |
| CLUS-04 | 이미 동료인 경우 ALREADY_COLLEAGUE | 중복 등록 시도 시 에러 검증                         |
| CLUS-05 | 동료 등록 시 양방향 목록 조회      | user_a_id < user_b_id 규칙 기반 UNION ALL 조회 검증 |
| CLUS-06 | 동료 등록 완료 시 알림 전송        | NotificationService 1회 호출 검증                   |

---

## 10. Chemistry 도메인

### ChemistryService (CHUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/chemistry/service/ChemistryServiceTest.java)]

_4주차 구현 시 작성_

| Test ID | Test Name                       | Description                                          |
| ------- | ------------------------------- | ---------------------------------------------------- |
| CHUS-01 | 코인 부족 시 INSUFFICIENT_COINS | coins=0 상태에서 발행 요청 시 422 에러 검증          |
| CHUS-02 | 발행 요청 시 202 즉시 반환      | @Async 호출 전 202 반환 검증                         |
| CHUS-03 | 동료 관계 없는 상대 발행 차단   | 동료 미등록 상태 발행 시도 시 에러 검증              |
| CHUS-04 | LLM 호출 완료 후 SSE 푸시       | ChemistryLlmService 완료 후 SseService 1회 호출 검증 |

---

## 11. Notification 도메인

### NotificationService (NUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/notification/service/NotificationServiceTest.java)]

_3주차 구현 시 작성_

| Test ID | Test Name                   | Description                     |
| ------- | --------------------------- | ------------------------------- |
| NUS-01  | 알림 생성 정상 동작         | notifications INSERT 검증       |
| NUS-02  | 알림 삭제 시 본인 소유 검증 | 타인 알림 삭제 시도 시 403 검증 |

---

## 12. Coin 도메인

### CoinService (COUS)

[[Test Code](../backend/src/test/java/com/mycpt/backend/domain/coin/service/CoinServiceTest.java)]

_4주차 구현 시 작성_

| Test ID | Test Name                           | Description                                  |
| ------- | ----------------------------------- | -------------------------------------------- |
| COUS-01 | 충전 조건 충족 시 코인 충전         | next_coin_at <= NOW() 시 coins +1 검증       |
| COUS-02 | 충전 조건 미충족 시 미충전          | next_coin_at > NOW() 시 coins 변화 없음 검증 |
| COUS-03 | 만충 상태에서 충전 미발생           | coins=3 상태에서 충전 호출 시 변화 없음 검증 |
| COUS-04 | 코인 차감 시 coin_transactions 적재 | 케미 발행 차감 후 로그 INSERT 검증           |

---

## 13. 통합 테스트 시나리오

_4주차 QA 단계에 Testcontainers 기반으로 작성_

| Test ID | 시나리오                          | Description                                                   |
| ------- | --------------------------------- | ------------------------------------------------------------- |
| AIT-01  | 비회원 검사 응시 → 채점 전체 흐름 | 문항 조회 → POST /results/score → 원점수/버킷값/보고서 반환   |
| AIT-02  | 비회원 → 회원 결과 저장 연계      | sessionStorage 원점수 → 로그인 → POST /results → DB 저장      |
| AIT-03  | 타인 평정 전체 흐름               | 링크 생성 → 토큰 접속 → 응시 → 제출 → tests/disc_results 저장 |
| AIT-04  | 케미 보고서 발행 전체 흐름        | 코인 차감 → @Async LLM 호출 → SSE 푸시 → 알림 저장            |
| AIT-05  | 동료 등록 전체 흐름               | 코드 생성 → 유효성 확인 → 등록 → 알림 전송                    |

---

_본 문서는 기능 구현 진행에 따라 지속적으로 업데이트됩니다._
_테스트 코드 경로는 각 섹션의 링크를 참조한다._
