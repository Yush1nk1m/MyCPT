/**
 * DoneState
 *
 * 결과 표시 + 회원/비회원 분기.
 *
 * 회원:
 *   - 마운트 시 POST /results 자동 호출 (1회)
 *   - 저장 상태 칩 표시 (saving → saved | failed)
 *   - MemberCta: "결과 상세로 가기"
 *
 * 비회원:
 *   - GuestCta: "카카오로 결과 저장하기"
 */

"use client";

import { useEffect, useRef, useState } from "react";
import type { ScoreResult } from "@/stores/testSheetStore";
import { useMe } from "@/hooks/useMe";
import { TypePill, BalancedPill } from "@/components/disc/TypePill";
import { DiscBarsLarge } from "@/components/disc/DiscBarsLarge";
import { ReportMarkdown } from "@/components/disc/ReportMarkdown";
import { GuestCta } from "./GuestCta";
import { MemberCta } from "./MemberCta";
import { getDiscProfile } from "@/lib/disc/profile";

type SaveStatus = "idle" | "saving" | "saved" | "failed";

interface DoneStateProps {
  result: ScoreResult;
  onClose: () => void;
}

export function DoneState({ result, onClose }: DoneStateProps) {
  const { data: meData, isLoading: meLoading } = useMe();
  const isAuthenticated = !!meData;
  const user = meData ?? null;

  const profile = getDiscProfile(result.buckets);

  const [saveStatus, setSaveStatus] = useState<SaveStatus>("idle");
  const [resultId, setResultId] = useState<number | null>(null);

  // 마운트 1회만 실행
  const hasSaved = useRef(false);
  useEffect(() => {
    if (!isAuthenticated || hasSaved.current) return;
    hasSaved.current = true;

    setSaveStatus("saving");

    fetch("/api/v1/results", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        testType: result.testType,
        scores: result.scores,
      }),
    })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json() as Promise<{ resultId: number }>;
      })
      .then((data) => {
        try {
          sessionStorage.removeItem("disc_result");
        } catch {
          // 시크릿 모드 등 무시
        }
        setResultId(data.resultId);
        setSaveStatus("saved");
      })
      .catch(() => {
        setSaveStatus("failed");
      });
  }, [isAuthenticated, result.testType, result.scores]);

  return (
    <div className="flex-1 flex flex-col overflow-y-auto">
      {/* ── hero ── */}
      <div className="flex flex-col gap-3 px-5 pt-5 pb-4 bg-paper">
        <div className="flex gap-2 flex-wrap">
          {profile.kind === "typed" ? (
            profile.types.map((type) => <TypePill key={type} type={type} />)
          ) : (
            <BalancedPill />
          )}
        </div>

        <h1 className="text-2xl font-black text-ink leading-tight">
          {isAuthenticated && user ? `${user.nickname}님의` : "사용자님의"} DISC
          보고서
        </h1>

        <p className="text-sm text-ink-soft">
          24문항 응시 결과를 4축으로 정리했어요.
        </p>

        <DiscBarsLarge buckets={result.buckets} size="lg" />

        {/* 저장 상태 칩 (회원 전용) */}
        {isAuthenticated && (
          <div className="flex items-center h-6">
            {saveStatus === "saving" && (
              <span className="flex items-center gap-1.5 text-xs text-ink-soft">
                <span className="w-3 h-3 rounded-full border-2 border-ink-soft border-t-transparent animate-spin" />
                저장 중…
              </span>
            )}
            {saveStatus === "saved" && (
              <span className="text-xs text-accent font-semibold">
                ✓ 저장됨
              </span>
            )}
            {saveStatus === "failed" && (
              <span className="text-xs text-red-400">저장에 실패했어요</span>
            )}
          </div>
        )}
      </div>

      {/* ── 보고서 본문 ── */}
      <div className="flex-1 px-5 py-5 bg-paper-2">
        <ReportMarkdown report={result.report} />
      </div>

      {/* ── CTA ── */}
      {meLoading ? null : isAuthenticated ? (
        <MemberCta
          saveStatus={saveStatus}
          resultId={resultId}
          onClose={onClose}
        />
      ) : (
        <GuestCta onClose={onClose} />
      )}
    </div>
  );
}
