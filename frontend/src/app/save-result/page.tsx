/**
 * /save-result
 *
 * 비회원 검사 후 카카오 로그인 완료 시 리다이렉트되는 페이지.
 * 마운트 시 sessionStorage['disc_result']를 읽어 POST /results 호출.
 *
 * 흐름:
 *   sessionStorage 있음 → POST /results → 저장 완료 토스트 → /로 이동
 *   sessionStorage 없음 → /로 즉시 이동 (직접 접근 방어)
 */

"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function SaveResultPage() {
  const router = useRouter();

  useEffect(() => {
    async function saveAndRedirect() {
      let raw: string | null = null;

      try {
        raw = sessionStorage.getItem("disc_result");
      } catch {
        // 시크릿 모드 등 sessionStorage 접근 불가
      }

      if (!raw) {
        // sessionStorage 없음 - 직접 접근이므로 메인으로
        router.replace("/");
        return;
      }

      try {
        const { scores } = JSON.parse(raw) as {
          scores: { d: number; i: number; s: number; c: number };
        };

        const res = await fetch("/api/v1/results", {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ testType: "DISC", scores }),
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // 저장 성공 - sessionStorage 정리
        try {
          sessionStorage.removeItem("disc_result");
        } catch {
          // 무시
        }
      } catch {
        // 저장 실패 - 결과는 이미 응시 완료됨. 조용히 메인으로 리다이렉트
      } finally {
        router.replace("/");
      }
    }

    saveAndRedirect();
  }, [router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-paper">
      <div className="flex flex-col items-center gap-4">
        <div className="w-10 h-10 rounded-full border-4 border-line border-t-accent animate-spin" />
        <p className="text-sm text-ink-soft">결과를 저장하고 있어요...</p>
      </div>
    </div>
  );
}
