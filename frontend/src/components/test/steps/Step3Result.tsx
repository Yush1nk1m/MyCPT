/**
 * Step3Result
 *
 * 검사 시트 Step 3 — 결과 화면 (screens.yaml: test-sheet-step3-guest/member).
 *
 * 세 가지 상태 분기:
 *   submitting — 로딩 스피너 (POST /results/score 응답 대기)
 *   error      — 에러 메시지 + 재시도 버튼
 *   done       — 결과 표시 (06.05 결과 상세 화면 구현 전까지 플레이스홀더)
 *
 * done 상태의 회원/비회원 분기(자동저장 vs 카카오 CTA)는
 * 06.05 결과 화면 구현 시 authStore 연동과 함께 완성.
 */

import type { ScoreResult } from "@/stores/testSheetStore";

interface Step3ResultProps {
  submitStatus: "idle" | "submitting" | "done" | "error";
  result: ScoreResult | null;
  errorMessage: string | null;
  onRetry: () => void; // 에러 시 Step 2로 복귀
  onClose: () => void; // 닫기
}

export function Step3Result({
  submitStatus,
  result,
  errorMessage,
  onRetry,
  onClose,
}: Step3ResultProps) {
  if (submitStatus === "submitting") {
    return <SubmittingState />;
  }

  if (submitStatus === "error") {
    return <ErrorState message={errorMessage} onRetry={onRetry} />;
  }

  if (submitStatus === "done" && result) {
    return <DoneState result={result} onClose={onClose} />;
  }

  return null;
}

// ─── 상태별 UI ────────────────────────────────────────────

function SubmittingState() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4">
      <div className="w-10 h-10 rounded-full border-4 border-line border-t-accent animate-spin" />
      <p className="text-sm text-ink-soft">결과를 분석하고 있어요…</p>
    </div>
  );
}

function ErrorState({
  message,
  onRetry,
}: {
  message: string | null;
  onRetry: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4 text-center px-4">
      <span className="text-4xl">😥</span>
      <p className="text-base font-semibold text-ink">
        결과를 불러오지 못했어요
      </p>
      <p className="text-sm text-ink-soft">{message}</p>
      <button
        onClick={onRetry}
        className="px-6 py-3 rounded-pill bg-ink text-white font-semibold text-sm"
      >
        다시 시도하기
      </button>
    </div>
  );
}

function DoneState({
  result,
  onClose,
}: {
  result: ScoreResult;
  onClose: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col gap-4 pt-4">
      <p className="text-center text-2xl font-black text-ink">
        검사가 끝났어요! 🎉
      </p>
      {/* TODO: 06.05 결과 상세 화면 구현 시 DiscBarsLarge + 보고서 섹션으로 교체 */}
      <p className="text-center text-sm text-ink-soft">
        결과 화면은 06.05 구현 예정입니다.
      </p>
      <pre className="bg-paper-2 rounded-lg p-3 text-xs font-mono text-ink-soft overflow-auto">
        {JSON.stringify(result, null, 2)}
      </pre>
      <button
        onClick={onClose}
        className="w-full py-3 rounded-pill border border-line text-ink font-semibold"
      >
        닫기
      </button>
    </div>
  );
}
