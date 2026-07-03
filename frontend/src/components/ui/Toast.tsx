"use client";

import { useToastStore } from "@/stores/toastStore";

export function ToastContainer() {
  const message = useToastStore((s) => s.message);
  const action = useToastStore((s) => s.action);
  const hide = useToastStore((s) => s.hide);

  if (!message) return null;

  return (
    <div className="fixed bottom-24 left-1/2 -translate-x-1/2 z-50 pointer-events-none">
      <div className="flex items-center gap-2 bg-ink text-white text-[13px] font-medium px-5 py-2.5 rounded-full shadow-lg whitespace-nowrap">
        <span>{message}</span>
        {action && (
          <button
            onClick={() => {
              action.onClick();
              hide();
            }}
            className="pointer-events-auto px-3 py-1 rounded-full text-[12px] font-bold"
            style={{ background: "var(--kakao)", color: "var(--kakao-ink)" }}
          >
            {action.label}
          </button>
        )}
      </div>
    </div>
  );
}
