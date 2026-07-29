# MyCPT Frontend — 컴포넌트 맵

> 프론트엔드 프로젝트각 파일의 단일 책임과 의존 관계를 기록합니다.
> "이 파일은 무엇을 하는가"에만 답한다.

**last updated**: '26.07.29.

---

## 레이어 구조

```
순수 데이터/함수          lib/disc/
전역 상태                 stores/
컴포넌트용 로직 훅        hooks/
UI 조각 (원자)            components/test/OptionButton, DotProgressBar
UI 조각 (분자)            components/test/QuestionCard, TestCloseDialog
UI 조각 (화면 스텝)       components/test/steps/
UI 조립                   components/test/TestSheet, SheetFrame
```

---

## lib/disc/

| 파일           | 책임                                                                                   | 사이드이펙트 |
| -------------- | -------------------------------------------------------------------------------------- | ------------ |
| `questions.ts` | DISC 24문항 원본 데이터. 순수 상수.                                                    | 없음         |
| `scoring.ts`   | 원점수 계산, 검증, 선택지 셔플. 순수 함수.                                             | 없음         |
| `profile.ts`   | DISC 버킷값으로 유형 프로필 산출. 균형형(모든 축 2) / 유형(최댓값 축) 분기. 순수 함수. | 없음         |

테스트 전략: 입출력이 결정적(deterministic)이므로 단위 테스트로 100% 커버.

---

## stores/

| 파일                | 책임                                                                                                          |
| ------------------- | ------------------------------------------------------------------------------------------------------------- |
| `testSheetStore.ts` | 시트 열림 여부, 스텝, 응답 누적, 제출 상태, 에러 관리. API 호출 포함. handleAnswer(현재 문항 응답 기록) 포함. |

테스트 전략: Zustand 스토어를 직접 import해 액션 → 상태 변화 단위 테스트.
API 호출(`submitScores`)은 `fetch` 모킹으로 격리.

---

## hooks/

| 파일              | 책임                                                                                            |
| ----------------- | ----------------------------------------------------------------------------------------------- | --- |
| `useTestSheet.ts` | 스토어에서 컴포넌트에 필요한 값·핸들러만 추출해 제공. handleAnswer는 스토어 액션을 그대로 노출. |
| `useMe.ts`        | GET /auth/me TanStack Query 훅. 응답을 useAuthStore에 동기화. 인증 상태 초기화 담당.            | -   |
| `useResults.ts`   | GET /results 무한 스크롤 훅. useInfiniteQuery 기반 커서 페이지네이션.                           | -   |

테스트 전략: `renderHook`으로 상태 변화 검증. 스토어는 실제 인스턴스 사용.

---

## 쿼리 키 · 캐시 무효화 규약

전역 `staleTime`은 1분(`app/providers.tsx`). 따라서 **쓰기(mutation) 후 그 결과에 의존하는 쿼리를 명시적으로 무효화하지 않으면 최대 1분간 이전 응답이 재사용된다.** 화면 이동은 캐시를 비우지 않으며, 새로고침만이 QueryClient를 재생성한다.

| 쿼리 키 | 조회 대상 | 이 값을 바꾸는 쓰기 경로 |
| --- | --- | --- |
| `["me"]` | GET /auth/me | 프로필 저장, 탈퇴 |
| `["statistics", "comparison"]` / `["statistics", "trend", days]` | GET /statistics/* | 프로필 저장(생년·성별), 검사 결과 저장 |
| `["results", raterType]` | GET /results | 검사 결과 저장 |
| `["coins", "balance"]` / `["coins", "history"]` | GET /coins, /coins/history | 케미 발행, 온디맨드 충전 |
| `["colleagues"]`, `["chemistry-report*"]`, `["notifications"]` | 각 목록/상세 | 동료 등록·삭제, 케미 발행, SSE 수신 |

⚠️ **코인 잔량의 단일 출처는 `["coins","balance"]`다.** `GET /auth/me` 응답에도 `coins` 필드가 있지만 **UI는 이 값을 읽지 않는다.** 온디맨드 충전은 `GET /coins`에서만 일어나므로(`CoinService.getBalance`), `["me"].coins`는 충전이 반영되지 않은 낡은 값일 수 있다. 코인을 표시하는 화면은 반드시 `useCoinBalance()`를 쓴다. (07.29 헤더 잔량 버그의 원인 — 버그 이력 참조)

규칙:

1. 쓰기 성공 직후 위 표에서 영향받는 키를 `invalidateQueries`로 무효화한다.
2. 응답 본문으로 정확한 최신값을 알 수 있으면 `setQueryData`를 함께 써서 왕복을 줄인다(예: 프로필 저장의 `["me"]`).
3. 접두 매칭을 활용한다 — `["statistics"]` 무효화는 comparison·trend를, `["coins"]` 무효화는 balance·history를 모두 포함한다.
4. **한 값이 여러 키에 중복 존재하는지 먼저 확인한다** — 소비처가 읽는 키를 빠뜨리면 화면별로 갱신 여부가 갈린다.
5. **새 쿼리 키나 새 쓰기 경로를 추가하면 이 표를 갱신한다.**

---

## components/disc/

| 파일                 | 책임                                                                                                                              | props              |
| -------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ------------------ |
| `TypePill.tsx`       | DISC 유형 칩. 4축 색상 자동 적용. `mini` prop으로 소형 변형 지원.                                                                 | `type`, `mini?`    |
| `BalancedPill.tsx`   | 균형형 칩. 모든 버킷이 2일 때 사용. 보라색 계열.                                                                                  | 없음               |
| `DiscBarsLarge.tsx`  | DISC 4축 막대 시각화. 버킷(1~3) 기준 높이 계산. `size` prop으로 `lg`/`md` 분기.                                                   | `buckets`, `size?` |
| `ReportMarkdown.tsx` | DISC 보고서 마크다운 렌더러. h2/p/ul/li/blockquote/strong/hr 스타일 정의. Step3Result, 결과 상세, 케미 보고서 상세에서 공통 사용. | `report: string`   |

---

## components/test/

| 파일                  | 책임                                                                                       | props                                               |
| --------------------- | ------------------------------------------------------------------------------------------ | --------------------------------------------------- |
| `SheetFrame.tsx`      | 시트 껍데기. 핸들, 헤더(✕·제목·Step N/3), Framer Motion 슬라이드인/아웃.                   | `step`, `onClose`, `children`                       |
| `DotProgressBar.tsx`  | 24개 dot 진행 시각화. 완료/현재/미응시 상태 구분.                                          | `total`, `currentIndex`, `answeredCount`            |
| `OptionButton.tsx`    | 선택지 버튼 하나. selected/disabled/default 세 가지 시각 상태.                             | `text`, `state`, `onClick`                          |
| `QuestionCard.tsx`    | 문항 도입부 + Most 섹션 + Least 섹션 조립. 셔플 처리. Most 선택 시 Least 동일 옵션 비활성. | `question`, `initialAnswer`, `onAnswer`             |
| `TestCloseDialog.tsx` | 중단 확인 다이얼로그. 스크림 + 카드. 답한 문항 수 표시.                                    | `answeredCount`, `onConfirm`, `onDismiss`           |
| `TestSheet.tsx`       | 시트 조립. SheetFrame 안에서 스텝 분기만 담당.                                             | 없음 (스토어 직접 구독 금지 → useTestSheet 훅 경유) |

### steps/

| 파일                  | 책임                                                                 |
| --------------------- | -------------------------------------------------------------------- |
| `Step1TypeSelect.tsx` | 유형 선택 화면. MVP는 DISC 1개 고정.                                 |
| `Step2Answering.tsx`  | 응시 화면. DotProgressBar + QuestionCard + 이전/다음 버튼 조립.      |
| `Step3Result.tsx`     | 상태 분기 라우터만. submitting/error/done을 각 하위 컴포넌트로 위임. |

#### step3/

| 파일                  | 책임                                                                                                              |
| --------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `SubmittingState.tsx` | 로딩 스피너. POST /results/score 응답 대기 중 표시.                                                               |
| `ErrorState.tsx`      | 에러 메시지 + 재시도 버튼.                                                                                        |
| `DoneState.tsx`       | 결과 표시 + 회원/비회원 분기. 회원이면 마운트 시 POST /results 자동 저장. 저장 상태 칩(saving/saved/failed) 관리. |
| `GuestCta.tsx`        | 비회원 CTA. 카카오 저장 링크 + 닫기.                                                                              |
| `MemberCta.tsx`       | 회원 CTA. 저장 완료 후 "결과 상세로 가기" 활성화.                                                                 |

---

## 테스트 우선순위

| 우선순위 | 대상                                | 이유                              |
| -------- | ----------------------------------- | --------------------------------- |
| 🔴 필수  | `scoring.ts`                        | 채점 오류 = 서비스 신뢰도 직결    |
| 🔴 필수  | `testSheetStore.ts`                 | 핵심 흐름 전체를 관장             |
| 🟡 권장  | `OptionButton`, `QuestionCard`      | Most/Least 비활성 로직 복잡       |
| 🟡 권장  | `useTestSheet`                      | 스토어-컴포넌트 인터페이스 안정성 |
| 🟢 선택  | `DotProgressBar`, `TestCloseDialog` | 단순 렌더링, 스냅샷으로 충분      |

---

## 버그 이력

| 날짜  | 증상                                          | 원인                                                   | 해결                                                 |
| ----- | --------------------------------------------- | ------------------------------------------------------ | ---------------------------------------------------- |
| 05.30 | 문항 이동 시 이전 답 잔류                     | QuestionCard에 key 누락                                | key={currentIndex} 추가, useEffect 제거              |
| 05.30 | 선택지 가로 나열                              | QuestionCard 내부 flex-col 오타                        | 오타 수정                                            |
| 05.30 | answeredCount 테스트 실패                     | useCallback 클로저가 currentIndex 캡처                 | handleAnswer를 스토어로 이동                         |
| 06.09 | 균형형(2/2/2/2) 사용자가 주도형으로 표시      | getTopType이 페이지별로 중복 정의되어 균형형 처리 누락 | getDiscProfile을 lib/disc/profile.ts로 추출하여 공유 |
| 06.09 | DoneState에서 로그인 상태에도 GuestCta 렌더링 | useAuthStore 초기값 false로 인한 타이밍 문제           | DoneState에서 useMe() 직접 호출로 교체               |
| 07.29 | 프로필 저장 후 /me/insights가 "생년·성별 입력 필요" 배너 유지 (새로고침해야 반영) | 저장 시 ["statistics"] 캐시를 무효화하지 않아 staleTime 1분 동안 average=null 응답 재사용 | 프로필·검사 결과 저장 경로에 invalidateQueries 추가, 캐시 무효화 규약 문서화 |
| 07.29 | 케미 발행 후 헤더 코인 잔량이 그대로 (새로고침해야 반영) | coins가 ["me"]·["coins","balance"] 두 캐시에 중복 존재. 발행은 후자만 무효화하는데 헤더는 전자를 읽음. 게다가 /auth/me는 온디맨드 충전을 하지 않아 충전분도 놓침 | 헤더·/me 화면을 useCoinBalance()로 이전해 coins 출처를 단일화 |
