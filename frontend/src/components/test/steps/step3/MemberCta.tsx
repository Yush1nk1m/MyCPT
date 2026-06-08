"use client";

import { useRouter } from "next/navigation";

type SaveStatus = "idle" | "saving" | "saved" | "failed";

interface MemberCtaProps {
  saveStatus: SaveStatus;
  resultId: number | null;
  onClose: () => void;
}

export function MemberCta({ saveStatus, resultId, onClose }: MemberCtaProps) {
  const router = useRouter();

  return (
    <div className="flex flex-col gap-3 px-5 py-5 bg-paper border-t border-line-soft">
      <button
        onClick={() => {
          onClose();
          if (resultId) router.push(`/results/${resultId}`);
        }}
        disabled={saveStatus !== "saved"}
        className="w-full py-3.5 rounded-pill bg-accent text-white font-bold text-sm disabled:opacity-40 disabled:cursor-not-allowed"
      >
        결과 상세로 가기
      </button>
      <button onClick={onClose} className="w-full py-3 text-sm text-ink-soft">
        닫기
      </button>
    </div>
  );
}
