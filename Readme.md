# MyCPT (나의 역량)

> **My ComPeTency** — DISC 이론 기반 직무 역량 성향 분석 서비스
> A free, LLM-powered competency analysis service built on DISC theory.

![Status](https://img.shields.io/badge/status-active%20development-orange)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.5-6DB33F)
![Frontend](https://img.shields.io/badge/frontend-Next.js%2015-000000)
![Java](https://img.shields.io/badge/Java-25-007396)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## 📌 Overview · 프로젝트 개요

유료로 제공되던 DISC 역량 검사를 **누구나 무료로** 응시하고, LLM이 생성한 개인 맞춤 피드백과
동료 간 협업 케미 보고서까지 받을 수 있는 서비스입니다. 타인이 나를 평정하는
**"나는 어떤 사람인가요?"** 기능으로 자기 평정과 타인 평정을 비교할 수 있습니다.

A solo full-stack project that makes paid DISC competency assessments **free for everyone**,
pairing LLM-generated personalized feedback with collaboration-chemistry reports between colleagues.

### 이 프로젝트의 설계 의도 · Design Thesis

직전 팀 프로젝트(MSA 기반 _Basetalk_)에서 마주한 **분산 시스템의 복잡성**을 항해한 경험을
바탕으로, MyCPT는 **의도적 설계로 기술 부채를 최소화**하는 것을 일관된 목표로 삼았습니다.
"복잡성을 다루는 능력"과 "복잡성을 만들지 않는 판단"을 한 포트폴리오 안에서 대비시키기 위한
단독 풀스택 프로젝트입니다.

> _Basetalk demonstrated navigating distributed-system complexity; MyCPT is the deliberate
> counterpoint — minimizing technical debt through intentional, up-front design._

심리학·AI 복수 전공 배경을 살려 **강제선택(Forced Choice)** 척도를 채택, 사회적 바람직성
편향(social desirability bias)을 통제하는 측정 설계를 적용한 점이 일반적인 자가응답형 검사와
다른 도메인 차별점입니다.

---

## 🎯 Key Technical Decisions · 핵심 기술 의사결정

포트폴리오에서 가장 중요한 부분입니다. 각 결정은 "왜 그렇게 했는가"의 근거를 함께 둡니다.

### 1. LLM 비용 상한 고정 — 버킷 기반 Lazy Caching

LLM 호출 비용이 사용자 수에 비례해 선형 증가하는 문제를, **결과 캐싱 가능한 입력 공간으로
정규화**하여 비용 상한을 고정했습니다.

- D·I·S·C 각 척도의 원점수(`-24 ~ +48`)를 **불균등 3구간**(Low / Mid / High)으로 정규화
  → 가능한 조합은 `3⁴ = 81`가지
- 동일 버킷은 DB에서 즉시 반환(캐시 히트), 신규 버킷만 LLM 1회 호출 후 저장(캐시 미스)
- 만료는 조회 시점에 판정하는 **온디맨드 방식** — 실제 접근되는 버킷만 갱신
- 보고서 원문에 이름을 넣지 않고 저장 → **동일 버킷의 모든 사용자에게 재사용**, 이름은 렌더링 시 삽입

구간 폭을 균등하지 않게 둔 이유: 강제선택형 검사는 점수가 중앙(Mid)에 밀집하고 양 극단은
희소합니다. 9단계 균등 분할(`9⁴ = 6,561`) 대비 **캐시 히트율을 극대화**하기 위해 통계적
분포를 반영한 3구간으로 축소했습니다.

#### 💰 비용 정량 분석 · Cost Analysis

> 모델: `claude-sonnet-4-6` / 입력 $3.00·출력 $15.00 per 1M tokens
> _(2026년 기준 단가. 최신 단가는 [docs.claude.com](https://docs.claude.com) 참조 — 외부 변동 사실)_

| 시나리오                           | 사용자 10만 명 기준         | 비고                                                        |
| ---------------------------------- | --------------------------- | ----------------------------------------------------------- |
| 캐싱 없음 (사용자마다 실시간 호출) | **$3,150** (약 460만 원)    | 사용자 수에 선형 증가                                       |
| 버킷 Lazy Caching 적용             | **최대 $2.55** (약 3,700원) | 81개 버킷 전부 생성 시 Worst Case, 사용자 수 무관 상한 고정 |
| **절감률**                         | **≈ 99.9%**                 | (1만 명 기준 99.2%)                                         |

- 호출 1회 비용 = 입력 500토큰 × $3/1M + 출력 2,000토큰 × $15/1M = **$0.0315/회**
- 전체 버킷 사전 생성 최대 비용 = $0.0315 × 81 = **$2.55** (실제로는 접근된 버킷만 생성되어 더 낮음)

### 2. 검사 유형 확장을 위한 스키마 — Class Table Inheritance

향후 MBTI·Big5 등 검사 유형 추가를 대비해, 검사 공통 메타데이터와 유형별 결과를 분리했습니다.

- `tests` (부모): 유형 무관 공통 헤더 — `rater_type`(SELF/OTHER), `test_type`, `label`, `created_at`
- `disc_results` (자식): DISC 전용 원점수·버킷값. `test_id`를 PK 겸 FK로 두어 **1:1 관계를 스키마 레벨에서 강제**
- 신규 검사 유형은 `{type}_results` 테이블만 추가하면 되어 부모 테이블 변경 불필요

### 3. 케미 보고서 — @Async + SSE 비동기 실시간 발행

두 사람의 DISC 조합(`3⁴ × 3⁴ = 6,561`가지)은 6,561 버킷 Lazy Caching + SELECT FOR UPDATE 기반 중복 LLM 호출 방지를 적용합니다.

동일 버킷 조합 동시 요청 시 발행자 1개 스레드만 LLM을 호출하고 나머지는 Redis Pub/Sub으로 완료를 통보받습니다.

- 요청 즉시 `202 Accepted` 반환 → `@Async`로 LLM 호출
- 발행 완료 시 **SSE(`SseEmitter`)** 로 발행자에게 실시간 푸시 (재연결 시 `Last-Event-ID` 재전송)
- 무제한 호출을 막기 위한 **코인 시스템**(가입 시 3개, 24시간마다 1개 충전·최대 3개, 발행 1회당 1개 차감 — 온디맨드 충전)

### 4. 비회원 우선 설계 — 서버 세션 없이 플러딩 방어

검사 응시에 회원가입을 요구하지 않습니다. 비회원의 DISC 원점수는 **클라이언트
`sessionStorage`** 에만 임시 보관하고 서버 세션을 발급하지 않아 익명 플러딩을 방어하며,
로그인 시점에 원점수를 서버로 전송해 결과를 영구 저장합니다.

---

## 🏗️ Architecture · 아키텍처

![System Architecture](docs/images/MyCPT_Architecture.svg)

주요 데이터 흐름(검사 채점·캐시 / 케미 발행 @Async+SSE / 비회원→회원 결과 저장 / 타인 평정)의
상세 시퀀스 다이어그램은 [설계 문서](#-documentation--설계-문서)를 참조하세요.

---

## 🛠️ Tech Stack · 기술 스택

| 영역                 | 기술                                                         |
| -------------------- | ------------------------------------------------------------ |
| **Backend**          | Java 25, Spring Boot 3.5.x, Spring Security, Spring Data JPA |
| **Auth**             | Kakao OAuth 2.0, JWT                                         |
| **API Docs**         | SpringDoc OpenAPI (Swagger UI)                               |
| **DB / Cache**       | MySQL, Redis (`@Cacheable`)                                  |
| **LLM**              | Anthropic Claude API (`claude-sonnet-4-6`)                   |
| **Async / Realtime** | Spring `@Async`, SSE (`SseEmitter`)                          |
| **Batch**            | Spring Batch (만료 토큰·코드 정리)                           |
| **Storage**          | Local FS (dev) → AWS S3 (prod)                               |
| **Frontend**         | Next.js 15 (App Router), TypeScript, Tailwind CSS v4         |
| **State**            | TanStack Query (server), Zustand (client)                    |
| **UI**               | Framer Motion, react-markdown                                |
| **Test**             | JUnit5, Mockito/BDDMockito, Testcontainers, Vitest           |
| **Infra**            | Docker Compose (profiles: infra / frontend / all)            |

---

## 📂 Project Structure · 프로젝트 구조

모노레포 구조입니다. 백엔드는 **도메인 중심 패키지** 분리(레이어는 도메인 내부에 위치)를 따릅니다.

```
.
├── backend/                  # Spring Boot (com.mycpt.backend)
│   └── src/main/java/.../domain/
│       ├── auth/             # 카카오 OAuth2 + JWT
│       ├── user/             # 프로필
│       ├── result/           # 채점 · LLM 캐시 · 결과 저장/이력
│       │   ├── controller/   # ResultApi (계약·Swagger) + ResultV1Controller (구현)
│       │   ├── service/      # ScoringService / CacheService / LlmService / ResultService
│       │   ├── entity/       # Test (CTI 부모) · DiscResult (자식) · DiscCache
│       │   └── ...
│       ├── chemistry/        # 케미 보고서 (Lazy Caching + SELECT FOR UPDATE + Redis Pub/Sub)
│       ├── notification/     # 인앱 알림 + SSE
│       └── coin/             # 코인 시스템
├── frontend/                 # Next.js 15 (App Router)
├── infra/docker/dev/         # Docker Compose 개발 환경 (MySQL · Redis)
└── docs/                     # 설계 문서 (아래 참조)
```

> **컨트롤러 패턴**: 도메인마다 `{Domain}Api` 인터페이스(Swagger 애너테이션·API 계약)와
> 이를 구현하는 `{Domain}V1Controller`를 분리합니다.

---

## ✅ Development Status · 개발 현황

> **MVP 개발 진행 중입니다 (Active development).** 아래는 기능별 실제 구현 현황입니다.

| 기능                                                  | 상태       |
| ----------------------------------------------------- | ---------- |
| 카카오 OAuth2 로그인 + JWT 인증                       | ✅ 완료    |
| 검사 응시 · 채점 · 버킷 정규화                        | ✅ 완료    |
| LLM 버킷 캐싱 (Lazy / 온디맨드 만료)                  | ✅ 완료    |
| 결과 저장 · 이력 · 상세 조회 (커서 페이지네이션)      | ✅ 완료    |
| 타인 평정 링크 생성 · 응시                            | ✅ 완료    |
| 프로필 이미지 (S3 연동)                               | 🚧 진행 중 |
| 동료 초대 · 네트워킹                                  | ✅ 완료    |
| 케미 보고서 (Lazy Caching + 중복 방지 + SSE) — 백엔드 | ✅ 완료    |
| 케미 보고서 프론트엔드 · 코인 화면 · SSE 토스트       | 🚧 진행 중 |
| 통계 비교 (나이대/성별)                               | ✅ 완료    |
| 배포                                                  | ⏳ 예정    |

---

## 🚀 Getting Started · 실행 방법

```bash
# 1. 인프라 기동 (MySQL + Redis)
docker compose --profile infra up -d

# 2. 백엔드 (포트 8080)
cd backend && ./gradlew bootRun

# 3. 프론트엔드 (포트 3000)
cd frontend && npm install && npm run dev
```

> 백엔드 환경 변수에 Kakao OAuth 키와 `ANTHROPIC_API_KEY`가 필요합니다.
> 프론트엔드 프록시는 `BACKEND_URL` 환경 변수를 사용합니다 (서버 주소 하드코딩 없음).
> API 문서는 백엔드 기동 후 `/swagger-ui`에서 확인할 수 있습니다.

---

## 📖 Documentation · 설계 문서

설계가 코드에 선행하는 것을 원칙으로, 모든 결정은 문서에 먼저 반영합니다.

| 문서                                 | 내용                                           |
| ------------------------------------ | ---------------------------------------------- |
| `docs/common/service-design.md`      | 서비스 기획 · 검사 설계 · LLM 비용 최적화 전략 |
| `docs/common/requirements-design.md` | 기능/비기능 요구사항 · 유즈케이스 · MVP 범위   |
| `docs/common/architecture-design.md` | 시스템 아키텍처 · 컴포넌트 · 패키지 구조       |
| `docs/backend/database-design.md`    | ERD · 테이블 명세 (CTI 패턴) · 인덱스 전략     |
| `docs/common/test-design.md`         | 레이어별 테스트 전략 · 테스트 ID 체계          |

---

## ⚖️ Disclaimer

본 결과는 참고용 성향 분석이며 심리 진단이 아닙니다. DISC 이론(W. M. Marston)은 퍼블릭 도메인이며,
MyCPT는 독자적인 브랜드명과 문항으로 구성된 서비스입니다.

---

<p align="center"><sub>Built by 김유신 · Solo full-stack · 2026</sub></p>
