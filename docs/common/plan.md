# MyCPT 개발 계획

**최종 갱신**: 2026-07-14
**기간**: 2026.05.23 ~ (진행 중)
**목표**: MVP 출시 및 주변인 대상 베타 테스트 + 설문조사

> **문서 사용법** — 이 문서는 관심사별로 세 개의 살아있는 섹션과 아카이브로 구성된다.
> 각 섹션은 서로 다른 질문에 답한다. 갱신 시 해당 관심사의 섹션만 손댄다.
>
> - **§1 진척 현황** — *"지금 어디까지 됐는가."* 기능·화면별 완료 상태 스냅샷. 항상 최신 유지.
> - **§2 다음 작업** — *"에이전트가 무엇을 해야 하는가."* 우선순위 백로그 + 현재 진행 중 작업. 이월·지연 작업은 전부 여기 모은다.
> - **§3 작업 로그** — *"언제 무엇을 했는가."* 일자별 append-only 기록. 완료된 것만 최신순으로 쌓는다.
> - **부록** — 1~6주차 원 주차 계획(요약), 리스크, 설문 초안. 참조용이며 갱신하지 않는다.
>
> 상태 기호: ✅ 완료 · 🚧 진행 중 · ⬜ 미착수

---

## §1. 진척 현황 (Status)

### 백엔드 — 도메인별

| 도메인 | 핵심 기능 | 상태 | 근거 |
| --- | --- | --- | --- |
| `auth` | 카카오 OAuth, JWT 발급/갱신, `GET /auth/me` | ✅ | api-design |
| `assessment` | 검사 응시·채점(ScoringService, 버킷 정규화), 타인 평정 링크/일회용 토큰 | ✅ | service-design §3 |
| `result` | 결과 저장(`POST /results`), 이력 커서 페이지네이션 | ✅ | |
| `statistics` | 나이대/성별 통계 비교, 변화 추이 | ✅ | |
| `coin` | CoinService(초기 지급·온디맨드 충전·차감·이력) | ✅ | |
| `colleague` | 초대 코드 생성/리프레시, 동료 등록(UNION ALL) | ✅ | |
| `chemistry` | ChemistryService, Lazy Caching(SELECT FOR UPDATE + CountDownLatch), Redis Pub/Sub, `@TransactionalEventListener(AFTER_COMMIT)`, 캐시 복구 스케줄러 | ✅ | service-design §4, sequence-chemistry |
| `notification` | CTI 알림 3종, `GET /notifications`, SSE 스트림(`/notifications/stream`) | ✅ | |
| `user` | 프로필(닉네임·생년·성별·이미지), **회원 탈퇴**(`DELETE /users/me`, `GET /users/me/withdrawal-info`, KakaoUnlinkClient) | ✅ | api-design v0.10 |
| 배치 | 만료 코드·평정 토큰 삭제 스케줄러, 케미 캐시 복구 스케줄러 (`@Scheduled`) | ✅ | |

### 프론트엔드 — 화면별

| 화면 | 경로 | 상태 |
| --- | --- | --- |
| 홈(4-CTA), 서비스 소개 시트, 공유 시트 | `/` | ✅ |
| 검사 응시(시트) | testSheet | ✅ |
| 결과 목록 / 상세 | `/results`, `/results/[id]` | ✅ |
| 로그인 후 결과 저장 | `/save-result` | ✅ |
| 통계·변화 추이 | `/me/insights` | ✅ |
| 프로필 설정 | `/me/profile` | ✅ |
| 코인 | `/me/coins` | ✅ |
| 알림 센터 | `/me/notifications` | ✅ |
| 동료 목록 / 상세 | `/colleagues`, `/colleagues/[id]` | ✅ |
| 케미 목록 / 상세 | `/chemistry`, `/chemistry/[id]` | ✅ |
| 동료 초대 수락 | `/invite/[code]` | ✅ |
| 타인 평정 응시 | `/assessments/[token]` | ✅ |
| 전역 셸(헤더·탭바·알림 드롭다운·토스트·SSE) | (app) 레이아웃 | ✅ |
| 서비스 소개 & 약관 | `/me/about` | ✅ |
| 고객 문의 & FAQ | `/me/help` | ✅ |
| 회원 탈퇴 | `/me/leave` | ✅ |

### 전역·인프라

| 항목 | 상태 |
| --- | --- |
| 개발 환경 (Docker Compose: MySQL + Redis) | ✅ |
| 테스트 체계 (백엔드 UT/ST/IT, 프론트 vitest·시나리오) | ✅ |
| 배포 환경 (AWS S3 스토리지, 운영 리버스 프록시/SSE) | ⬜ |
| 베타 테스터 초대 + 설문 배포 | ⬜ |

---

## §2. 다음 작업 (Backlog)

우선순위 순. 이월·지연 작업은 전부 여기에 모은다.

### P1 — 다음 (마무리)

프론트 페이지 구현이 모두 끝나, 남은 것은 QA·프롬프트 교정·배포·베타다.

- ⬜ **전체 흐름 통합 QA** — 비회원 / 회원 / 타인 평정 / 케미 시나리오 end-to-end
  (설계·시드 규약: `common/qa-test-design.md`)
- ⬜ **배포 전 — DISC 검사 결과 보고서 프롬프트 교정** — 사용자별 `{닉네임}님`으로 치환 가능한 구조로.
  현재 `disc_cache.report`는 이름 미포함 원문을 버킷 단위로 공유하므로, 렌더링 시점에 이름을
  삽입할 자리를 프롬프트가 일관되게 만들어줘야 한다
- ⬜ **배포 전 — 케미 보고서 프롬프트 교정** — 프롬프트 캐싱(`cache_control` 마커) 활용.
  현재 변수 분리 구조까지만 적용되어 있어 캐싱이 실제로 켜져 있지 않다
- ⬜ **배포 환경 구성** — AWS S3 스토리지 연동(로컬 → S3), 운영 리버스 프록시에서 SSE 압축 제외 설정
- ⬜ **베타 테스터 초대 + 설문조사 배포** (설문 초안 부록 C)

### 알려진 개선 여지 (기능 정상 동작, 우선순위 낮음)

- ⬜ 설계 문서 구조 및 내용 전면 개정
- ⬜ SSE 엔드포인트(`/notifications/stream`)를 범용 실시간 채널 경로(`/events/stream` 등)로 분리 검토
- ⬜ 비회원 결과 화면이 새로고침으로 복원되지 않음 — testSheet 스토어에 persist 미적용,
  `sessionStorage['disc_result']`는 잔존하나 이를 읽어 화면을 복원하는 경로가 없어 24문항
  재응시가 필요. 저장 전 이탈 시 이탈률 요인 (근거: `selfAssessmentSlice.ts:99·107`)
- ⬜ 카카오톡 공유 링크 미표시 — localhost 한계, 배포 후 재검증
- ⬜ 에러 메시지 토스트 전환 잔여분 (`me/profile` 등)

---

## §3. 작업 로그 (Log)

완료 작업을 일자별로 최신순 기록(append-only). 계획·체크리스트는 §1/§2에서 관리하며 여기엔 남기지 않는다.

| 날짜 | 작업 | 산출물 |
| --- | --- | --- |
| 07.15 | 통합 QA 설계 + 수동 QA 문서 통합 + 시드 setup/teardown 자동화 (FK RESTRICT로 깨져 있던 초기화 블록 정정) | qa-test-design.md(신설 — scenario-test-design.md 흡수), qa/teardown.sql, qa/flow-4-chemistry.sql, qa.sh, Makefile, dev_scenario_seed.sql, Entry-Point.md v0.3, unit-test-design.md v0.2, ScoringService Javadoc |
| 07.14 | 프론트 순수 로직 추출 리팩토링(lib 6모듈) + 단위 테스트 체계화(40+케이스) + jsdom 환경 원인규명·Node 20.20.2 고정 | lib/{format,coin,assessment,colleague,chemistry,disc/insights}.ts + 각 __tests__, profile/questions/authStore/toastStore 테스트, 페이지 9종, .nvmrc, unit-test-design.md, Entry-Point.md |
| 07.14 | 계획 문서 전면 개정 + 프론트 정적/보조 3페이지 구현(`/me/about`·`/me/help`·`/me/leave`) | plan.md, me/about·help·leave/page.tsx, useWithdrawal.ts, lib/withdrawal.ts, scenario-test-design.md |
| 07.13 | CLAUDE.md 범용 작업 지침으로 재작성, Entry-Point.md 신설(설계 문서 진입점 규약) | CLAUDE.md, docs/Entry-Point.md |
| 07.12 | 회원 탈퇴 시나리오 테스트 + 누락 테스트 구현·검증, 백엔드 테스트 설계 문서 보강 | UserServiceTest, UserWithdrawIntegrationTest, UserV1ControllerTest, UserTest, 6개 Repository 테스트, test-design.md |
| 07.10 | 회원 탈퇴 백엔드 API 구현 + 탈퇴 정책 확정 문서 개정 | `DELETE /users/me`, `GET /users/me/withdrawal-info`, KakaoUnlinkClient, UserService.withdraw, WithdrawRequest/WithdrawalInfoResponse, 6개 repo 삭제 메서드 / api-design.md v0.10, database-design.md, empty-states.md, screens.yaml, schema.sql |

> 07.06 이전(1~6주차)의 일자별 상세 기록은 부록 A 요약을 참조.

---

## 부록 A. 1~6주차 원 계획 요약 (아카이브)

> 원 계획은 6주(2026.05.23~07.03)였다. 당초 4주 계획이 취업 준비 병행으로 6주로 확장되었고, 실제 실행은 07.06까지 이어졌다. 아래는 주차별 테마·핵심 산출물 요약이며, 07.10 이후 작업은 §3 로그에 기록한다.

| 주차 | 기간 | 테마 | 핵심 산출물 |
| --- | --- | --- | --- |
| 1주차 | 05.23~05.29 | 설계 완성 & 환경 구축 | 서비스 기획서·요구사항·유즈케이스, ERD/DDL(10개 테이블), API 명세(29개), 아키텍처·시퀀스 3종, 와이어프레임 19개, Spring Boot(Java 25)·Next.js·Docker Compose 초기화, ScoringService |
| 2주차 | 05.30~06.05 | 검사 핵심 기능 | 검사 응시 UI(Most/Least 강제선택·셔플), DISC 채점, disc_cache Lazy Caching, Claude API 6섹션 프롬프트, 타인 평정 링크, 결과 확인 화면 |
| 3주차 | 06.06~06.12 | 회원 기능 | 프로필 설정, sessionStorage→`POST /results` 저장, 이력 조회, 나이대/성별 통계·변화 추이, 동료 초대 코드, 동료 등록·알림 |
| 4주차 | 06.13~06.22 | 동료/알림 도메인 + 기술 부채 | ColleagueService, Notification CTI 3종+NotificationService, BusinessException/ErrorCode/ErrorResponse 통합, 응답 DTO record 전환, 테스트 지원 클래스 분리 |
| 5주차 | 06.23~06.29 | 코인·케미·SSE (백엔드+프론트 페어) | CoinService+`/me/coins`, ChemistryService(Lazy Caching·중복 방지·AFTER_COMMIT), SseService+Redis Pub/Sub, chemistry_cache 6,561행 seeding, `/colleagues`·`/chemistry` 화면, Chemistry 도메인 테스트(IT 포함) |
| 6주차 | 06.30~07.06 | 잔여 페이지 + 배치 + 전역 셸 | 전역 셸 통합(헤더·탭바·알림 드롭다운·토스트·SSE), 홈 4-CTA 리뉴얼·공유/소개 시트, `/invite/[code]`·`/assessments/[token]`·`/results/[id]`, 만료 코드/토큰 삭제 스케줄러(@Scheduled), 케미 캐시 복구 스케줄러, 카카오 공유 SDK 연동 |

> 6주차에서 정적/보조 페이지(`/me/about`·`/me/help`·`/me/leave`)와 통합 QA·배포·베타는 미완료로 남았으며, 현재 §2 백로그에서 관리한다.

---

## 부록 B. 리스크 및 대응

| 리스크 | 가능성 | 대응 방안 |
| --- | --- | --- |
| Claude API 응답 지연 (10초 초과) | 중 | @Async 비동기 처리 + SSE 푸시로 대기 시간 제거 |
| 카카오 OAuth 심사 지연 | 저 | 개발용 테스트 앱으로 우선 진행 |
| 설계 변경으로 인한 일정 지연 | 저 | 1주차 핵심 설계 완성 (ERD/API/DDL 완료) |
| 배포 환경 설정 이슈 | 중 | 배포 단계에서 Redis + S3 연동 사전 준비 |
| 베타 테스터 참여율 저조 | 저 | 사전 참여 의사 확인 후 초대 |

---

## 부록 C. 베타 테스트 설문조사 항목 (초안)

MVP 출시 후 주변인 대상으로 수집할 설문 문항이다.

**사용성**
- 검사 문항이 이해하기 쉬웠나요? (5점 척도)
- 검사를 완료하는 데 얼마나 걸렸나요? (객관식)
- 검사 중 불편하거나 막힌 부분이 있었나요? (주관식)

**결과 만족도**
- 분석 결과가 자신을 잘 표현한다고 느끼나요? (5점 척도)
- 강점/약점 설명이 납득되었나요? (5점 척도)
- 타인 평정 결과와 자기 평정 결과를 비교해보셨나요? (Y/N)
- 가장 인상적인 결과 항목은 무엇인가요? (주관식)

**케미 기능**
- 동료 케미 보고서를 사용해보셨나요? (Y/N)
- 케미 보고서가 실제 관계를 잘 반영한다고 느끼나요? (5점 척도)

**전반적 평가**
- 이 서비스를 지인에게 추천하겠나요? (NPS 0~10점)
- 유료 전환 시 얼마까지 지불할 의사가 있나요? (객관식)
- 추가로 원하는 기능이 있다면? (주관식)

---

_본 문서는 개발 진행에 따라 지속적으로 업데이트됩니다._
