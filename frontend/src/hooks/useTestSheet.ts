/**
 * useTestSheet
 *
 * testSheetStore를 컴포넌트 인터페이스로 변환하는 훅.
 *
 * 목적:
 *   컴포넌트가 스토어 구조에 직접 의존하지 않도록 격리.
 *   스토어 내부 구조가 바뀌어도 이 훅만 수정하면 컴포넌트는 무영향.
 *   백엔드의 Service 레이어가 Repository 구조를 Controller에 노출하지
 *   않는 것과 같은 맥락.
 *
 * TestSheet, Step2Answering 등은 이 훅을 통해서만 스토어에 접근.
 */

import { useTestSheetStore } from "@/stores/testSheetStore";
import { TOTAL_QUESTIONS } from "@/lib/disc/questions";

export function useTestSheet() {
  const store = useTestSheetStore();

  // 현재 문항의 기존 응답 (이전 문항으로 돌아왔을 때 복원용)
  const currentAnswer = store.answers[store.currentIndex];

  // 현재 문항에 Most + Least 모두 선택됐는지
  const isCurrentAnswered = Boolean(
    currentAnswer?.most && currentAnswer?.least,
  );

  // 완료한 문항 수
  const answeredCount = Object.keys(store.answers).length;

  return {
    // 시트 상태
    isOpen: store.isOpen,
    step: store.step,
    mode: store.mode,

    // 응시 진행
    currentIndex: store.currentIndex,
    answeredCount,
    totalQuestions: TOTAL_QUESTIONS,
    currentAnswer,
    isCurrentAnswered,
    isFirstQuestion: store.currentIndex === 0,
    isLastQuestion: store.currentIndex === TOTAL_QUESTIONS - 1,

    // 중단 다이얼로그
    isCloseDialogOpen: store.isCloseDialogOpen,

    // 제출
    submitStatus: store.submitStatus,
    result: store.result,
    errorMessage: store.errorMessage,

    // 액션
    open: store.open,
    close: store.close,
    forceClose: store.forceClose,
    dismissCloseDialog: store.dismissCloseDialog,
    goToStep: store.goToStep,
    handleAnswer: store.handleAnswer,
    goNext: store.goNext,
    goPrev: store.goPrev,
  };
}
