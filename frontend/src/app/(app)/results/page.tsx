"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useResults, RaterType } from "@/hooks/useResults";
import { TypePill, BalancedPill } from "@/components/disc/TypePill";
import { DiscBarsLarge } from "@/components/disc/DiscBarsLarge";
import { getDiscProfile } from "@/lib/disc/profile";

// 날짜 포맷: "2026-05-24T14:30:00" → "2026.05.24"
function formatDate(iso: string): string {
  return iso.slice(0, 10).replace(/-/g, ".");
}

const TABS: { label: string; value: RaterType | null }[] = [
  { label: "내 검사 결과", value: "SELF" },
  { label: "친구가 본 내 모습", value: "OTHER" },
];

export default function ResultsPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<RaterType>("SELF");
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, status } =
    useResults(activeTab);

  const results = data?.pages.flatMap((p) => p.results) ?? [];

  return (
    <div style={{ maxWidth: 480, margin: "0 auto", minHeight: "100dvh" }}>
      {/* 상단 탭 */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          borderBottom: "1.5px solid var(--line)",
          position: "sticky",
          top: 0,
          background: "var(--paper)",
          zIndex: 10,
        }}
      >
        {TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setActiveTab(tab.value as RaterType)}
            style={{
              padding: "14px 0",
              fontSize: 13,
              fontWeight: 700,
              color:
                activeTab === tab.value ? "var(--ink)" : "var(--ink-faint)",
              background: "none",
              border: "none",
              borderBottom:
                activeTab === tab.value
                  ? "2px solid var(--accent)"
                  : "2px solid transparent",
              cursor: "pointer",
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* 콘텐츠 */}
      <div
        style={{
          padding: "14px 16px 80px",
          display: "flex",
          flexDirection: "column",
          gap: 10,
        }}
      >
        {/* 로딩 */}
        {status === "pending" &&
          Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              style={{
                height: 90,
                borderRadius: 14,
                background: "var(--paper-2)",
                animation: "pulse 1.5s ease-in-out infinite",
              }}
            />
          ))}

        {/* 빈 상태 */}
        {status === "success" && results.length === 0 && (
          <div
            style={{
              textAlign: "center",
              padding: "60px 0",
              color: "var(--ink-faint)",
              fontSize: 14,
            }}
          >
            {activeTab === "SELF"
              ? "아직 검사 기록이 없어요"
              : "아직 친구에게 평정 요청한 적이 없어요"}
          </div>
        )}

        {/* 카드 리스트 */}
        {results.map((r) => {
          const displayBuckets = {
            d: r.buckets.d,
            i: r.buckets.i,
            s: r.buckets.s,
            c: r.buckets.c,
          };
          const profile = getDiscProfile(r.buckets);

          return (
            <div
              key={r.resultId}
              onClick={() => router.push(`/results/${r.resultId}`)}
              style={{
                border: "1.5px solid var(--ink)",
                borderRadius: 14,
                background: "white",
                padding: "14px 16px",
                display: "grid",
                gridTemplateColumns: "1fr auto",
                gap: 12,
                alignItems: "center",
                cursor: "pointer",
              }}
            >
              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                {/* 타인 평정 라벨 */}
                {r.raterType === "OTHER" && r.label && (
                  <span
                    style={{
                      alignSelf: "flex-start",
                      padding: "2px 8px",
                      borderRadius: 999,
                      background: "var(--paper-2)",
                      color: "var(--ink-soft)",
                      fontSize: 10.5,
                      fontWeight: 700,
                    }}
                  >
                    ● {r.label}
                  </span>
                )}
                {profile.kind === "balanced" ? (
                  <BalancedPill />
                ) : (
                  profile.types.map((type) => (
                    <TypePill key={type} type={type} />
                  ))
                )}
                <span
                  style={{
                    fontSize: 11,
                    color: "var(--ink-faint)",
                    fontFamily: "var(--font-mono)",
                  }}
                >
                  {formatDate(r.createdAt)}
                </span>
              </div>
              {/* 미니 막대 */}
              <div style={{ width: 100 }}>
                <DiscBarsLarge buckets={displayBuckets} size="md" />
              </div>
            </div>
          );
        })}

        {/* 더 보기 */}
        {hasNextPage && (
          <button
            onClick={() => fetchNextPage()}
            disabled={isFetchingNextPage}
            style={{
              width: "100%",
              padding: "12px 0",
              borderRadius: 10,
              border: "1.5px solid var(--line)",
              background: "none",
              fontSize: 13,
              color: "var(--ink-soft)",
              cursor: "pointer",
            }}
          >
            {isFetchingNextPage ? "불러오는 중…" : "더 보기"}
          </button>
        )}
      </div>
    </div>
  );
}
