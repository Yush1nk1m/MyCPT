/**
 * SheetFrame
 *
 * 검사/공유 풀스크린 시트의 공통 껍데기.
 * - 메인이 희미하게 보이는 상단 여백
 * - 핸들 바
 * - 헤더 (✕ 버튼 · 제목 · Step N/3)
 * - Framer Motion 슬라이드인/아웃 (아래 → 위)
 *
 * TestSheet, ShareSheet 등 시트 계열 컴포넌트가 공통으로 사용.
 * 내부 스크롤은 children을 감싸는 컨테이너가 담당.
 */

"use client";

import { AnimatePresence, motion } from "framer-motion";

interface SheetFrameProps {
  isOpen: boolean;
  step: number;
  totalSteps: number;
  title: string;
  onClose: () => void;
  children: React.ReactNode;
}

export function SheetFrame({
  isOpen,
  step,
  totalSteps,
  title,
  onClose,
  children,
}: SheetFrameProps) {
  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          className="fixed inset-0 z-40 flex flex-col"
          initial={{ y: "100%" }}
          animate={{ y: 0 }}
          exit={{ y: "100%" }}
          transition={{ duration: 0.32, ease: [0.2, 0.8, 0.2, 1] }}
        >
          {/* 메인이 희미하게 보이는 상단 여백 */}
          <div className="h-6 bg-ink/40 flex items-center px-4 shrink-0">
            <span className="font-mono text-[9px] text-white/70">← 메인</span>
          </div>

          {/* 시트 본체 */}
          <div className="flex-1 bg-white rounded-t-2xl flex flex-col overflow-hidden relative">
            {/* 핸들 */}
            <div className="flex justify-center pt-3 pb-1 shrink-0">
              <div className="w-10 h-1 rounded-full bg-line" />
            </div>

            {/* 헤더 */}
            <div className="flex items-center justify-between px-4 py-2 border-b border-line-soft shrink-0">
              <button
                onClick={onClose}
                className="w-8 h-8 flex items-center justify-center rounded-full bg-paper-2 text-ink-soft text-base"
                aria-label="닫기"
              >
                ✕
              </button>
              <span className="text-base font-bold text-ink">{title}</span>
              <span className="font-mono text-xs text-ink-faint">
                Step {step}/{totalSteps}
              </span>
            </div>

            {/* 본문 영역 — 스크롤 허용 */}
            <div className="flex-1 overflow-y-auto px-4 pb-4 flex flex-col">
              {children}
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
