"use client";

import { TestSheet } from "@/components/test/TestSheet";
import { useTestSheetStore } from "@/stores/testSheetStore";

export default function Home() {
  const open = useTestSheetStore((s) => s.open);

  return (
    <div className="min-h-screen bg-paper flex flex-col items-center justify-center gap-4 p-6">
      <p className="text-xl font-bold text-ink">MyCPT</p>
      <button
        onClick={() => open("self")}
        className="px-6 py-3 rounded-pill bg-ink text-white font-semibold text-base"
      >
        나는 누구일까? (검사 시작)
      </button>

      {/* 검사 시트 — 전역 상태로 열고 닫음 */}
      <TestSheet />
    </div>
  );
}
