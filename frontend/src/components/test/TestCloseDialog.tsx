/**
 * TestCloseDialog
 *
 * 응시 중단 확인 다이얼로그 (screens.yaml: test-close-dialog).
 * Step 2 이상에서 ✕ 탭 시 표시.
 *
 * - "중단하고 닫기" → forceClose (danger 스타일)
 * - "계속 응시하기" → dismissCloseDialog (secondary)
 * - 답한 문항 수 / 전체 문항 수 표시
 */

import { TOTAL_QUESTIONS } from "@/lib/disc/questions";

interface TestCloseDialogProps {
  answeredCount: number;
  onConfirm: () => void; // "중단하고 닫기"
  onDismiss: () => void; // "계속 응시하기"
}

export function TestCloseDialog({
  answeredCount,
  onConfirm,
  onDismiss,
}: TestCloseDialogProps) {
  return (
    /* 스크림 — 탭하면 다이얼로그 닫기 (계속 응시) */
    <div
      className="absolute inset-0 flex items-center justify-center px-6"
      style={{ background: "var(--scrim)", zIndex: 50 }}
      onClick={onDismiss}
    >
      <div
        className="w-full bg-white rounded-xl p-6 flex flex-col gap-5 shadow-dialog"
        onClick={(e) => e.stopPropagation()} // 카드 내부 클릭은 전파 차단
      >
        {/* 아이콘 */}
        <div className="text-3xl text-center">⚠️</div>

        {/* 제목 */}
        <div className="flex flex-col gap-1 text-center">
          <h3 className="text-lg font-bold text-ink">응시를 중단할까요?</h3>
          <p className="text-sm text-ink-soft leading-relaxed">
            지금까지 답한{" "}
            <span className="font-bold text-ink">
              {answeredCount} / {TOTAL_QUESTIONS}
            </span>{" "}
            문항은 저장되지 않아요.
            <br />
            처음부터 다시 시작해야 합니다.
          </p>
        </div>

        {/* 버튼 */}
        <div className="flex flex-col gap-2">
          <button
            onClick={onConfirm}
            className="w-full py-3 rounded-pill bg-danger text-white font-semibold text-base transition-opacity active:opacity-80"
          >
            중단하고 닫기
          </button>
          <button
            onClick={onDismiss}
            className="w-full py-3 rounded-pill border border-line bg-white text-ink font-semibold text-base transition-colors hover:bg-paper-2 active:bg-paper-3"
          >
            계속 응시하기
          </button>
        </div>
      </div>
    </div>
  );
}
