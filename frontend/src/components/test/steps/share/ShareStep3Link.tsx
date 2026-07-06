"use client";

import { useTestSheetStore } from "@/stores/testSheetStore";
import { useMe } from "@/hooks/useMe";
import { useToast } from "../../../../hooks/useToast";
import { useKakaoShare } from "../../../../hooks/useKakaoShare";

export function ShareStep3Link() {
  const shareStatus = useTestSheetStore((s) => s.shareStatus);
  const shareToken = useTestSheetStore((s) => s.shareToken);
  const shareError = useTestSheetStore((s) => s.shareError);
  const createShareLink = useTestSheetStore((s) => s.createShareLink);
  const { data: me } = useMe();
  const showToast = useToast();
  const { share } = useKakaoShare();

  const url = shareToken
    ? `${window.location.origin}/assessments/${shareToken}`
    : "";

  function handleCopy() {
    navigator.clipboard.writeText(url);
    showToast("링크를 복사했어요");
  }

  function handleKakaoShare() {
    share({
      title: `${me?.nickname ?? "친구"}님은 어떤 사람일까요?`,
      description: "5분이면 끝나는 DISC 성향 평가로 답해주세요.",
      imageUrl: `${window.location.origin}/og-share.png`,
      url,
      buttonTitle: "평가하러 가기",
    });
  }

  if (shareStatus === "creating") {
    return (
      <div className="flex-1 flex flex-col items-center justify-center gap-3">
        <div className="w-8 h-8 rounded-full border-4 border-line border-t-accent animate-spin" />
        <p className="text-sm text-ink-soft">링크를 만들고 있어요…</p>
      </div>
    );
  }

  if (shareStatus === "error") {
    return (
      <div className="flex-1 flex flex-col items-center justify-center gap-3">
        <p className="text-sm text-ink-soft">{shareError}</p>
        <button
          onClick={createShareLink}
          className="px-4 py-2 rounded-pill bg-ink text-white text-xs font-bold"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col gap-4 px-5 pt-4">
      <p className="text-sm font-bold text-ink">링크가 준비됐어요</p>
      <div className="px-3 py-2.5 rounded-xl border border-line bg-paper-2 text-[12px] text-ink-soft break-all">
        {url}
      </div>
      <div className="flex gap-2">
        <button
          onClick={handleCopy}
          className="flex-1 py-2.5 rounded-xl border border-line text-ink-soft text-[13px] font-semibold"
        >
          복사
        </button>
        <button
          onClick={handleKakaoShare}
          className="flex-1 py-2.5 rounded-xl text-[13px] font-bold"
          style={{ background: "var(--kakao)", color: "var(--kakao-ink)" }}
        >
          💬 카카오톡 공유
        </button>
      </div>
      <p className="text-[11px] text-ink-faint text-center mt-2">
        링크는 24시간 동안, 1회만 사용할 수 있어요
      </p>
    </div>
  );
}
