# MyCPT MVP 개발 계획

**기간**: 2026.05.23 ~ 2026.07.03 (6주, 04주 계획에서 확장)
**목표**: MVP 출시 및 주변인 대상 베타 테스트 + 설문조사

---

## 전체 일정 요약

| 주차  | 기간          | 테마                                    | 핵심 산출물                                                          |
| ----- | ------------- | --------------------------------------- | -------------------------------------------------------------------- |
| 1주차 | 05.23 ~ 05.29 | 설계 완성 & 환경 구축                   | API 명세, 시퀀스 다이어그램, 아키텍처, 와이어프레임, 프로젝트 초기화 |
| 2주차 | 05.30 ~ 06.05 | 검사 핵심 기능 구현                     | 검사 응시, 채점 로직, LLM 캐시, 결과 확인, 타인 평정 링크            |
| 3주차 | 06.06 ~ 06.12 | 회원 기능 구현                          | 카카오 로그인, 결과 저장/이력, 통계 비교, 동료 초대/등록, 알림       |
| 4주차 | 06.13 ~ 06.22 | 동료/알림 도메인 + 기술 부채 해소       | ColleagueService, NotificationService, 테스트 정합성 보강            |
| 5주차 | 06.23 ~ 06.29 | 코인·케미·SSE — 백엔드+프런트 페어 진행 | CoinService+/me/coins, ChemistryService+/colleagues, SSE+/chemistry  |
| 6주차 | 06.30 ~ 07.03 | 잔여 페이지 + 배치 + QA + 배포          | 배치, 잔여 9개 화면, 전체 QA, 배포, 설문조사                         |

> **일정 변경 이력**: 당초 4주(~06.22) 계획이었으나, 취업 준비 병행으로 6주(~07.03)로 확장. 06.13 진입 시점에 4주차 작업(코인/케미/SSE/배치/QA/배포)이 전부 미착수 상태였음을 반영해 5·6주차로 재분배.

---

## 설계 산출물 현황

개발 착수 전 완성이 필요한 설계 산출물 목록이다.
1주차 중 미완성 항목을 완성하여 이후 개발의 기준으로 삼는다.

- [x] **서비스 기획서** — service-design.md v0.6 완성
- [x] **요구사항 명세서** — requirements-design.md v0.1 완성 (service-design.md에서 분리)
- [x] **유즈케이스 다이어그램** — usecase.puml v0.6 완성
- [x] **ERD + 테이블 명세** — database-design.md v0.6 완성 (profile_image_url 컬럼명 변경)
- [x] **API 명세** — api-design.md v0.4 완성 (GET /auth/me nextCoinAt 추가, profile_image_url 통일)
- [x] **DDL** — schema.sql v0.5 완성 (10개 테이블)
- [x] **시스템 아키텍처 다이어그램** — architecture-design.md 완성 (컴포넌트 다이어그램 + 주요 데이터 흐름 3종)
- [x] **시퀀스 다이어그램** — 3종 완성 (검사 채점/캐시 흐름, 케미 발행 Lazy Caching+중복방지+SSE 흐름, 비회원→회원 결과 저장 연계)
- [x] **Spring 패키지 구조 설계** — architecture-design.md에 포함. 레이어드 아키텍처 기준 패키지 트리
- [x] **화면 설계 (와이어프레임)** — 19개 화면 완성
- [x] **테스트 설계 문서** — test-design.md v0.1 완성 (레이어별 전략, 도메인별 테스트 케이스 ID 체계)
- [x] **유지보수 가이드** — maintenance-guide.md v0.1 완성 (API 버저닝, 컨트롤러 네이밍 규칙, 프로필 이미지 저장 방식)

---

## 1주차 — 설계 완성 & 환경 구축 (05.23 ~ 05.29)

### 목표

개발 착수 전 설계를 완성하고, 컨텍스트가 끊겨도 이어서 개발할 수 있는 기준 문서 체계를 수립한다.

### 계획

- [x] 서비스 기획서, 유즈케이스 다이어그램, ERD + 테이블 명세 완성
- [x] DDL 작성, API 명세 작성, 요구사항 명세서 분리
- [x] 와이어프레임 19개 화면 전체 완성
- [x] 시스템 아키텍처 + Spring 패키지 구조 설계
- [x] 시퀀스 다이어그램 3종 작성
- [x] 와이어프레임 대조 후 API 명세 개정
- [x] 카카오 OAuth 앱 등록 및 Spring Security 기본 설정
- [x] Spring Boot 프로젝트 초기화, Docker Compose 개발 환경 구성 (MySQL + Redis)
- [x] ScoringService 구현 — 원점수 범위 검증, D+I+S+C=24 검증, 버킷 정규화 로직 + 단위 테스트
- [x] Next.js 프로젝트 초기화 — App Router, Tailwind v4, Framer Motion, TanStack Query, Zustand

### 실행 기록

| 날짜       | 내용                                                                                                                                                                                                                                                                                                                                                           | 산출물                                                                                               |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| 05.23 (토) | 서비스 기획서 v0.5, 유즈케이스 다이어그램 v0.5, ERD + 테이블 명세 완성                                                                                                                                                                                                                                                                                         | service-design.md, database-design.md                                                                |
| 05.24 (일) | DDL 작성, API 명세 작성, 요구사항 명세서 분리                                                                                                                                                                                                                                                                                                                  | schema.sql, api-design.md, requirements-design.md                                                    |
| 05.25 (월) | 와이어프레임 스케치 (P1: 메인/검사/공유, P2: 결과/결과상세/동료&케미 완성). 시스템 아키텍처 + Spring 패키지 구조 설계, 시퀀스 다이어그램 3종 작성(05.26~05.27 작업분 선행 완료). 와이어프레임 대조 후 API 명세 개정(v0.2), database-design.md/schema.sql v0.5 개정                                                                                             | architecture-design.md, sequence\_\*.puml 3종, api-design.md, database-design.md, schema.sql         |
| 05.26 (화) | 와이어프레임 나머지 12개 화면 완성 → API 명세 최종 대조. 카카오 OAuth 앱 등록 및 Spring Security 기본 설정(05.29 작업분 선행 완료)                                                                                                                                                                                                                             | 와이어프레임 완성, SecurityConfig, CustomOAuth2UserService, AuthApi, AuthV1Controller, SwaggerConfig |
| 05.27 (수) | (05.25에 선행 완료)                                                                                                                                                                                                                                                                                                                                            | —                                                                                                    |
| 05.28 (목) | Spring Boot 프로젝트 초기화(Java 25, Spring Boot 3.5.14), 의존성 설정, Docker Compose 개발 환경 구성(MySQL+Redis), DDL 기반 DB 스키마 적용. ScoringService 구현(2주차 작업분 선행). Next.js 프로젝트 초기화 — App Router, Tailwind v4, Framer Motion, TanStack Query, Zustand, 모노레포 frontend/ 편입, Docker Compose frontend profile 구성, 디자인 토큰 설정 | 프로젝트 레포지토리, infra/docker/dev/, ScoringService, frontend/                                    |
| 05.29 (금) | (05.26에 선행 완료)                                                                                                                                                                                                                                                                                                                                            | —                                                                                                    |

### 완료 확인 체크리스트

- [x] 서비스 기획서 완성 (service-design.md v0.6)
- [x] 요구사항 명세서 완성 (requirements-design.md v0.1)
- [x] 유즈케이스 다이어그램 완성 (usecase.puml v0.6)
- [x] ERD 완성 (database-design.md v0.5. 10개 테이블, FK 관계, 인덱스 명시)
- [x] DDL 완성 (schema.sql v0.5. 10개 테이블)
- [x] API 명세 완성 (api-design.md v0.2. 29개 엔드포인트)
- [x] 아키텍처 다이어그램 완성 (architecture-design.md)
- [x] 시퀀스 다이어그램 3종 완성 (sequence_scoring / sequence_chemistry / sequence_guest_to_member)
- [x] Spring 패키지 구조 설계 완성 (architecture-design.md 내 포함)
- [x] Spring Boot 프로젝트 실행 확인 (Java 25.0.3 + Spring Boot 3.5.14)
- [x] DB 테이블 생성 확인 (10개 테이블, Docker Compose MySQL)
- [x] 와이어프레임 전체 완성 (19개 화면 완성)
- [x] API 명세 와이어프레임 최종 대조 및 보완
- [x] 카카오 개발자 앱 등록 완료
- [x] 카카오 OAuth 로그인 및 JWT 액세스 토큰 발급 확인
- [x] 미인증 상태 /auth/me 401 응답 확인
- [x] Swagger UI (/swagger-ui) 정상 접근 확인
- [x] 단위 테스트 작성 완료 (AUS-01~03, AUC-01~03)
- [x] 테스트 설계 문서 작성 완료 (test-design.md v0.1)
- [x] Next.js 프로젝트 초기화 확인 (App Router, Tailwind v4, Framer Motion, TanStack Query, Zustand)
- [x] Docker Compose frontend profile 구성 및 컨테이너 기동 확인
- [x] 디자인 토큰 적용 확인 (globals.css @theme, 폰트)

---

## 2주차 — 검사 핵심 기능 구현 (05.30 ~ 06.05)

### 목표

비회원도 사용 가능한 검사 응시 → 채점 → LLM 캐시 조회 → 결과 확인 흐름을 완성한다.
비회원 sessionStorage 임시 저장 및 로그인 후 결과 저장 연계까지 포함한다.
타인 평정 링크 생성 및 응시 흐름("나는 어떤 사람인가요?")도 포함한다.

### 계획

- [x] 검사 응시 화면 구현 — 문항 순서 표시, Most/Least 강제선택 UI, 클라이언트 셔플
- [x] DISC 채점 로직 구현 — 원점수 산출, 3단계 버킷 정규화, sessionStorage 임시 저장
- [x] disc_cache Lazy Caching 구현 — HIT/MISS 분기, 온디맨드 만료 처리
- [x] Claude API 연동 — 6개 섹션 버킷 기반 프롬프트 설계 및 응답 파싱 후 캐시 저장
- [x] 타인 평정 링크 생성/응시 흐름 구현 — 일회용 토큰, used 처리, 라벨 복사
- [x] 결과 확인 화면 구현 — DISC 시각화, 6개 섹션 보고서 표시, 자기/타인 평정 구분

### 실행 기록

| 날짜       | 내용                                                                                                                 | 산출물                                   |
| ---------- | -------------------------------------------------------------------------------------------------------------------- | ---------------------------------------- |
| 05.30 (토) | (05.28에 선행 완료). 검사 응시 화면 구현 — 문항 순서 표시, Most/Least 강제선택 UI, 클라이언트 셔플                   | 검사 페이지                              |
| 05.31 (일) | (05.30에 선행 완료)                                                                                                  | —                                        |
| 06.01 (월) | DISC 채점 로직 구현, disc_cache Lazy Caching 구현, Claude API 연동(6개 섹션 프롬프트 설계 및 응답 파싱 후 캐시 저장) | ScoringService, CacheService, LlmService |
| 06.02 (화) | (06.01에 선행 완료)                                                                                                  | —                                        |
| 06.03 (수) | 타인 평정 링크 생성/응시 흐름 구현 — 일회용 토큰, used 처리, 라벨 복사                                               | AssessmentService                        |
| 06.04 (목) | 결과 확인 화면 구현 — DISC 시각화, 6개 섹션 보고서 표시, 자기/타인 평정 구분                                         | 결과 페이지                              |
| 06.05 (금) | (06.04에 선행 완료)                                                                                                  | —                                        |

### 완료 확인 체크리스트

- [x] ScoringService 단위 테스트 통과 (원점수 범위 검증, D+I+S+C=24, 버킷 정규화 경계값)
- [x] Most/Least 강제선택 UI 정상 동작
- [x] 클라이언트 선택지 셔플 정상 동작
- [x] 문항 이동 시 이전 답 잔류 없음 (key 리마운트)
- [x] 24문항 완료 후 제출 버튼 활성화
- [x] Step 2에서 ✕ 탭 시 중단 확인 다이얼로그 표시
- [x] vitest 환경 구성 및 테스트 30개 통과
- [x] 캐시 HIT + 유효 시 DB 즉시 반환 확인
- [x] 캐시 HIT + 만료 시 LLM 호출 후 UPDATE 확인
- [x] 캐시 MISS 시 LLM 호출 후 INSERT 확인
- [x] 비회원 원점수 sessionStorage 저장 확인
- [x] 타인 평정 링크 생성 및 일회용 처리 확인
- [x] 타인 평정 응시 완료 시 라벨 포함 tests + disc_tests 저장 확인
- [x] 결과 화면 6개 섹션 보고서 정상 표시 확인
- [x] 비회원 이름 "사용자"로 렌더링 확인

---

## 3주차 — 회원 기능 구현 (06.06 ~ 06.12)

### 목표

카카오 로그인을 중심으로 결과 저장, 이력 조회, 통계 비교, 변화 추이까지 완성하고,
동료 초대 코드 생성 → 동료 등록 → 알림 기능까지 케미 기능의 전처리 흐름을 완성한다.

### 계획

- [x] 프로필 설정 API 및 페이지 구현 (닉네임, 생년, 성별, 프로필 이미지)
- [x] '로그인하고 결과 저장' — sessionStorage 원점수를 `POST /results`로 전송
- [x] 결과 이력 조회 페이지 구현 (자기/타인 평정 구분, 커서 기반 페이지네이션)
- [x] 통계 집계 로직 구현 — 나이대/성별 기준 평균 산출
- [x] 통계 비교 + 변화 추이 화면 구현
- [x] 동료 초대 코드 생성/조회 API 구현
- [x] 동료 등록 흐름 구현, 동료 등록 알림 전송

### 실행 기록

| 날짜       | 내용                                                                                               | 산출물                                                             |
| ---------- | -------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| 06.06 (토) | 프로필 설정 API 및 페이지 구현 (닉네임, 생년, 성별, 프로필 이미지)                                 | UserService, UserV1Controller, useMe, /me, /me/profile, middleware |
| 06.07 (일) | '로그인하고 결과 저장' — sessionStorage 원점수를 `POST /results`로 전송, tests + disc_tests INSERT | ResultService                                                      |
| 06.08 (월) | 결과 이력 조회 페이지 구현 (자기/타인 평정 구분, 커서 기반 페이지네이션)                           | 이력 페이지                                                        |
| 06.09 (화) | 통계 집계 로직 구현 — 나이대/성별 기준 평균 산출 (자기 평정만), tests JOIN disc_tests 집계 쿼리    | StatisticsService                                                  |
| 06.10 (수) | 통계 비교 + 변화 추이 화면 구현 (`GET /statistics/comparison`, `GET /statistics/trend`)            | 통계 페이지                                                        |
| 06.11 (목) | 동료 초대 코드 생성/조회 API 구현 (대문자+숫자 8자리, 온디맨드 리프레시)                           | PeerCodeService                                                    |
| 06.12 (금) | 동료 등록 흐름 구현 (링크/코드 입력 → colleagues INSERT, UNION ALL 조회), 동료 등록 알림 전송      | ColleagueService, NotificationService                              |

### 완료 확인 체크리스트

- [x] 카카오 로그인 후 JWT 발급/갱신 처리
- [x] 프로필 설정 (닉네임, 생년, 성별, 이미지) 정상 동작 확인
- [x] 프로필 이미지 업로드 — jpg/png/webp 형식, 10MB 이하 검증 확인
- [x] sessionStorage 원점수 전송 후 tests + disc_tests 정상 저장 확인
- [x] 결과 이력 목록 자기/타인 평정 구분 및 라벨 표시 확인
- [x] 나이대/성별 통계 비교 수치 정확성 확인 (자기 평정만 집계)
- [x] 생년/성별 미입력 시 average: null 반환 확인
- [x] 변화 추이 summary + trend 정상 반환 확인
- [x] 동료 코드 생성 및 만료 7일 확인
- [x] 동료 코드 온디맨드 리프레시 확인
- [x] 초대 링크 및 코드 직접 입력 양방향 동료 등록 확인
- [x] SELF_INVITE, EXPIRED_CODE, ALREADY_COLLEAGUE 에러 처리 확인
- [x] UNION ALL 양방향 동료 목록 조회 확인
- [x] 동료 등록 완료 시 알림 전송 확인

### 기술 부채 (4주차 착수 전 처리)

- [x] ResultService 단위 테스트 작성
- [x] DiscTestRepository 슬라이스 테스트 작성 (@DataJpaTest + JpaTestSupport)
- [x] AssessmentV1Controller, UserV1Controller, AuthV1Controller 응답 DTO 리팩토링 (Map → record)
- [x] BusinessException / ErrorCode / ErrorResponse 예외 처리 체계 통합
- [ ] MemberCta "결과 상세로 가기" → "친구에게도 평정 요청해보기" CTA로 교체 (공유 시트 연동 후 구현, 미착수)

---

## 4주차 — 동료/알림 도메인 + 기술 부채 해소 (06.13 ~ 06.22)

### 목표

당초 계획은 코인·케미·SSE·배치·QA·배포까지 포함한 마무리 주차였으나,
실제로는 Colleague/Notification 도메인 구현과 테스트 정합성 보강에 집중되었다.
미착수 항목(코인/케미/SSE/배치/QA/배포)은 5·6주차로 재배치한다.

### 계획

- [x] 기술 부채 해소 — BusinessException/ErrorCode/ErrorResponse 통합, 응답 DTO record 전환, 테스트 지원 클래스 분리
- [x] Colleague 도메인 백엔드 구현 — ColleagueRepository, ColleagueService, ColleagueApi/V1Controller
- [x] Notification CTI 엔티티 3종 + NotificationService + NotificationRepository 구현
- [x] ChemistryReport 스텁 엔티티, TestType enum 추가
- [x] NotificationResponse/NotificationListResponse DTO, NotificationService.list() 반환 타입 변경
- [x] NotificationApi/NotificationV1Controller 구현
- [x] ColleagueV1ControllerTest 작성 (기술 부채 해소)
- [x] test-design.md §8~§11 누락 Test Code 링크 보강

### 실행 기록

| 날짜       | 내용                                                                                                                                                                                                                    | 산출물                                                                                                                   |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| 06.13 (토) | 기술 부채 해소 — BusinessException/ErrorCode/ErrorResponse 통합, 응답 DTO Map→record 리팩토링, ResultService/DiscTestRepository 테스트 작성, MvcTestSupport→JpaTestSupport 테스트 지원 클래스 분리                      | BusinessException, ErrorCode, ErrorResponse, MvcTestSupport, JpaTestSupport, ResultServiceTest, DiscTestRepositoryTest   |
| 06.17 (수) | Colleague 도메인 백엔드 구현, Notification CTI 엔티티 3종 + NotificationService + NotificationRepository 구현, ChemistryReport 스텁 엔티티 + TestType enum 추가, NotificationResponse/NotificationListResponse DTO 추가 | ColleagueService, ColleagueV1Controller, Notification, ColleagueNotification, ChemistryNotification, NotificationService |
| 06.20 (토) | NotificationApi/NotificationV1Controller 구현 (GET /notifications, DELETE /notifications/{id}). ColleagueV1ControllerTest 작성(기술 부채 해소). test-design.md §8~§11 누락 Test Code 링크 보강 및 정합성 검증           | NotificationApi, NotificationV1Controller, ColleagueV1ControllerTest, test-design.md                                     |

### 완료 확인 체크리스트

- [x] StatisticsService/PeerCodeService/ColleagueService/NotificationService 단위 테스트 전체 통과
- [x] ColleagueV1Controller, NotificationV1Controller 슬라이스 테스트 전체 통과 (인증 분기)
- [x] NotificationResponse에 type/referenceId 필드 포함 및 instanceof 분기 검증
- [x] test-design.md 문서-코드 정합성 검증 완료 (Test Code 링크, 케이스 표 일치)

### 미착수 → 5·6주차 이월

- [x] 코인 시스템 구현 — CoinService → **5주차 완료**
- [x] 케미 보고서 발행 흐름 — ChemistryService + Lazy Caching + 중복 방지 → **5주차 완료 (백엔드)**
- [x] SSE 연결 구현 — SseService → **5주차 완료 (백엔드)**
- [x] Claude API 케미 프롬프트 설계 → **5주차 완료**
- [ ] Spring Batch (만료 코드/토큰 삭제) → **6주차로 이월**
- [ ] 전체 흐름 QA, 배포, 베타 테스터 초대 → **6주차로 이월**

---

## 5주차 — 코인·케미·SSE: 백엔드+프런트 페어 진행 (06.23 ~ 06.29)

### 목표

백엔드 기능을 구현한 즉시 대응하는 프런트 페이지를 붙여서, 매 기능 단위로 실제 동작을 눈으로 검증할 수 있게 한다.
동료 등록 흐름(`/invite/[code]`)은 6주차로 미루되, 케미 검증에 필요한 동료 데이터는 Swagger UI로 `POST /colleagues`를 직접 호출해 우회 생성한다.

### 계획

**페어 1 — 코인**

- [x] CoinService 구현 (가입 초기 지급 3개, 온디맨드 충전 `next_coin_at` 기반, coin_transactions 적재)
- [x] `/me/coins` 페이지 구현 (잔량 + 3슬롯 시각화 + 카운트다운 + 사용 이력)
- [x] 검증: 코인 초기 지급/충전/이력이 화면에 정확히 반영되는지 확인

**페어 2 — 케미 발행**

- [x] ChemistryService 구현 (동료 검증 + 버킷 조회 + 코인 차감 + chemistry_reports INSERT + @TransactionalEventListener(AFTER_COMMIT) 트리거)
- [x] ChemistryReportProcessor 구현 (@Async + @Retryable + @Recover)
- [x] ChemistryCacheService 구현 (Lazy Caching + SELECT FOR UPDATE 발행자/구독자 분기 + CountDownLatch 대기)
- [x] ChemistryTxHelper 구현 (REQUIRES_NEW 트랜잭션 전담 빈 — self-invocation 방지)
- [x] ChemistryEventPublisher / ChemistryEventSubscriber 구현 (Redis Pub/Sub 브로커)
- [x] SseService 구현 (Map<userId, SseEmitter> 소유, pushChemistryReady / pushChemistryError)
- [x] RedisConfig 구현 (RedisMessageListenerContainer, chemistry:\* 패턴 구독)
- [x] ChemistryReportIssuedEvent 구현
- [x] chemistry_cache 6,561행 seeding (schema.sql + chemistry_cache_seed.sql)
- [x] Claude API 케미 프롬프트 설계 (두 사람 DISC 버킷값 기반 6개 섹션)
- [x] `/colleagues` 동료 목록 페이지 구현
- [x] `/colleagues/[id]` 동료 프로필 + 케미 발행 CTA 페이지 구현
- [x] 검증: Swagger로 동료 관계 생성 → 화면에서 케미 발행 트리거 → 202 응답 확인

**페어 3 — SSE 실시간 반영**

- [x] SSE 연결 구현 (`GET /notifications/stream`, Last-Event-ID 기반 재전송)
- [x] 케미 보고서 발행 완료 시 SSE 푸시 + 상대방 인앱 알림 전송 연동
- [x] `/chemistry` 케미 보고서 목록 페이지 구현 (generating 상태 inline 표시)
- [x] `/chemistry/[id]` 케미 보고서 상세 페이지 구현
- [ ] 헤더 알림 드롭다운 구현 → 6주차로 이월
- [x] 검증: 케미 발행 → SSE 푸시 도착 → 화면 자동 갱신, 알림 클릭 시 즉시 삭제까지 end-to-end 확인

### 실행 기록

| 날짜       | 내용                                                                                                                                                                                                                                                                                                                                                                                                                          | 산출물                                                                                                                                                              |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 06.22 (월) | CoinReason enum, CoinTransaction 엔티티, CoinTransactionRepository, CoinService(가입보너스/온디맨드충전/차감/이력조회), CoinApi, CoinV1Controller 구현. EntityTestSupport(구 EntityIdSetter) setField() 추가. CoinServiceTest(UT 11케이스) + CoinV1ControllerTest(ST 4케이스) 통과.                                                                                                                                           | CoinReason, CoinTransaction, CoinTransactionRepository, CoinService, CoinApi, CoinV1Controller, EntityTestSupport, CoinServiceTest, CoinV1ControllerTest            |
| 06.23 (화) | ChemistryCache 엔티티 설계 개선 — status 컬럼(NULL/GENERATING/READY) 추가, 6,561행 seding 방침 전환. ChemistryCacheStatus enum 신규. ChemistryReport.create() cacheId 생성 시점 주입으로 변경 (실패 추적 강화). ChemistryService.issue()에 버킷 조회 + BusinessException(NO_RESULT) 즉시 반환 추가.                                                                                                                           | ChemistryCacheStatus, ChemistryCache, ChemistryReport, ChemistryService                                                                                             |
| 06.24 (수) | ChemistryCacheService 전면 재작성 (Lazy Caching + SELECT FOR UPDATE 기반 발행자/구독자 분기 + CountDownLatch + CopyOnWriteArrayList). ChemistryTxHelper 신규 (REQUIRES_NEW 트랜잭션 전담 — self-invocation 방지). ChemistryCacheRepository findByIdWithLock() 추가. ChemistryReportProcessor LatestBuckets 파라미터 수신으로 변경 (재조회 제거).                                                                              | ChemistryCacheService, ChemistryTxHelper, ChemistryCacheRepository, ChemistryReportProcessor                                                                        |
| 06.25 (목) | ChemistryEventPublisher / ChemistryEventSubscriber 신규 (Redis Pub/Sub 브로커. publishReady 파라미터 String report 제거). RedisConfig 신규 (chemistry:\* 패턴 구독). SseService 신규 (Map<userId, SseEmitter> 소유). ChemistryReportIssuedEvent 신규. ChemistryService @TransactionalEventListener(AFTER_COMMIT) 트리거로 전환 (커밋 전 @Async 실행 문제 원천 차단). ChemistryReportProcessor handle() / process() 역할 분리. | ChemistryEventPublisher, ChemistryEventSubscriber, RedisConfig, SseService, ChemistryReportIssuedEvent                                                              |
| 06.26 (금) | chemistry_cache 6,561행 seeding SQL 작성 (CROSS JOIN 방식, schema.sql + chemistry_cache_seed.sql). 개발 문서 전체 수정 — database-design.md v0.12, architecture-design.md v0.10, service-design.md v0.11, api-design.md POST /chemistry-reports 섹션 개정, usecase.puml 재작성, sequence-chemistry.puml 전면 재작성, sequence-guest-to-member.puml "9단계→3단계" 수정.                                                        | schema.sql, chemistry_cache_seed.sql, 개발 문서 6종, UML 2종                                                                                                        |
| 06.27 (토) | Chemistry 도메인 테스트 전체 작성 — ChemistryReportTest(UT 3), ChemistryCacheIdTest(UT 2), ChemistryCacheTest(UT 3), ChemistryRepositoryTest(ST 4), ChemistryTxHelperTest(IT 3, TransactionTemplate 외부 트랜잭션 롤백 방식), ChemistryCacheServiceIntegrationTest(IT 9 — Lazy Caching 3, Duplication Defense 2, AFTER_COMMIT 검증 2). test-design.md v0.8/v0.9 개정.                                                         | ChemistryReportTest, ChemistryCacheIdTest, ChemistryCacheTest, ChemistryRepositoryTest, ChemistryTxHelperTest, ChemistryCacheServiceIntegrationTest, test-design.md |
| 06.29 (월) | /chemistry, /chemistry/[id] 페이지 구현 (myRole 기반 페어 레이블, GENERATING 직접 접근 가드). ChemistryReportSummary/ChemistryReportDetail에 닉네임+myRole 필드 추가 (클라이언트 userId 비교 제거). 케미 보고서 이름 플레이스홀더({REQUESTER}/{PARTNER}) 조회 시점 치환 방식으로 전환. 전역 토스트 시스템 구축(toastStore, ToastContainer, useToast).                                                                         | ChemistryReportSummary, ChemistryReportDetail, /chemistry, /chemistry/[id], toastStore, Toast, useToast                                                             |

### 완료 확인 체크리스트

- [x] 코인 초기 지급 (3개) 확인
- [x] next_coin_at 기반 온디맨드 충전 로직 확인
- [x] coin_transactions 로그 적재 확인
- [x] `/me/coins` 화면에서 잔량/카운트다운/이력 정상 표시 확인
- [x] 동료 목록에서 대상 선택 → 202 즉시 반환 확인
- [x] `/colleagues`, `/colleagues/[id]` 화면 정상 동작 확인
- [x] @TransactionalEventListener(AFTER_COMMIT) 커밋 후 LLM 호출 확인 (IT 검증 완료)
- [x] 동시 요청 시 LLM 1회 호출 보장 확인 (IT 검증 완료)
- [ ] 인터넷 재연결 시 Last-Event-ID 기반 놓친 알림 재전송 확인
- [ ] 케미 보고서 발행 시 상대방 인앱 알림 전송 확인
- [x] 알림 클릭 시 즉시 삭제 확인
- [x] 코인 0개일 때 발행 차단 (INSUFFICIENT_COINS) 확인
- [x] `/chemistry`, `/chemistry/[id]` 화면에서 generating → ready 자동 전환 확인
- [ ] 헤더 알림 드롭다운 정상 동작 확인

### 미착수 → 6주차 이월

- [ ] 헤더 알림 드롭다운 구현 → **6주차로 이월**

### 알려진 개선 여지 (기능은 정상 동작, 우선순위 낮음)

- [ ] SSE 엔드포인트(`/notifications/stream`)는 알림 도메인이 아닌 범용 실시간 채널 — `/api/v1/events/stream` 등으로 경로 분리 검토
- [ ] 프롬프트 캐싱(`cache_control` 블록 마커) — buildPrompt()는 변수 분리 구조까지만 적용, 실제 캐싱 비활성화 상태
- [ ] Next.js rewrites 프록시는 SSE를 우회하는 것으로 임시 해결 — 운영 배포 시 Nginx 등 리버스 프록시 레벨에서 압축 제외 설정 필요 (현재는 dev 환경 한정 회피)

---

## 6주차 — 잔여 페이지 + 배치 + QA + 배포 (06.30 ~ 07.03)

### 목표

5주차에서 검증용으로 우회했던 동료 등록 흐름을 포함한 잔여 화면을 완성하고,
배치 작업과 전체 QA를 거쳐 배포 후 베타 테스트를 시작한다. 4일의 단축 주차이므로 정적 페이지보다 핵심 동선을 우선한다.

### 계획

**전역 셸 통합**

IA v0.3에 확정된 전역 컴포넌트(상단 헤더, 하단 탭바 4개, SSE 토스트 영역, 푸터) 구현은 1~5주차 동안 의도적으로 보류함.
페어별 기능(코인/케미/SSE) 검증을 셸 통합 없이 개별 URL 접근으로 진행해, 셸 변수를 배제한 채 페이지 자체의 정확성에 집중하기 위함이었음.
구현 대상 페이지 범위가 사실상 확정된 시점이므로, 모든 개별 페이지를 한 번에 엮는 통합 작업을 마무리 단계에 진행.

- [ ] `(app)` route group 레이아웃 구현 — 헤더(로고/알림 종/프로필 칩 or 로그인 버튼) + 하단 탭바(홈/검사 결과/동료 & 케미/내 정보) + 푸터(면책 조항)
- [ ] 비회원 잠김 탭 클릭 시 "로그인이 필요해요" 토스트 안내
- [ ] 구현 페이지(`/me/coins`, `/colleagues`, `/colleagues/[id]`, `/chemistry`, `/chemistry/[id]`)를 `(app)` 레이아웃 하위로 편입
- [ ] 검증: 4탭 전환 정상 동작, 비회원 잠김 탭 토스트 확인, 헤더 프로필 칩 클릭 시 `/me` 진입 확인

**이월 — 5주차 미완료 프론트엔드**

- [ ] 헤더 알림 드롭다운
- [ ] end-to-end 케미 발행 검증

**우선순위 1 — 핵심 동선**

- [ ] `/invite/[code]` 동료 초대 수락 페이지 (비회원→로그인 연계)
- [x] `/results/[id]` 결과 상세 페이지
- [ ] `/assessments/[token]` 타인 평정 응시 페이지
- [ ] Spring Batch — 만료 동료 코드 + 만료 평정 토큰 통합 삭제 스케줄러

**우선순위 2 — 정적/보조 페이지**

- [ ] `/me/about` 서비스 소개 & 약관
- [ ] `/me/help` 고객 문의/FAQ
- [ ] `/me/leave` 회원탈퇴

**마무리**

- [ ] 전체 흐름 통합 테스트 — 비회원/회원/타인 평정/케미 시나리오
- [ ] 배포 환경 구성 (Redis, AWS S3 스토리지 연동)
- [ ] 베타 테스터 초대 및 설문조사 배포

### 실행 기록

| 날짜       | 내용                                                                                                                                                                                                                                                                                                                                                                                                                                          | 산출물                                                                                                                  |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| 06.30 (화) | NotificationApi/V1Controller에 GET /notifications/stream 엔드포인트 추가 (SseService.connect() 연동 누락분). 발행자 본인 SSE push 누락 수정 (ChemistryReportProcessor). SSE 페이로드 키 reportId → referenceId 통일. 전역 SSE 연결 구조로 전환 (useSseConnection, SseProvider — 페이지별 개별 연결 제거). Next.js rewrites 프록시의 gzip 압축이 SSE 스트리밍을 버퍼링하는 문제 발견 — SSE만 백엔드 직접 연결로 분리(NEXT_PUBLIC_BACKEND_URL). | NotificationApi, NotificationV1Controller, ChemistryReportProcessor, useSseConnection, SseProvider, next.config.ts 우회 |

### 완료 확인 체크리스트

- [ ] 만료 동료 코드 + 만료 평정 토큰 배치 삭제 확인
- [ ] `/invite/[code]` 초대 수락 → 동료 등록 정상 흐름 확인
- [ ] `/results/[id]` 결과 상세 정상 렌더링 확인
- [ ] `/assessments/[token]` 타인 평정 응시 정상 흐름 확인
- [ ] `/me/about`, `/me/help`, `/me/leave` 정상 접근 확인
- [ ] 비회원 전체 플로우 QA 통과
- [ ] 회원 전체 플로우 QA 통과
- [ ] 타인 평정 전체 플로우 QA 통과
- [ ] 케미 전체 플로우 QA 통과
- [ ] AWS S3 스토리지 연동 확인 (로컬 → S3 교체)
- [ ] 배포 후 운영 환경 정상 동작 확인
- [ ] 베타 테스터 최소 5명 초대 완료
- [ ] 설문조사 문항 준비 완료

---

## 베타 테스트 설문조사 항목 (초안)

MVP 출시 후 주변인 대상으로 수집할 설문 문항이다.

### 사용성

- 검사 문항이 이해하기 쉬웠나요? (5점 척도)
- 검사를 완료하는 데 얼마나 걸렸나요? (객관식)
- 검사 중 불편하거나 막힌 부분이 있었나요? (주관식)

### 결과 만족도

- 분석 결과가 자신을 잘 표현한다고 느끼나요? (5점 척도)
- 강점/약점 설명이 납득되었나요? (5점 척도)
- 타인 평정 결과와 자기 평정 결과를 비교해보셨나요? (Y/N)
- 가장 인상적인 결과 항목은 무엇인가요? (주관식)

### 케미 기능

- 동료 케미 보고서를 사용해보셨나요? (Y/N)
- 케미 보고서가 실제 관계를 잘 반영한다고 느끼나요? (5점 척도)

### 전반적 평가

- 이 서비스를 지인에게 추천하겠나요? (NPS 0~10점)
- 유료 전환 시 얼마까지 지불할 의사가 있나요? (객관식)
- 추가로 원하는 기능이 있다면? (주관식)

---

## 리스크 및 대응

| 리스크                                | 가능성 | 대응 방안                                                                    |
| ------------------------------------- | ------ | ---------------------------------------------------------------------------- |
| Claude API 응답 지연 (10초 초과)      | 중     | @Async 비동기 처리 + SSE 푸시로 대기 시간 제거                               |
| 카카오 OAuth 심사 지연                | 저     | 개발용 테스트 앱으로 우선 진행                                               |
| 설계 변경으로 인한 일정 지연          | 저     | 1주차 핵심 설계 완성 (ERD/API/DDL 완료)                                      |
| 배포 환경 설정 이슈                   | 중     | 6주차에 Redis + S3 연동 포함하여 사전 준비                                   |
| 베타 테스터 참여율 저조               | 저     | 사전 참여 의사 확인 후 초대                                                  |
| 6주차 4일 단축 주에 작업 과밀         | 중     | 우선순위 1(핵심 동선)/2(정적 페이지) 구분, 2는 QA·배포 이후로 추가 이월 가능 |
| 5주차 백엔드 항목 재지연 (4주차 전례) | 중     | 페어 단위(백엔드→프런트→검증)로 쪼개 매일 단위 진행 상황 가시화              |

---

_본 문서는 개발 진행에 따라 지속적으로 업데이트됩니다._
