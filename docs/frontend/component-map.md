# MyCPT Frontend — 컴포넌트 맵

> 프론트엔드 프로젝트각 파일의 단일 책임과 의존 관계를 기록합니다.
> "이 파일은 무엇을 하는가"에만 답한다.

**last updated**: 2026-05-30

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

| 파일           | 책임                                       | 사이드이펙트 |
| -------------- | ------------------------------------------ | ------------ |
| `questions.ts` | DISC 24문항 원본 데이터. 순수 상수.        | 없음         |
| `scoring.ts`   | 원점수 계산, 검증, 선택지 셔플. 순수 함수. | 없음         |

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
| ----------------- | ----------------------------------------------------------------------------------------------- |
| `useTestSheet.ts` | 스토어에서 컴포넌트에 필요한 값·핸들러만 추출해 제공. handleAnswer는 스토어 액션을 그대로 노출. |

테스트 전략: `renderHook`으로 상태 변화 검증. 스토어는 실제 인스턴스 사용.

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

| 파일                  | 책임                                                            |
| --------------------- | --------------------------------------------------------------- |
| `Step1TypeSelect.tsx` | 유형 선택 화면. MVP는 DISC 1개 고정.                            |
| `Step2Answering.tsx`  | 응시 화면. DotProgressBar + QuestionCard + 이전/다음 버튼 조립. |
| `Step3Result.tsx`     | 결과 화면. submitting/done/error 상태 분기.                     |

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

| 날짜  | 증상                      | 원인                                   | 해결                                    |
| ----- | ------------------------- | -------------------------------------- | --------------------------------------- |
| 05.30 | 문항 이동 시 이전 답 잔류 | QuestionCard에 key 누락                | key={currentIndex} 추가, useEffect 제거 |
| 05.30 | 선택지 가로 나열          | QuestionCard 내부 flex-col 오타        | 오타 수정                               |
| 05.30 | answeredCount 테스트 실패 | useCallback 클로저가 currentIndex 캡처 | handleAnswer를 스토어로 이동            |
