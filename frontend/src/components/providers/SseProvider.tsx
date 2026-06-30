"use client";

import { useSseConnection } from "@/hooks/useSseConnection";

// layout.tsx에 마운트되는 빈 컴포넌트 — 훅 실행만이 목적
export function SseProvider() {
  useSseConnection();
  return null;
}
