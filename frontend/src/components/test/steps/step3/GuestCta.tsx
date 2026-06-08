"use client";

interface GuestCtaProps {
  onClose: () => void;
}

export function GuestCta({ onClose }: GuestCtaProps) {
  function handleKakaoLogin() {
    const returnTo = sessionStorage.getItem("disc_result")
      ? "/save-result"
      : "/";
    window.location.href = `/api/v1/auth/kakao?returnTo=${returnTo}`;
  }

  return (
    <div className="flex flex-col gap-3 px-5 py-5 bg-paper border-t border-line-soft">
      <button
        onClick={handleKakaoLogin}
        className="w-full py-3 rounded-pill flex items-center justify-center gap-2 font-semibold text-sm"
        style={{ background: "var(--kakao)", color: "var(--kakao-ink)" }}
      >
        <span>💬</span>
        <span>카카오로 결과 저장하기</span>
      </button>
      <button onClick={onClose} className="w-full py-3 text-sm text-ink-soft">
        저장하지 않고 닫기
      </button>
    </div>
  );
}
