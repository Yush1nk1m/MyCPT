# MyCPT 시스템 아키텍처 설계

**문서 버전**: v0.10
**작성일**: '26.06.24.
**작성자**: 김유신

---

## 변경 이력

| 버전  | 변경 내용                                                                                                                                                                                                                                                                                           | 날짜       |
| ----- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| v0.2  | 패키지 루트 com.mycpt.backend로 수정. 컨트롤러 Interface+V1 네이밍 구조 반영. Swagger UI 추가.                                                                                                                                                                                                      | '26.05.26. |
| v0.3  | JWT 인증 방식으로 변경에 따른 아키텍처 및 Redis 명세 수정                                                                                                                                                                                                                                           | '26.05.27. |
| v0.4  | Next.js 컴포넌트 역할 추가                                                                                                                                                                                                                                                                          | '26.05.28. |
| v0.5  | 타인 평정 흐름 시퀀스 다이어그램 추가                                                                                                                                                                                                                                                               | '26.06.05. |
| v0.6  | result 도메인 패키지 구조 실제 구현 반영. RaterType enums 분리. DTO 패키지 추가. ForbiddenException 추가. EntityNotFoundException 핸들러 전역 이동.                                                                                                                                                 | '26.06.09. |
| v0.7  | 예외 처리 체계 통합 (BusinessException/ErrorCode/ErrorResponse). 개별 예외 클래스 제거. 응답 DTO Map → record 교체 반영 (MeResponse, UpdateProfileResponse, UpdateProfileImageResponse, CreateTokenResponse, SubjectInfoResponse, SubmitResponse).                                                  | '26.06.13. |
| v0.8  | `DiscResult` → `DiscTest`, `DiscResultRepository` → `DiscTestRepository` 이름 변경. `Test` 엔티티 추상 클래스 전환 (`@Inheritance(JOINED)`). notification 도메인 CTI 적용 (`Notification` 추상화, `ColleagueNotification` / `ChemistryNotification` 서브클래스 추가). MySQL 테이블 수 12 → 13 반영. | '26.06.15. |
| v0.9  | ScoreRequest → DiscScoreRequest, ScoreResponse → DiscScoreResponse 이름 변경. common/enums/TestType.java 추가 (ChemistryReport 전용). TestType을 DTO에 사용하지 않는 설계 근거 반영.                                                                                                                | '26.06.16. |
| v0.10 | chemistry 도메인: ChemistryCache.status 필드 추가, chemistry/event 패키지 신규 (ChemistryEventPublisher / ChemistryEventSubscriber). RedisConfig 추가. Redis 용도 Pub/Sub 브로커 역할 추가.                                                                                                         | '26.06.24. |

---

## 목차

1. [시스템 아키텍처 다이어그램](#1-시스템-아키텍처-다이어그램)
2. [컴포넌트 설명](#2-컴포넌트-설명)
3. [주요 데이터 흐름](#3-주요-데이터-흐름)
4. [Spring 패키지 구조](#4-spring-패키지-구조)

---

## 1. 시스템 아키텍처 다이어그램

![시스템 아키텍처](../images/MyCPT_Architecture.svg)

---

## 2. 컴포넌트 설명

### 2.1 Client — Next.js

| 역할            | 설명                                                                         |
| --------------- | ---------------------------------------------------------------------------- |
| SSR/SSG         | 초기 페이지 렌더링                                                           |
| sessionStorage  | 비회원 DISC 원점수 임시 보관                                                 |
| SSE 수신        | `/notifications/stream` 연결 유지, 재연결 시 Last-Event-ID 전송              |
| 선택지 셔플     | DISC 태그 미노출 상태에서 클라이언트 셔플 처리                               |
| 원점수 산출     | 24문항 응답 기반 DISC 원점수 계산 (프론트엔드 처리)                          |
| 서버 상태 관리  | TanStack Query — API 캐싱, 로딩/에러 처리, 글로벌 onError(401 자동 로그아웃) |
| 클라이언트 상태 | Zustand — 유저 인증 상태, 시트 열림 여부 등 전역 UI 상태                     |

### 2.2 Spring Boot — 백엔드

| 레이어     | 구성 요소                         | 역할                                                |
| ---------- | --------------------------------- | --------------------------------------------------- |
| Security   | Spring Security + Kakao OAuth 2.0 | 인증/인가. JWT 액세스 토큰 발급(Authorization 헤더) |
| Controller | REST Controllers                  | HTTP 요청 수신, DTO 변환, 응답 반환                 |
| Service    | Business Services                 | 비즈니스 로직, 트랜잭션 경계                        |
| Repository | JPA Repositories                  | DB CRUD                                             |
| Async      | @Async + ThreadPoolTaskExecutor   | 케미 보고서 LLM 비동기 호출                         |
| SSE        | SseEmitter                        | 케미 보고서 완료 푸시, Last-Event-ID 재전송         |
| Batch      | Spring Batch                      | 만료 동료 코드 + 만료 평정 토큰 주기 삭제           |

### 2.3 외부 시스템

| 시스템               | 용도                                                                                           |
| -------------------- | ---------------------------------------------------------------------------------------------- |
| MySQL                | 메인 데이터 저장소 (13개 테이블)                                                               |
| Redis                | disc_cache @Cacheable L2 캐시 (운영 환경). chemistry_cache 중복 LLM 호출 방지용 Pub/Sub 브로커 |
| Anthropic Claude API | DISC 분석 보고서 생성, 케미 보고서 생성                                                        |
| AWS S3               | 프로필 이미지 저장 (운영 환경. 개발은 로컬 파일시스템)                                         |
| Kakao OAuth          | 소셜 로그인                                                                                    |
| Swagger UI           | API 문서화 및 수동 테스트 (`/swagger-ui`)                                                      |

---

## 3. 주요 데이터 흐름

### 3.1 검사 응시 → 결과 반환 흐름

![검사 응시 및 결과 반환 흐름](../images/MyCPT_Sequence_Scoring.svg)

### 3.2 케미 보고서 발행 흐름 (@Async + SSE)

![케미 보고서 발행 흐름](../images/MyCPT_Sequence_Chemistry.svg)

### 3.3 비회원 → 회원 결과 저장 연계 흐름

![비회원에서 회원 가입 후 결과 저장 흐름](../images/MyCPT_Sequence_GuestToMember.svg)

### 3.4 타인 평정 흐름 (링크 생성 → 접속 → 제출)

![타인 평정 흐름](../images/MyCPT_Sequence_Assessment.svg)

---

## 4. Spring 패키지 구조

```
com.mycpt.backend
├── BackendApplication.java
├── BackendApplication.java:Zone.Identifier
├── common
│   ├── enums
│   │   └── TestType.java
│   ├── exception
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── llm
│   │   └── AnthropicLlmClient.java
│   ├── response
│   │   └── ErrorResponse.java
│   └── storage
│       ├── LocalStorageService.java
│       └── StorageService.java
├── config
│   ├── AsyncConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtProvider.java
│   ├── RedisConfig.java
│   ├── SecurityConfig.java
│   ├── StorageConfig.java
│   └── SwaggerConfig.java
├── domain
│   ├── assessment
│   │   ├── controller
│   │   │   ├── AssessmentApi.java
│   │   │   └── AssessmentV1Controller.java
│   │   ├── dto
│   │   │   ├── CreateTokenRequest.java
│   │   │   ├── CreateTokenResponse.java
│   │   │   ├── SubjectInfoResponse.java
│   │   │   └── SubmitResponse.java
│   │   ├── entity
│   │   │   └── AssessmentToken.java
│   │   ├── repository
│   │   │   └── AssessmentTokenRepository.java
│   │   └── service
│   │       └── AssessmentService.java
│   ├── auth
│   │   ├── controller
│   │   │   ├── AuthApi.java
│   │   │   └── AuthV1Controller.java
│   │   ├── dto
│   │   │   ├── KakaoUserInfo.java
│   │   │   ├── MeResponse.java
│   │   │   └── UserPrincipal.java
│   │   └── service
│   │       └── CustomOAuth2UserService.java
│   ├── chemistry
│   │   ├── controller
│   │   │   ├── ChemistryApi.java
│   │   │   └── ChemistryV1Controller.java
│   │   ├── dto
│   │   │   ├── ChemistryReportDetail.java
│   │   │   ├── ChemistryReportListResponse.java
│   │   │   ├── ChemistryReportRequest.java
│   │   │   └── ChemistryReportSummary.java
│   │   ├── entity
│   │   │   ├── ChemistryCache.java
│   │   │   ├── ChemistryCacheId.java
│   │   │   └── ChemistryReport.java
│   │   ├── enums
│   │   │   ├── ChemistryCacheStatus.java
│   │   │   └── ChemistryReportStatus.java
│   │   ├── event
│   │   │   ├── ChemistryEventPublisher.java
│   │   │   ├── ChemistryEventSubscriber.java
│   │   │   └── ChemistryReportIssuedEvent.java
│   │   ├── repository
│   │   │   ├── ChemistryCacheRepository.java
│   │   │   └── ChemistryReportRepository.java
│   │   └── service
│   │       ├── ChemistryCacheService.java
│   │       ├── ChemistryReportProcessor.java
│   │       ├── ChemistryService.java
│   │       └── ChemistryTxHelper.java
│   ├── coin
│   │   ├── controller
│   │   │   ├── CoinApi.java
│   │   │   └── CoinV1Controller.java
│   │   ├── dto
│   │   │   ├── CoinBalanceResponse.java
│   │   │   ├── CoinHistoryResponse.java
│   │   │   └── CoinTransactionResponse.java
│   │   ├── entity
│   │   │   └── CoinTransaction.java
│   │   ├── enums
│   │   │   └── CoinReason.java
│   │   ├── repository
│   │   │   └── CoinTransactionRepository.java
│   │   └── service
│   │       └── CoinService.java
│   ├── colleague
│   │   ├── controller
│   │   │   ├── ColleagueApi.java
│   │   │   └── ColleagueV1Controller.java
│   │   ├── dto
│   │   │   ├── ColleagueListResponse.java
│   │   │   ├── ColleagueRegisterRequest.java
│   │   │   ├── ColleagueResponse.java
│   │   │   ├── InviteInfoResponse.java
│   │   │   └── PeerCodeResponse.java
│   │   ├── entity
│   │   │   ├── Colleague.java
│   │   │   └── PeerCode.java
│   │   ├── repository
│   │   │   ├── ColleagueRepository.java
│   │   │   └── PeerCodeRepository.java
│   │   └── service
│   │       ├── ColleagueService.java
│   │       └── PeerCodeService.java
│   ├── notification
│   │   ├── controller
│   │   │   ├── NotificationApi.java
│   │   │   └── NotificationV1Controller.java
│   │   ├── dto
│   │   │   ├── NotificationListResponse.java
│   │   │   └── NotificationResponse.java
│   │   ├── entity
│   │   │   ├── ChemistryNotification.java
│   │   │   ├── ColleagueNotification.java
│   │   │   └── Notification.java
│   │   ├── repository
│   │   │   └── NotificationRepository.java
│   │   └── service
│   │       ├── NotificationService.java
│   │       └── SseService.java
│   ├── result
│   │   ├── controller
│   │   │   ├── ResultApi.java
│   │   │   └── ResultV1Controller.java
│   │   ├── dto
│   │   │   ├── DiscBuckets.java
│   │   │   ├── DiscScoreRequest.java
│   │   │   ├── DiscScoreResponse.java
│   │   │   ├── DiscScores.java
│   │   │   ├── ResultDetailResponse.java
│   │   │   ├── ResultListResponse.java
│   │   │   ├── ResultSummaryResponse.java
│   │   │   └── SaveResponse.java
│   │   ├── entity
│   │   │   ├── DiscCache.java
│   │   │   ├── DiscCacheId.java
│   │   │   ├── DiscTest.java
│   │   │   └── Test.java
│   │   ├── enums
│   │   │   └── RaterType.java
│   │   ├── repository
│   │   │   ├── DiscCacheRepository.java
│   │   │   ├── DiscTestRepository.java
│   │   │   └── TestRepository.java
│   │   └── service
│   │       ├── DiscCacheService.java
│   │       ├── ResultService.java
│   │       └── ScoringService.java
│   ├── statistics
│   │   ├── controller
│   │   │   ├── StatisticsApi.java
│   │   │   └── StatisticsV1Controller.java
│   │   ├── dto
│   │   │   ├── BucketAverage.java
│   │   │   ├── ComparisonResponse.java
│   │   │   ├── DiscAverageDto.java
│   │   │   ├── DiscBucketsDto.java
│   │   │   ├── LatestBuckets.java
│   │   │   ├── TrendPoint.java
│   │   │   └── TrendResponse.java
│   │   ├── repository
│   │   │   └── StatisticsRepository.java
│   │   └── service
│   │       └── StatisticsService.java
│   └── user
│       ├── controller
│       │   ├── UserApi.java
│       │   └── UserV1Controller.java
│       ├── dto
│       │   ├── UpdateProfileImageResponse.java
│       │   ├── UpdateProfileRequest.java
│       │   └── UpdateProfileResponse.java
│       ├── entity
│       │   └── User.java
│       ├── enums
│       │   └── Gender.java
│       ├── repository
│       │   └── UserRepository.java
│       └── service
│           └── UserService.java
└── global
    └── exception
        └── GlobalExceptionHandler.java
```

---

### 패키지 구조 설계 원칙

| 원칙             | 내용                                                                                                   |
| ---------------- | ------------------------------------------------------------------------------------------------------ |
| 도메인 중심 분리 | 기능별로 `domain/` 하위에 패키지를 나눔. 레이어(controller/service/repository)는 각 도메인 내부에 위치 |
| 의존 방향        | Controller → Service → Repository. 역방향 참조 금지                                                    |
| 트랜잭션 경계    | Service 레이어에서만 `@Transactional` 선언                                                             |
| 비동기 격리      | @Async + @TransactionalEventListener는 ChemistryReportProcessor에 격리.                                |
|                  | REQUIRES_NEW 트랜잭션은 ChemistryTxHelper 별도 빈으로 분리 (self-invocation 방지)                      |
| 환경별 전환      | `StorageService` 인터페이스로 로컬/S3 구현체를 분리. `@Profile`로 환경별 Bean 등록                     |
| 컨트롤러 버저닝  | 인터페이스({도메인}Api)와 구현체({도메인}V1Controller)로 분리.                                         |
|                  | Swagger 애노테이션은 인터페이스에 집중. 구현체는 로직만 담당.                                          |
|                  | V2 추가 시 {도메인}V2Controller implements {도메인}Api.                                                |

---

_본 문서는 개발 진행에 따라 지속적으로 업데이트됩니다._
