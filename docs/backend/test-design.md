# MyCPT 테스트 설계 문서

**문서 버전**: v0.2  
**작성일**: '26.05.28.  
**작성자**: 김유신

---

## 목차

- [MyCPT 테스트 설계 문서](#mycpt-테스트-설계-문서)
  - [목차](#목차)
  - [1. 테스트 전략](#1-테스트-전략)
    - [핵심 원칙](#핵심-원칙)
    - [테스트 종류 선택 기준](#테스트-종류-선택-기준)
    - [레이어별 도구](#레이어별-도구)
    - [자동화 제외 영역 (Swagger UI 수동 테스트)](#자동화-제외-영역-swagger-ui-수동-테스트)
  - [2. 테스트 ID 체계](#2-테스트-id-체계)
    - [형식](#형식)
    - [테스트 종류 코드](#테스트-종류-코드)
    - [규칙](#규칙)
  - [3. 도구 및 환경](#3-도구-및-환경)
  - [4. Auth 도메인](#4-auth-도메인)
    - [CustomOAuth2UserService (UT)](#customoauth2userservice-ut)
    - [AuthV1Controller (ST)](#authv1controller-st)
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

### 테스트 종류 선택 기준

| 질문                                       | 테스트 종류 |
| ------------------------------------------ | ----------- |
| HTTP 계약(상태코드, 인증 분기)이 핵심인가? | ST          |
| 순수 비즈니스 로직인가?                    | UT          |
| DB/Redis 실제 연동이 검증의 핵심인가?      | IT          |
| 여러 도메인이 연결된 전체 흐름인가?        | IT          |

### 레이어별 도구

| 종류 | 어노테이션                      | 도구                           | 작성 시점        |
| ---- | ------------------------------- | ------------------------------ | ---------------- |
| UT   | `@ExtendWith(MockitoExtension)` | JUnit 5 + Mockito              | 기능 구현과 동시 |
| ST   | `@WebMvcTest`                   | MockMvc + spring-security-test | 기능 구현과 동시 |
| IT   | `@SpringBootTest`               | Testcontainers (MySQL, Redis)  | 4주차 QA 단계    |

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
| ST   | 슬라이스 테스트 | `@WebMvcTest`                   |
| IT   | 통합 테스트     | `@SpringBootTest`               |

### 규칙

- **행위**: 테스트 대상 기능 (명사구)
- **상황**: 성공 / 실패 조건 또는 엣지 케이스
- 단일 케이스면 상황 생략 가능

---

## 3. 도구 및 환경

| 항목              | 내용                                                               |
| ----------------- | ------------------------------------------------------------------ |
| 테스트 프레임워크 | JUnit 5                                                            |
| Mock 라이브러리   | Mockito                                                            |
| 슬라이스 테스트   | `@WebMvcTest` + spring-security-test                               |
| ST 인증 주입      | `SliceTestSupport.authenticated()` — JWT 쿠키 주입, 필터 실제 실행 |
| IT DB             | Testcontainers (MySQL 8.0)                                         |
| IT Redis          | Testcontainers (Redis 7.0)                                         |
| IT 베이스 클래스  | `IntegrationTestSupport`                                           |
| ST 베이스 클래스  | `SliceTestSupport`                                                 |
| 커버리지 측정     | JaCoCo (4주차 QA 단계 적용)                                        |
| 수동 테스트       | Swagger UI (`/swagger-ui`)                                         |

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

> `@WebMvcTest` 슬라이스 테스트. `SliceTestSupport.authenticated()`로 JWT 쿠키 주입.
> `JwtAuthenticationFilter` 실제 실행을 통해 인증 분기 검증.

| Test ID                                 | 행위                          | 상황                                                                            |
| --------------------------------------- | ----------------------------- | ------------------------------------------------------------------------------- |
| ST-AuthController-사용자인증-성공       | 인증된 사용자 `/auth/me` 호출 | 200 응답 검증                                                                   |
| ST-AuthController-사용자인증-미인증접근 | 미인증 사용자 `/auth/me` 호출 | 401 + `{"code":"UNAUTHORIZED"}` 검증                                            |
| ST-AuthController-응답바디형식확인-성공 | `/auth/me` 응답 바디 스펙     | userId/nickname/profileImageUrl/coins/nextCoinAt/birthYear/gender 7개 필드 검증 |

---

## 5. User 도메인

_3주차 구현 시 작성_

---

## 6. Result 도메인

### ScoringService (UT)

| Test ID                                       | 행위                            | 상황                                             |
| --------------------------------------------- | ------------------------------- | ------------------------------------------------ |
| UT-ScoringService-버킷정규화-성공             | 정상 원점수 입력 시 버킷값 반환 | D=32, I=10, S=-4, C=-14 → 버킷 8,5,3,2 검증      |
| UT-ScoringService-버킷정규화-최솟값최댓값혼합 | 최솟값/최댓값 경계 혼합 입력    | D=-24(버킷1), C=48(버킷9) 검증                   |
| UT-ScoringService-합계검증-실패               | D+I+S+C ≠ 24                    | 합계 25 → InvalidScoreException                  |
| UT-ScoringService-범위초과-상한               | 개별 원점수 48 초과             | D=49 → InvalidScoreException, 메시지에 49 포함   |
| UT-ScoringService-범위초과-하한               | 개별 원점수 -24 미만            | I=-25 → InvalidScoreException, 메시지에 -25 포함 |
| UT-ScoringService-toBucket-경계값             | 버킷 전환점 전수 검증           | 9단계 하한/상한 18개 케이스                      |

---

## 7. Assessment 도메인

_2주차 구현 시 작성_

---

## 8. Statistics 도메인

_3주차 구현 시 작성_

---

## 9. Colleague 도메인

_3주차 구현 시 작성_

---

## 10. Chemistry 도메인

_4주차 구현 시 작성_

---

## 11. Notification 도메인

_3주차 구현 시 작성_

---

## 12. Coin 도메인

_4주차 구현 시 작성_

---

## 13. 통합 테스트 시나리오

_4주차 QA 단계에 Testcontainers 기반으로 작성_

| Test ID                                  | 행위                                        | 상황                                   |
| ---------------------------------------- | ------------------------------------------- | -------------------------------------- |
| IT-AuthFlow-로그인후JWT쿠키발급-성공     | 카카오 로그인 완료 후 accessToken 쿠키 발급 | 쿠키 존재 및 보호된 API 접근 가능 검증 |
| IT-ResultFlow-비회원채점-성공            | 문항 조회 → POST /results/score             | 원점수/버킷값/보고서 반환 검증         |
| IT-ResultFlow-비회원결과저장연계-성공    | 로그인 후 비회원 결과 저장                  | POST /results → DB 저장 검증           |
| IT-AssessmentFlow-타인평정전체흐름-성공  | 링크 생성 → 토큰 접속 → 제출                | tests/disc_results 저장 검증           |
| IT-ChemistryFlow-보고서발행전체흐름-성공 | 코인 차감 → LLM 호출 → SSE 푸시             | 알림 저장 및 보고서 생성 검증          |
| IT-ColleagueFlow-동료등록전체흐름-성공   | 코드 생성 → 유효성 확인 → 등록              | 알림 전송 검증                         |

---

_본 문서는 기능 구현 진행에 따라 지속적으로 업데이트됩니다._  
_테스트 코드 경로는 각 섹션의 링크를 참조한다._
