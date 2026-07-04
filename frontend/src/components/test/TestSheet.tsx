"use client";

import { useTestSheet } from "@/hooks/useTestSheet";
import { SheetFrame } from "./SheetFrame";
import { TestCloseDialog } from "./TestCloseDialog";
import { Step1TypeSelect } from "./steps/Step1TypeSelect";
import { Step2Answering } from "./steps/Step2Answering";
import { Step3Result } from "./steps/Step3Result";
import { ShareStep2Label } from "./steps/share/ShareStep2Label";
import { ShareStep3Link } from "./steps/share/ShareStep3Link";

export function TestSheet() {
  const {
    isOpen,
    step,
    mode,
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
        title={mode === "share" ? "친구에게 물어보기" : "DISC 검사"}
        onClose={close}
      >
        {step === 1 && <Step1TypeSelect onStart={() => goToStep(2)} />}

        {step === 2 && mode === "self" && (
          <Step2Answering
            currentIndex={currentIndex}
            answeredCount={answeredCount}
            currentAnswer={currentAnswer}
            onAnswer={handleAnswer}
            onNext={goNext}
            onPrev={goPrev}
          />
        )}
        {step === 2 && mode === "share" && <ShareStep2Label />}

        {step === 3 && mode === "self" && (
          <Step3Result
            submitStatus={submitStatus}
            result={result}
            errorMessage={errorMessage}
            onRetry={() => goToStep(2)}
            onClose={forceClose}
          />
        )}
        {step === 3 && mode === "share" && <ShareStep3Link />}
      </SheetFrame>

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
