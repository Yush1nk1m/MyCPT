"use client";

import { useTestSheetStore } from "@/stores/testSheetStore";

export function ShareStep2Label() {
  const shareLabel = useTestSheetStore((s) => s.shareLabel);
  const setShareLabel = useTestSheetStore((s) => s.setShareLabel);
  const createShareLink = useTestSheetStore((s) => s.createShareLink);

  return (
    <div className="flex-1 flex flex-col gap-4 px-5 pt-4">
      <div className="flex flex-col gap-1.5">
        <p className="text-base font-bold text-ink">
          이 사람을 뭐라고 부를까요?
        </p>
        <p className="text-xs text-ink-soft">
          링크를 받는 분에게는 안 보여요. 결과 이력에서 구분할 때만 사용해요.
        </p>
      </div>

      <input
        value={shareLabel}
        onChange={(e) => setShareLabel(e.target.value.slice(0, 20))}
        placeholder="예: 여자친구, 팀장님"
        autoFocus
        className="px-4 py-3 rounded-xl border border-line text-sm text-ink outline-none focus:border-ink"
      />
      <span className="text-[11px] text-ink-faint text-right">
        {shareLabel.length}/20
      </span>

      <button
        onClick={createShareLink}
        disabled={!shareLabel.trim()}
        className="mt-auto mb-4 py-3.5 rounded-2xl font-bold text-sm bg-ink text-white disabled:opacity-30"
      >
        링크 만들기
      </button>
    </div>
  );
}
