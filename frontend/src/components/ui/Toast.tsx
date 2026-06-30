"use client";

import { useToastStore } from "@/stores/toastStore";

export function ToastContainer() {
  const message = useToastStore((s) => s.message);

  if (!message) return null;

  return (
    <div className="fixed bottom-24 left-1/2 -translate-x-1/2 z-50 pointer-events-none">
      <div className="bg-[var(--ink)] text-white text-[13px] font-medium px-5 py-2.5 rounded-full shadow-lg whitespace-nowrap">
        {message}
      </div>
    </div>
  );
}
