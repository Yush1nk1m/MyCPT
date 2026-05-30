/**
 * TestSheet
 *
 * 검사 시트 최상위 조립 컴포넌트.
 * 스텝 분기와 TestCloseDialog 표시만 담당.
 * 모든 상태는 useTestSheet 훅을 통해서만 접근.
 */

"use client";

import { useTestSheet } from "@/hooks/useTestSheet";
import { SheetFrame } from "./SheetFrame";
import { TestCloseDialog } from "./TestCloseDialog";
import { Step1TypeSelect } from "./steps/Step1TypeSelect";
import { Step2Answering } from "./steps/Step2Answering";
import { Step3Result } from "./steps/Step3Result";

export function TestSheet() {
  const {
    isOpen,
    step,
    currentIndex,
    answeredCount,
    currentAnswer,
    isCloseDialogOpen,
    submitStatus,
    result,
    errorMessage,
    close,
    forceClose,
    dismissCloseDialog,
    goToStep,
    handleAnswer,
    goNext,
    goPrev,
  } = useTestSheet();

  return (
    <>
      <SheetFrame
        isOpen={isOpen}
        step={step}
        totalSteps={3}
        title="DISC 검사"
        onClose={close}
      >
        {step === 1 && <Step1TypeSelect onStart={() => goToStep(2)} />}

        {step === 2 && (
          <Step2Answering
            currentIndex={currentIndex}
            answeredCount={answeredCount}
            currentAnswer={currentAnswer}
            onAnswer={handleAnswer}
            onNext={goNext}
            onPrev={goPrev}
          />
        )}

        {step === 3 && (
          <Step3Result
            submitStatus={submitStatus}
            result={result}
            errorMessage={errorMessage}
            onRetry={() => goToStep(2)}
            onClose={forceClose}
          />
        )}
      </SheetFrame>

      {/* 중단 다이얼로그 — SheetFrame 바깥에서 portal처럼 렌더 */}
      {isCloseDialogOpen && (
        <TestCloseDialog
          answeredCount={answeredCount}
          onConfirm={forceClose}
          onDismiss={dismissCloseDialog}
        />
      )}
    </>
  );
}
