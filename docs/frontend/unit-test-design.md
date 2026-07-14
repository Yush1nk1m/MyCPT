# MyCPT 프론트엔드 단위 테스트 설계

**문서 버전**: v0.1
**작성일**: '26.07.14.
**작성자**: 김유신

## 변경 이력

| 버전 | 변경 내용 | 날짜       |
| ---- | --------- | ---------- |
| v0.1 | 초안 작성 — 순수 로직 추출(`lib/*` 6모듈) + 단위 테스트 체계화 | '26.07.14. |

---

## 1. 목적 · 범위 · 실행

### 목적

여러 `page.tsx`(`"use client"` + default export 컴포넌트)에 흩어져 있던 순수 헬퍼 함수를
`src/lib/*`로 추출해 (a) 단위 테스트 가능하게 하고, (b) 중복 구현을 제거하고, (c) 페이지
컴포넌트는 렌더링에만 집중하도록 정리한다.

### 범위

이 문서는 **순수 로직 단위 테스트**(`src/lib/**`, `src/stores/**`, `src/hooks/**` 중 순수
훅)만 다룬다. 화면 단위 수동 QA 시나리오는 `frontend/scenario-test-design.md`를 참조한다.

### 실행

- 로컬: `cd frontend && npx vitest run` — **Node ≥ 20.19 필요**(`require(ESM)` 미지원 시
  `ERR_REQUIRE_ESM`로 실패). 저장소 정본은 `frontend/.nvmrc` = `20.20.2`.
- 컨테이너(레이어드 대안): `cd infra/docker/dev && make test-front` — 기존 `node:22` 프론트
  컨테이너를 1회성으로 재사용해 `npx vitest run` 실행. 새 Dockerfile·CI 파이프라인은 두지
  않는다(저장소에 아직 CI가 없음). 추후 CI 도입 시 `setup-node: node-version-file:
  frontend/.nvmrc` 한 줄로 통합 가능.

---

## 2. 대상 모듈 인벤토리

`vitest.config.ts`의 커버리지 include 대상(`src/lib/**`, `src/stores/**`, `src/hooks/**`)과
일치한다.

| 모듈 | 테스트 파일 | 비고 |
| --- | --- | --- |
| `lib/format.ts` | `lib/__tests__/format.test.ts` | `results/page`·`results/[id]`·`chemistry/page`·`me/notifications/page`·`me/coins/page`에서 추출 |
| `lib/coin.ts` | `lib/__tests__/coin.test.ts` | `me/coins/page`·`colleagues/[id]/page`에서 추출. `reasonLabel`은 `tx`→`reason: string`으로 순수화 |
| `lib/assessment.ts` | `lib/__tests__/assessment.test.ts` | `assessments/[token]/page`에서 추출 |
| `lib/colleague.ts` | `lib/__tests__/colleague.test.ts` | `colleagues/page`에서 추출(`formatCode`만; 동일 페이지의 `errorMessage`는 컴포넌트 결합도가 높아 잔류) |
| `lib/chemistry.ts` | `lib/__tests__/chemistry.test.ts` | `chemistry/page`에서 추출. `myRole` 유니온은 `ChemistryReportSummary` 실제 타입 기준 `"REQUESTER" \| "PARTNER"` |
| `lib/disc/insights.ts` | `lib/disc/__tests__/insights.test.ts` | `me/insights/page`의 `InsightSentence` 편차 계산 로직 추출 |
| `lib/disc/profile.ts` (기존) | `lib/disc/__tests__/profile.test.ts` | 기존 export 모듈, 이번에 테스트 신규 추가 |
| `lib/disc/questions.ts` (기존) | `lib/disc/__tests__/questions.test.ts` | `toRaterStem` + `DISC_QUESTIONS` 불변식, 이번에 테스트 신규 추가 |
| `stores/authStore.ts` (기존) | `stores/__tests__/authStore.test.ts` | 이번에 테스트 신규 추가 |
| `stores/toastStore.ts` (기존) | `stores/__tests__/toastStore.test.ts` | fake timers 사용, 이번에 테스트 신규 추가 |

이미 테스트됨(변경 없음): `lib/withdrawal.ts`, `lib/disc/scoring.ts`, `stores/testSheetStore.ts`,
`hooks/useTestSheet`, `components/test/OptionButton`, `components/test/TestCloseDialog`.

---

## 3. 테스트 케이스 카탈로그

명명: `UT-[모듈]-[행위]-[상황]` (`common/test-process.md` Step2 ID 포맷을 프론트에 적용).
실제 코드의 `describe`/`it`은 §4 규칙을 따른다.

**format.ts**
- UT-format-formatDateDot-ISO앞10자변환 / -하이픈을점으로
- UT-format-formatDateTime-날짜시각조합 / -분단위영점패딩
- UT-format-formatDateKo-한국어긴포맷
- UT-format-formatRelative-1분미만-방금전 / -분 / -시간 / -일 / -7일이상-절대날짜(now 주입)

**coin.ts**
- UT-coin-calcRemaining-양수차이-시분초 / -diff0이하-영패딩 / -자릿수패딩
- UT-coin-reasonLabel-3종매핑 / -미지정코드-passthrough
- UT-coin-formatNextCharge-null-만충 / -diff0이하-곧충전 / -양수-약N시간M분후

**assessment.ts**
- UT-assessment-errorMessage-4코드매핑 / -미지정-기본메시지

**colleague.ts**
- UT-colleague-formatCode-소문자대문자화 / -특수문자제거 / -8자절단

**chemistry.ts**
- UT-chemistry-peerLabel-REQUESTER-상대닉 / -PARTNER-요청자닉

**disc/insights.ts**
- UT-insights-bucketToPercent-3은100 / -1은33 / -반올림
- UT-insights-dominantDeviation-최대편차축 / -음수방향-낮아요 / -전축동일0-null

**disc/profile.ts (getDiscProfile)**
- UT-profile-전축2-balanced / -단일최대-typed단일 / -공동최대-typed복수

**disc/questions.ts**
- UT-questions-toRaterStem-1인칭치환 / -Q22불규칙override / -비매칭패턴-원문+warn
- UT-questions-DISC데이터-24문항 / -각문항4보기DISC1개씩 / -optionId포맷

**authStore.ts**
- UT-authStore-setUser-isAuthenticated true / -setUser-null이면 false / -clear-초기화

**toastStore.ts** (`vi.useFakeTimers()`)
- UT-toastStore-show-메시지설정 / -action없으면2초후자동hide / -action있으면5초 /
  -duration명시우선 / -연속show-이전타이머취소 / -hide-즉시초기화

---

## 4. 명명 · 작성 규칙

- `describe` = 테스트 대상(함수/스토어명), `it` = 한국어 행위 서술. 기존 프론트 테스트
  관례(`lib/__tests__/withdrawal.test.ts`, `lib/disc/__tests__/scoring.test.ts`)를 그대로
  따른다.
- 비결정 입력(현재 시각 등)에 의존하는 함수는 `now?: number` 선택 인자로 주입 가능하게
  하고, 기본값은 `Date.now()`로 둔다. 테스트에서는 항상 고정된 `now` 값을 넘겨 결정적으로
  검증한다.
- `setTimeout`/`setInterval`에 의존하는 스토어(`toastStore`)는 `vi.useFakeTimers()` +
  `vi.advanceTimersByTime()`으로 검증하고, `afterEach`에서 `vi.useRealTimers()`로 복원한다.
- zustand 스토어 테스트는 `act()`로 상태 변경을 감싸고, `beforeEach`에서 스토어를 초기
  상태로 리셋한다(`clear()`/`hide()` 등).
- 순수 함수는 mock 없이 입력→출력만 검증한다. DOM/네트워크가 필요한 로직은 이 문서의
  대상이 아니다(컴포넌트 테스트 또는 시나리오 테스트로 분리).
