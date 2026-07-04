"use client";

import { useEffect, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useToast } from "@/hooks/useToast";
import { useMe } from "@/hooks/useMe";
import type { SseEventPayload } from "@/types/sse";

// 전역 SSE 연결 — 로그인 상태일 때만 1회 연결, 앱 전체 생명주기 동안 유지
// Next.js rewrites 프록시를 거치면 gzip 압축으로 스트리밍이 버퍼링되므로
// 백엔드에 직접 연결한다 (CORS + withCredentials로 쿠키 인증 유지)
const SSE_URL = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/notifications/stream`;

export function useSseConnection() {
  const { data: me } = useMe();
  const queryClient = useQueryClient();
  const showToast = useToast();
  const esRef = useRef<EventSource | null>(null);

  useEffect(() => {
    // 비로그인 상태에서는 연결하지 않음 (401 방지)
    if (!me) {
      esRef.current?.close();
      esRef.current = null;
      return;
    }

    if (esRef.current) return; // 이미 연결됨

    const es = new EventSource(SSE_URL, {
      withCredentials: true,
    });
    esRef.current = es;

    // 케미 보고서 완료 — 토스트 + 관련 쿼리 무효화
    es.addEventListener("CHEMISTRY_REPORT", (e: MessageEvent) => {
      try {
        const payload: SseEventPayload = JSON.parse(e.data);
        showToast(payload.message);
        queryClient.invalidateQueries({ queryKey: ["chemistry-reports"] });
        queryClient.invalidateQueries({
          queryKey: ["chemistry-report", String(payload.referenceId)],
        });
        queryClient.invalidateQueries({ queryKey: ["notifications"] }); // 추가
      } catch {
        queryClient.invalidateQueries({ queryKey: ["chemistry-reports"] });
        queryClient.invalidateQueries({ queryKey: ["notifications"] }); // 추가
      }
    });

    // 케미 보고서 실패
    es.addEventListener("CHEMISTRY_ERROR", (e: MessageEvent) => {
      try {
        const payload: SseEventPayload = JSON.parse(e.data);
        showToast(payload.message);
        queryClient.invalidateQueries({ queryKey: ["chemistry-reports"] });
        queryClient.invalidateQueries({
          queryKey: ["chemistry-report", String(payload.referenceId)],
        });
        queryClient.invalidateQueries({ queryKey: ["notifications"] }); // 추가
      } catch {
        queryClient.invalidateQueries({ queryKey: ["chemistry-reports"] });
        queryClient.invalidateQueries({ queryKey: ["notifications"] }); // 추가
      }
    });

    // 동료 등록 알림
    es.addEventListener("COLLEAGUE_REGISTERED", (e: MessageEvent) => {
      try {
        const payload: SseEventPayload = JSON.parse(e.data);
        showToast(payload.message);
        queryClient.invalidateQueries({ queryKey: ["colleagues"] });
        queryClient.invalidateQueries({ queryKey: ["notifications"] }); // 추가
      } catch {
        queryClient.invalidateQueries({ queryKey: ["colleagues"] });
        queryClient.invalidateQueries({ queryKey: ["notifications"] }); // 추가
      }
    });

    // 브라우저가 Last-Event-ID를 자동으로 재연결 헤더에 포함시키므로 별도 처리 없음
    return () => {
      es.close();
      esRef.current = null;
    };
  }, [me, queryClient, showToast]);
}
