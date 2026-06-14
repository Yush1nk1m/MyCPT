# MyCPT MVP 개발 계획

**기간**: 2026.05.23 ~ 2026.06.22 (4주)
**목표**: MVP 출시 및 주변인 대상 베타 테스트 + 설문조사

---

## 전체 일정 요약

| 주차  | 기간          | 테마                  | 핵심 산출물                                                          |
| ----- | ------------- | --------------------- | -------------------------------------------------------------------- |
| 1주차 | 05.23 ~ 05.29 | 설계 완성 & 환경 구축 | API 명세, 시퀀스 다이어그램, 아키텍처, 와이어프레임, 프로젝트 초기화 |
| 2주차 | 05.30 ~ 06.05 | 검사 핵심 기능 구현   | 검사 응시, 채점 로직, LLM 캐시, 결과 확인, 타인 평정 링크            |
| 3주차 | 06.06 ~ 06.12 | 회원 기능 구현        | 카카오 로그인, 결과 저장/이력, 통계 비교, 동료 초대/등록, 알림       |
| 4주차 | 06.13 ~ 06.22 | 케미 기능 + 마무리    | 케미 보고서, 코인 시스템, SSE, 배치, QA, 배포, 설문조사 준비         |

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
- [x] **시퀀스 다이어그램** — 3종 완성 (검사 채점/캐시 흐름, 케미 발행 @Async+SSE 흐름, 비회원→회원 결과 저장 연계)
- [x] **Spring 패키지 구조 설계** — architecture-design.md에 포함. 레이어드 아키텍처 기준 패키지 트리
- [x] **화면 설계 (와이어프레임)** — 19개 화면 완성
- [x] **테스트 설계 문서** — test-design.md v0.1 완성 (레이어별 전략, 도메인별 테스트 케이스 ID 체계)
- [x] **유지보수 가이드** — maintenance-guide.md v0.1 완성 (API 버저닝, 컨트롤러 네이밍 규칙, 프로필 이미지 저장 방식)

---

## 1주차 — 설계 완성 & 환경 구축 (05.23 ~ 05.29)

### 목표

개발 착수 전 설계를 완성하고, 컨텍스트가 끊겨도 이어서 개발할 수 있는 기준 문서 체계를 수립한다.

### 일별 계획

| 날짜       | 작업                                                                                                                                                                                                                                        | 산출물                                                                                                  | 완료 |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ---- |
| 05.23 (토) | 서비스 기획서 v0.5, 유즈케이스 다이어그램 v0.5, ERD + 테이블 명세 완성                                                                                                                                                                      | service-design.md, database-design.md                                                                   | [x]  |
| 05.24 (일) | DDL 작성, API 명세 작성, 요구사항 명세서 분리                                                                                                                                                                                               | schema.sql, api-design.md, requirements-design.md                                                       | [x]  |
| 05.25 (월) | 와이어프레임 스케치 (P1: 메인/검사/공유, P2: 결과/결과상세/동료&케미 완성. 나머지 12개 화면 미완성 — Claude Design 토큰 소진)                                                                                                               | 와이어프레임 (진행 중)                                                                                  | [-]  |
|            | 시스템 아키텍처 + Spring 패키지 구조 설계 (05.26 선행 완료)                                                                                                                                                                                 | architecture-design.md                                                                                  | [x]  |
|            | 시퀀스 다이어그램 3종 작성 (05.27 선행 완료)                                                                                                                                                                                                | sequence\_\*.puml 3종                                                                                   | [x]  |
|            | 와이어프레임 대조 후 API 명세 개정 (v0.2), database-design.md v0.5 / schema.sql v0.5 개정                                                                                                                                                   | api-design.md, database-design.md, schema.sql                                                           | [x]  |
| 05.26 (화) | 와이어프레임 나머지 12개 화면 완성 → API 명세 최종 대조                                                                                                                                                                                     | 와이어프레임 완성, api-design.md 보완                                                                   | [x]  |
|            | 카카오 OAuth 앱 등록 및 Spring Security 기본 설정 (05.29 선행 완료)                                                                                                                                                                         | SecurityConfig, CustomOAuth2UserService, AuthApi, AuthV1Controller, SwaggerConfig, 단위/슬라이스 테스트 | [x]  |
| 05.27 (수) | (선행 완료 — 05.25에 진행)                                                                                                                                                                                                                  | —                                                                                                       | [x]  |
| 05.28 (목) | Spring Boot 프로젝트 초기화 (Java 25, Spring Boot 3.5.14), 의존성 설정, Docker Compose 개발 환경 구성 (MySQL + Redis), DDL 기반 DB 스키마 적용                                                                                              | 프로젝트 레포지토리, infra/docker/dev/                                                                  | [x]  |
|            | ScoringService 구현 — 원점수 범위 검증, D+I+S+C=24 검증, 버킷 정규화 로직 + 단위 테스트 (2주차 05.30 선행)                                                                                                                                  | ScoringService                                                                                          | [x]  |
|            | Next.js 프로젝트 초기화 — App Router, Tailwind CSS v4, Framer Motion, TanStack Query, Zustand 설치, 모노레포 frontend/ 편입, Docker Compose frontend profile 구성, 디자인 토큰 설정 (globals.css @theme), 폰트 설정 (Inter, JetBrains Mono) | frontend/                                                                                               | [x]  |

| 05.29 (금) | (선행 완료 — 05.26에 진행) | — | [x] |

### 체크리스트

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

### 일별 계획

| 날짜       | 작업                                                                           | 산출물            | 완료 |
| ---------- | ------------------------------------------------------------------------------ | ----------------- | ---- |
| 05.30 (토) | (선행 완료 — 05.28에 진행)                                                     | -                 | [x]  |
|            | 검사 응시 화면 구현 — 문항 순서 표시, Most/Least 강제선택 UI, 클라이언트 셔플  | 검사 페이지       | [x]  |
| 05.31 (일) | (05.30 선행 완료)                                                              |                   | [x]  |
| 06.01 (월) | DISC 채점 로직 구현 — 원점수 산출, 3단계 버킷 정규화, sessionStorage 임시 저장 | ScoringService    | [x]  |
|            | disc_cache Lazy Caching 구현 — HIT/MISS 분기, 온디맨드 만료 처리               | CacheService      | [x]  |
|            | Claude API 연동 — 6개 섹션 버킷 기반 프롬프트 설계 및 응답 파싱 후 캐시 저장   | LlmService        | [x]  |
| 06.02 (화) | (선행 완료 — 06.01에 진행)                                                     | -                 | [x]  |
| 06.03 (수) | (선행 완료 — 06.01에 진행)                                                     | -                 | [x]  |
|            | 타인 평정 링크 생성/응시 흐름 구현 — 일회용 토큰, used 처리, 라벨 복사         | AssessmentService | [x]  |
| 06.04 (목) | (선행 완료 — 06.03에 진행)                                                     | -                 | [x]  |
|            | 결과 확인 화면 구현 — DISC 시각화, 6개 섹션 보고서 표시, 자기/타인 평정 구분   | 결과 페이지       | [x]  |
| 06.05 (금) | (선행 완료 — 06.04에 진행)                                                     | -                 | [x]  |

### 체크리스트

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
- [x] 타인 평정 응시 완료 시 라벨 포함 tests + disc_results 저장 확인
- [x] 결과 화면 6개 섹션 보고서 정상 표시 확인
- [x] 비회원 이름 "사용자"로 렌더링 확인

---

## 3주차 — 회원 기능 구현 (06.06 ~ 06.12)

### 목표

카카오 로그인을 중심으로 결과 저장, 이력 조회, 통계 비교, 변화 추이까지 완성하고,
동료 초대 코드 생성 → 동료 등록 → 알림 기능까지 케미 기능의 전처리 흐름을 완성한다.

### 일별 계획

| 날짜       | 작업                                                                                                 | 산출물                                                             | 완료 |
| ---------- | ---------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ | ---- |
| 06.06 (토) | 프로필 설정 API 및 페이지 구현 (닉네임, 생년, 성별, 프로필 이미지)                                   | UserService, UserV1Controller, useMe, /me, /me/profile, middleware | [x]  |
| 06.07 (일) | '로그인하고 결과 저장' — sessionStorage 원점수를 `POST /results`로 전송, tests + disc_results INSERT | ResultService                                                      | [x]  |
| 06.08 (월) | 결과 이력 조회 페이지 구현 (자기/타인 평정 구분, 커서 기반 페이지네이션)                             | 이력 페이지                                                        | [x]  |
| 06.09 (화) | 통계 집계 로직 구현 — 나이대/성별 기준 평균 산출 (자기 평정만), tests JOIN disc_results 집계 쿼리    | StatisticsService                                                  | [ ]  |
| 06.10 (수) | 통계 비교 + 변화 추이 화면 구현 (`GET /statistics/comparison`, `GET /statistics/trend`)              | 통계 페이지                                                        | [ ]  |
| 06.11 (목) | 동료 초대 코드 생성/조회 API 구현 (대문자+숫자 8자리, 온디맨드 리프레시)                             | PeerCodeService                                                    | [ ]  |
| 06.12 (금) | 동료 등록 흐름 구현 (링크/코드 입력 → colleagues INSERT, UNION ALL 조회), 동료 등록 알림 전송        | ColleagueService, NotificationService                              | [ ]  |

### 체크리스트

- [ ] 카카오 로그인 후 JWT 발급/갱신 처리
- [ ] 프로필 설정 (닉네임, 생년, 성별, 이미지) 정상 동작 확인
- [ ] 프로필 이미지 업로드 — jpg/png/webp 형식, 10MB 이하 검증 확인
- [x] sessionStorage 원점수 전송 후 tests + disc_results 정상 저장 확인
- [x] 결과 이력 목록 자기/타인 평정 구분 및 라벨 표시 확인
- [ ] 나이대/성별 통계 비교 수치 정확성 확인 (자기 평정만 집계)
- [ ] 생년/성별 미입력 시 average: null 반환 확인
- [ ] 변화 추이 summary + trend 정상 반환 확인
- [ ] 동료 코드 생성 및 만료 7일 확인
- [ ] 동료 코드 온디맨드 리프레시 확인
- [ ] 초대 링크 및 코드 직접 입력 양방향 동료 등록 확인
- [ ] SELF_INVITE, EXPIRED_CODE, ALREADY_COLLEAGUE 에러 처리 확인
- [ ] UNION ALL 양방향 동료 목록 조회 확인
- [ ] 동료 등록 완료 시 알림 전송 확인

### 기술 부채 (4주차 착수 전 처리)

- [x] ResultService 단위 테스트 작성 — list(): hasNext 판단, raterType 필터, cursor null 여부 / detail(): 본인 소유 검증(403), 존재하지 않는 ID(404)
- [x] DiscResultRepository 슬라이스 테스트 작성 (@DataJpaTest + JpaTestSupport) — findByUserIdWithCursor() 커서/raterType 필터 동작 검증 / findByTestIdWithDetail() JOIN FETCH 정상 로드 검증
- [x] AssessmentV1Controller, UserV1Controller, AuthV1Controller 응답 DTO 리팩토링 (Map → record)
- [x] BusinessException / ErrorCode / ErrorResponse 예외 처리 체계 통합 — 개별 예외 클래스 4개 제거, IllegalArgumentException도 BusinessException(INVALID_REQUEST)으로 통합
- [ ] MemberCta "결과 상세로 가기" → "친구에게도 평정 요청해보기" CTA로 교체 (공유 시트 연동 후 구현)

---

## 4주차 — 케미 기능 + QA + 배포 (06.13 ~ 06.22)

### 목표

코인 시스템과 동료 케미 보고서 발행 기능(비동기 + SSE)을 완성하고, 전체 흐름 QA 후 배포하여 베타 테스트를 시작한다.

### 일별 계획

| 날짜       | 작업                                                                                                                                                                                                                                   | 산출물                                                                                                                   | 완료 |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ | ---- |
| 06.13 (토) | 기술 부채 해소 (4주차 착수 전 처리) — BusinessException/ErrorCode/ErrorResponse 통합, 응답 DTO Map→record 리팩토링, ResultService/DiscResultRepository 테스트 작성, MvcTestSupport→JpaTestSupport 테스트 지원 클래스 분리 (06.13 완료) | BusinessException, ErrorCode, ErrorResponse, MvcTestSupport, JpaTestSupport, ResultServiceTest, DiscResultRepositoryTest | [x]  |
|            | 코인 시스템 구현 — 가입 초기 지급(3개), 온디맨드 충전 로직(next_coin_at 기반), coin_transactions 적재                                                                                                                                  | CoinService                                                                                                              | [ ]  |
| 06.14 (일) | 동료 목록 페이지 구현 및 케미 보고서 발행 흐름 구현 (@Async LLM 호출, 202 즉시 반환)                                                                                                                                                   | ChemistryService                                                                                                         | [ ]  |
| 06.15 (월) | SSE 연결 구현 (`GET /notifications/stream`) — Last-Event-ID 기반 재전송, 인터넷 재연결 토스트 알림                                                                                                                                     | SseEmitter, SSE 핸들러                                                                                                   | [ ]  |
| 06.16 (화) | Claude API 케미 프롬프트 설계 — 두 사람의 DISC 버킷값 기반 6개 섹션 보고서 생성                                                                                                                                                        | 케미 프롬프트                                                                                                            | [ ]  |
| 06.17 (수) | 케미 보고서 결과 화면 구현 — 6개 섹션 표시, 발행자/대상자 이름 렌더링                                                                                                                                                                  | 케미 결과 페이지                                                                                                         | [ ]  |
| 06.18 (목) | 케미 보고서 발행 완료 시 SSE 푸시 + 상대방 인앱 알림 전송 처리                                                                                                                                                                         | NotificationService                                                                                                      | [ ]  |
| 06.19 (금) | Spring Batch — 만료 동료 코드 + 만료 평정 토큰 통합 삭제 스케줄러 구현                                                                                                                                                                 | BatchScheduler                                                                                                           | [ ]  |
| 06.20 (토) | 전체 흐름 통합 테스트 — 비회원/회원/타인 평정/케미 시나리오                                                                                                                                                                            | QA 체크리스트                                                                                                            | [ ]  |
| 06.21 (일) | 버그 수정 및 UI 마감 정리. 배포 환경 구성 (Redis, AWS S3 스토리지 연동)                                                                                                                                                                | 배포 완료                                                                                                                | [ ]  |
| 06.22 (월) | 베타 테스터 초대 및 설문조사 배포                                                                                                                                                                                                      | 테스터 초대, 설문 링크                                                                                                   | [ ]  |

### 체크리스트

- [ ] 코인 초기 지급 (3개) 확인
- [ ] next_coin_at 기반 온디맨드 충전 로직 확인
- [ ] coin_transactions 로그 적재 확인
- [ ] 동료 목록에서 대상 선택 → 202 즉시 반환 확인
- [ ] @Async LLM 호출 완료 후 SSE 푸시 확인
- [ ] 인터넷 재연결 시 Last-Event-ID 기반 놓친 알림 재전송 확인
- [ ] 케미 보고서 발행 시 상대방 인앱 알림 전송 확인
- [ ] 알림 클릭 시 즉시 삭제 확인
- [ ] 코인 0개일 때 발행 차단 (INSUFFICIENT_COINS) 확인
- [ ] 만료 동료 코드 + 만료 평정 토큰 배치 삭제 확인
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

| 리스크                           | 가능성 | 대응 방안                                        |
| -------------------------------- | ------ | ------------------------------------------------ |
| Claude API 응답 지연 (10초 초과) | 중     | @Async 비동기 처리 + SSE 푸시로 대기 시간 제거   |
| 카카오 OAuth 심사 지연           | 저     | 개발용 테스트 앱으로 우선 진행                   |
| 설계 변경으로 인한 일정 지연     | 저     | 1주차 핵심 설계 완성 (ERD/API/DDL 완료)          |
| 배포 환경 설정 이슈              | 중     | 4주차 06.21에 Redis + S3 연동 포함하여 사전 준비 |
| 베타 테스터 참여율 저조          | 저     | 사전 참여 의사 확인 후 초대                      |

---

_본 문서는 개발 진행에 따라 지속적으로 업데이트됩니다._
