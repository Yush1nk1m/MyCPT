/**
 * Step3Result
 *
 * 검사 시트 Step 3 — 상태 분기 라우터.
 * screens.yaml: test-sheet-step3-guest / test-sheet-step3-member
 *
 * submitting → SubmittingState
 * error      → ErrorState
 * done       → DoneState (회원/비회원 분기는 DoneState 내부)
 */

import type { ScoreResult } from "@/stores/testSheetStore";
import { SubmittingState } from "./step3/SubmittingState";
import { ErrorState } from "./step3/ErrorState";
import { DoneState } from "./step3/DoneState";

interface Step3ResultProps {
  submitStatus: "idle" | "submitting" | "done" | "error";
  result: ScoreResult | null;
  errorMessage: string | null;
  onRetry: () => void;
  onClose: () => void;
}

export function Step3Result({
  submitStatus,
  result,
  errorMessage,
  onRetry,
  onClose,
}: Step3ResultProps) {
  if (submitStatus === "submitting") return <SubmittingState />;
  if (submitStatus === "error")
    return <ErrorState message={errorMessage} onRetry={onRetry} />;
  if (submitStatus === "done" && result)
    return <DoneState result={result} onClose={onClose} />;
  return null;
}
