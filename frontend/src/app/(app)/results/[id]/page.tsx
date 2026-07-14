"use client";

import { use } from "react";
import { useQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { TypePill, BalancedPill } from "@/components/disc/TypePill";
import { DiscBarsLarge } from "@/components/disc/DiscBarsLarge";
import { ReportMarkdown } from "@/components/disc/ReportMarkdown";
import { getDiscProfile } from "@/lib/disc/profile";
import { formatDateDot } from "@/lib/format";

type RaterType = "SELF" | "OTHER";

interface ResultDetail {
  resultId: number;
  raterType: RaterType;
  label: string | null;
  scores: { d: number; i: number; s: number; c: number };
  buckets: { d: number; i: number; s: number; c: number };
  report: string;
  createdAt: string;
}

async function fetchResultDetail(id: string): Promise<ResultDetail> {
  const res = await fetch(`/api/v1/results/${id}`, { credentials: "include" });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (res.status === 403) return Promise.reject(new Error("FORBIDDEN"));
  if (res.status === 404) return Promise.reject(new Error("NOT_FOUND"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

export default function ResultDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data, status, error } = useQuery({
    queryKey: ["result", id],
    queryFn: () => fetchResultDetail(id),
  });

  // 로딩
  if (status === "pending") {
    return (
      <div style={{ padding: "20px 16px" }}>
        <div
          style={{
            height: 40,
            borderRadius: 8,
            background: "var(--paper-2)",
            marginBottom: 20,
          }}
        />
        <div
          style={{
            height: 160,
            borderRadius: 12,
            background: "var(--paper-2)",
          }}
        />
      </div>
    );
  }

  // 에러
  if (status === "error") {
    const msg =
      error.message === "NOT_FOUND"
        ? "결과를 찾을 수 없어요"
        : error.message === "FORBIDDEN"
          ? "접근 권한이 없어요"
          : "잠시 후 다시 시도해 주세요";

    return (
      <div
        style={{
          padding: "80px 16px",
          textAlign: "center",
          color: "var(--ink-faint)",
          fontSize: 14,
        }}
      >
        {msg}
      </div>
    );
  }

  const profile = getDiscProfile(data.buckets);

  return (
    <div style={{ paddingBottom: 80 }}>
      {/* 상단 백바 */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 12,
          padding: "14px 16px",
          borderBottom: "1px solid var(--line)",
          position: "sticky",
          top: 0,
          background: "var(--paper)",
          zIndex: 10,
        }}
      >
        <button
          onClick={() => router.back()}
          style={{
            background: "none",
            border: "none",
            fontSize: 20,
            cursor: "pointer",
            color: "var(--ink)",
            padding: 0,
          }}
        >
          ‹
        </button>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: "var(--ink)" }}>
            DISC 검사 결과
          </div>
          <div
            style={{
              fontSize: 11,
              color: "var(--ink-faint)",
              fontFamily: "var(--font-mono)",
            }}
          >
            {formatDateDot(data.createdAt)}
            {data.label && ` · ${data.label}`}
          </div>
        </div>
      </div>

      {/* Hero — DISC 시각화 */}
      <div
        style={{
          padding: "24px 16px 20px",
          borderBottom: "1px solid var(--line)",
        }}
      >
        {/* 타인 평정 라벨 */}
        {data.raterType === "OTHER" && data.label && (
          <span
            style={{
              display: "inline-block",
              marginBottom: 10,
              padding: "2px 8px",
              borderRadius: 999,
              background: "var(--paper-2)",
              color: "var(--ink-soft)",
              fontSize: 10.5,
              fontWeight: 700,
            }}
          >
            ● {data.label}
          </span>
        )}
        {profile.kind === "balanced" ? (
          <BalancedPill />
        ) : (
          profile.types.map((type) => <TypePill key={type} type={type} />)
        )}
        <div style={{ marginTop: 16 }}>
          <DiscBarsLarge buckets={data.buckets} size="lg" />
        </div>
      </div>

      {/* 보고서 본문 */}
      <div style={{ padding: "8px 16px" }}>
        <ReportMarkdown report={data.report} />
      </div>
    </div>
  );
}
