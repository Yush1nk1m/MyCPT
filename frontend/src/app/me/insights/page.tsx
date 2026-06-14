// frontend/src/app/me/insights/page.tsx
"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useComparison, useTrend } from "@/hooks/useStatistics";

// ── 상수 ──────────────────────────────────────────────────

const DISC_LABELS = ["D", "I", "S", "C"] as const;
type DiscAxis = (typeof DISC_LABELS)[number];

const DISC_META: Record<DiscAxis, { name: string; color: string }> = {
  D: { name: "주도", color: "var(--disc-d)" },
  I: { name: "사교", color: "var(--disc-i)" },
  S: { name: "안정", color: "var(--disc-s)" },
  C: { name: "신중", color: "var(--disc-c)" },
};

// trend 기간 필터 옵션 → API days 파라미터로 변환
const RANGE_OPTIONS = [
  { label: "최근 3개월", days: 90 },
  { label: "최근 6개월", days: 180 },
  { label: "최근 1년", days: 365 },
  { label: "전체", days: 3650 }, // 10년 = 사실상 전체
] as const;

// ── 서브 컴포넌트 ─────────────────────────────────────────

// 뒤로 가기 바
function BackBar({ title }: { title: string }) {
  const router = useRouter();
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 8,
        padding: "14px 16px",
        borderBottom: "1px solid var(--line)",
        background: "white",
      }}
    >
      <button
        onClick={() => router.back()}
        style={{
          background: "none",
          border: "none",
          cursor: "pointer",
          padding: 0,
          fontSize: 18,
          color: "var(--ink)",
          lineHeight: 1,
        }}
      >
        ←
      </button>
      <span style={{ fontWeight: 700, fontSize: 15, color: "var(--ink)" }}>
        {title}
      </span>
    </div>
  );
}

// 탭 바
function PageTabs({
  active,
  onChange,
}: {
  active: "compare" | "trend";
  onChange: (tab: "compare" | "trend") => void;
}) {
  return (
    <div
      style={{
        display: "flex",
        borderBottom: "1px solid var(--line)",
        background: "white",
      }}
    >
      {(["compare", "trend"] as const).map((tab) => {
        const label = tab === "compare" ? "비교" : "추이";
        const isActive = active === tab;
        return (
          <button
            key={tab}
            onClick={() => onChange(tab)}
            style={{
              flex: 1,
              padding: "12px 0",
              fontSize: 13,
              fontWeight: isActive ? 700 : 400,
              color: isActive ? "var(--ink)" : "var(--ink-faint)",
              background: "none",
              border: "none",
              borderBottom: isActive
                ? "2px solid var(--ink)"
                : "2px solid transparent",
              cursor: "pointer",
              transition: "all 0.15s",
            }}
          >
            {label}
          </button>
        );
      })}
    </div>
  );
}

// 버킷값(1~3)을 막대 높이 퍼센트로 변환 (1→33%, 2→66%, 3→100%)
function bucketToPercent(bucket: number) {
  return Math.round((bucket / 3) * 100);
}

// 비교 탭 — 4축 막대 차트
function CompareTab() {
  const { data, status } = useComparison();

  if (status === "pending") {
    return <SkeletonBox height={300} />;
  }

  if (status === "error") {
    return <ErrorBox message="통계를 불러오지 못했어요." />;
  }

  // 검사 이력 없음
  if (!data.my.buckets) {
    return <EmptyBox message="검사를 한 번 완료하면 통계 비교가 가능해요." />;
  }

  // 생년/성별 미입력 → 평균 없음
  const hasAverage = data.average !== null;

  return (
    <div
      style={{
        padding: "20px 16px",
        display: "flex",
        flexDirection: "column",
        gap: 24,
      }}
    >
      {/* 안내 배너 — 생년/성별 미입력 시 */}
      {!hasAverage && (
        <div
          style={{
            background: "var(--paper-2)",
            border: "1px dashed var(--line)",
            borderRadius: 12,
            padding: "12px 14px",
            fontSize: 12,
            color: "var(--ink-soft)",
            lineHeight: 1.6,
          }}
        >
          생년·성별을 입력하면{" "}
          <b style={{ color: "var(--ink)" }}>또래 평균과 비교</b>할 수 있어요.{" "}
          <Link
            href="/me/profile"
            style={{ color: "var(--accent)", textDecoration: "underline" }}
          >
            프로필 보완하기
          </Link>
        </div>
      )}

      {/* 범례 */}
      {hasAverage && (
        <div
          style={{
            display: "flex",
            gap: 16,
            fontSize: 11,
            color: "var(--ink-soft)",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <div
              style={{
                width: 10,
                height: 10,
                borderRadius: 2,
                background: "var(--ink)",
              }}
            />
            나
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
            <div
              style={{
                width: 10,
                height: 10,
                borderRadius: 2,
                background: "var(--line)",
                border: "1px solid var(--ink-faint)",
              }}
            />
            {data.average!.ageGroup}{" "}
            {data.average!.gender === "M" ? "남성" : "여성"} 평균
            <span style={{ color: "var(--ink-faint)", fontSize: 10 }}>
              (n={data.average!.sampleCount})
            </span>
          </div>
        </div>
      )}

      {/* 4축 막대 차트 */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 12,
        }}
      >
        {DISC_LABELS.map((axis) => {
          const myVal =
            data.my.buckets![axis.toLowerCase() as "d" | "i" | "s" | "c"];
          const avgVal = hasAverage
            ? data.average!.buckets[axis.toLowerCase() as "d" | "i" | "s" | "c"]
            : null;
          const myPct = bucketToPercent(myVal);
          const avgPct =
            avgVal !== null ? Math.round((avgVal / 3) * 100) : null;

          return (
            <div
              key={axis}
              style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 6,
              }}
            >
              {/* 막대 영역 */}
              <div
                style={{
                  position: "relative",
                  width: "100%",
                  height: 120,
                  display: "flex",
                  alignItems: "flex-end",
                  justifyContent: "center",
                  gap: hasAverage ? 4 : 0,
                }}
              >
                {/* 내 막대 */}
                <div
                  style={{
                    width: hasAverage ? "44%" : "60%",
                    height: `${myPct}%`,
                    background: DISC_META[axis].color,
                    borderRadius: "4px 4px 0 0",
                    transition: "height 0.4s ease",
                    display: "flex",
                    alignItems: "flex-start",
                    justifyContent: "center",
                    paddingTop: 4,
                  }}
                >
                  <span
                    style={{ fontSize: 10, fontWeight: 700, color: "white" }}
                  >
                    {myVal}
                  </span>
                </div>
                {/* 평균 막대 */}
                {avgPct !== null && (
                  <div
                    style={{
                      width: "44%",
                      height: `${avgPct}%`,
                      background: "var(--paper-2)",
                      border: "1.5px solid var(--line)",
                      borderRadius: "4px 4px 0 0",
                      transition: "height 0.4s ease",
                      display: "flex",
                      alignItems: "flex-start",
                      justifyContent: "center",
                      paddingTop: 4,
                    }}
                  >
                    <span
                      style={{
                        fontSize: 9,
                        color: "var(--ink-soft)",
                        fontWeight: 600,
                      }}
                    >
                      {avgVal!.toFixed(1)}
                    </span>
                  </div>
                )}
              </div>
              {/* 축 라벨 */}
              <div
                style={{
                  fontSize: 11,
                  fontWeight: 700,
                  color: DISC_META[axis].color,
                }}
              >
                {axis}
              </div>
              <div style={{ fontSize: 10, color: "var(--ink-faint)" }}>
                {DISC_META[axis].name}
              </div>
            </div>
          );
        })}
      </div>

      {/* 자연어 인사이트 — 편차가 가장 큰 축 강조 */}
      {hasAverage && (
        <InsightSentence my={data.my.buckets!} avg={data.average!.buckets} />
      )}
    </div>
  );
}

// 자연어 인사이트 — 편차 최대 축 설명
function InsightSentence({
  my,
  avg,
}: {
  my: { d: number; i: number; s: number; c: number };
  avg: { d: number; i: number; s: number; c: number };
}) {
  // 편차 절대값이 가장 큰 축 찾기
  const diffs = DISC_LABELS.map((axis) => {
    const key = axis.toLowerCase() as "d" | "i" | "s" | "c";
    return { axis, diff: my[key] - avg[key] };
  });
  const max = diffs.reduce((a, b) =>
    Math.abs(a.diff) > Math.abs(b.diff) ? a : b,
  );

  if (Math.abs(max.diff) === 0) {
    return (
      <p style={{ fontSize: 12, color: "var(--ink-soft)", lineHeight: 1.6 }}>
        또래 평균과 전반적으로 비슷한 성향이에요.
      </p>
    );
  }

  const direction = max.diff > 0 ? "높아요" : "낮아요";
  const meta = DISC_META[max.axis];

  return (
    <p style={{ fontSize: 12, color: "var(--ink-soft)", lineHeight: 1.6 }}>
      또래에 비해{" "}
      <b style={{ color: meta.color }}>
        {max.axis}축({meta.name})
      </b>
      이 특히 {direction}.
    </p>
  );
}

// 추이 탭 — 4축 라인 차트 + 응시 기록
function TrendTab() {
  type RangeOption = (typeof RANGE_OPTIONS)[number];
  const [selectedRange, setSelectedRange] = useState<RangeOption>(
    RANGE_OPTIONS[1], // 기본: 최근 6개월
  );
  const { data, status } = useTrend(selectedRange.days);

  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {/* 기간 필터 칩 */}
      <div
        style={{
          padding: "12px 16px",
          display: "flex",
          gap: 8,
          overflowX: "auto",
          borderBottom: "1px solid var(--line)",
          background: "white",
        }}
      >
        {RANGE_OPTIONS.map((opt) => {
          const isActive = opt.days === selectedRange.days;
          return (
            <button
              key={opt.days}
              onClick={() => setSelectedRange(opt)}
              style={{
                padding: "5px 12px",
                borderRadius: 999,
                border: `1.2px solid ${isActive ? "var(--ink)" : "var(--line)"}`,
                background: isActive ? "var(--ink)" : "white",
                color: isActive ? "white" : "var(--ink-soft)",
                fontSize: 12,
                fontWeight: isActive ? 700 : 400,
                cursor: "pointer",
                whiteSpace: "nowrap",
                transition: "all 0.15s",
              }}
            >
              {opt.label}
            </button>
          );
        })}
      </div>

      {/* 콘텐츠 */}
      <div
        style={{
          padding: "20px 16px",
          display: "flex",
          flexDirection: "column",
          gap: 20,
        }}
      >
        {status === "pending" && <SkeletonBox height={200} />}

        {status === "error" && (
          <ErrorBox message="추이 데이터를 불러오지 못했어요." />
        )}

        {status === "success" && data.trend.length < 2 && (
          <EmptyBox message="한 번 더 검사하면 변화 추이를 볼 수 있어요." />
        )}

        {status === "success" && data.trend.length >= 2 && (
          <>
            <TrendLineChart points={data.trend} />

            {/* 요약 */}
            <div
              style={{
                background: "var(--paper-2)",
                borderRadius: 12,
                padding: "12px 14px",
                fontSize: 12,
                color: "var(--ink-soft)",
                lineHeight: 1.7,
              }}
            >
              {selectedRange.label} 동안{" "}
              <b style={{ color: "var(--ink)" }}>{data.summary.count}회</b>{" "}
              응시했어요.
              {data.summary.average && (
                <>
                  {" "}
                  평균은 D{data.summary.average.d.toFixed(1)} / I
                  {data.summary.average.i.toFixed(1)} / S
                  {data.summary.average.s.toFixed(1)} / C
                  {data.summary.average.c.toFixed(1)}이에요.
                </>
              )}
            </div>

            {/* 응시 기록 리스트 */}
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              <p
                style={{
                  fontSize: 11,
                  color: "var(--ink-faint)",
                  fontFamily: "'JetBrains Mono', monospace",
                  letterSpacing: "0.04em",
                  textTransform: "uppercase",
                  margin: 0,
                }}
              >
                응시 기록
              </p>
              {[...data.trend].reverse().map((point, i) => (
                <div
                  key={i}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "10px 12px",
                    background: "white",
                    borderRadius: 10,
                    border: "1px solid var(--line)",
                  }}
                >
                  <span
                    style={{
                      fontSize: 11,
                      color: "var(--ink-faint)",
                      fontFamily: "'JetBrains Mono', monospace",
                    }}
                  >
                    {point.createdAt}
                  </span>
                  <div style={{ display: "flex", gap: 6 }}>
                    {DISC_LABELS.map((axis) => (
                      <span
                        key={axis}
                        style={{
                          fontSize: 11,
                          fontWeight: 700,
                          color: DISC_META[axis].color,
                        }}
                      >
                        {axis}
                        {
                          point.buckets[
                            axis.toLowerCase() as "d" | "i" | "s" | "c"
                          ]
                        }
                      </span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

// SVG 라인 차트 — 4축 동시 표시
function TrendLineChart({
  points,
}: {
  points: Array<{
    buckets: { d: number; i: number; s: number; c: number };
    createdAt: string;
  }>;
}) {
  const W = 320;
  const H = 140;
  const PADDING = { top: 10, bottom: 24, left: 8, right: 8 };
  const innerW = W - PADDING.left - PADDING.right;
  const innerH = H - PADDING.top - PADDING.bottom;
  const n = points.length;

  // x: 0~(n-1) 균등 분할, y: 버킷 1~3 → innerH~0
  const xOf = (i: number) => PADDING.left + (i / (n - 1)) * innerW;
  const yOf = (bucket: number) =>
    PADDING.top + innerH - ((bucket - 1) / 2) * innerH;

  const buildPath = (axis: "d" | "i" | "s" | "c") =>
    points
      .map(
        (p, i) =>
          `${i === 0 ? "M" : "L"} ${xOf(i).toFixed(1)} ${yOf(p.buckets[axis]).toFixed(1)}`,
      )
      .join(" ");

  return (
    <svg
      width="100%"
      viewBox={`0 0 ${W} ${H}`}
      style={{ display: "block", overflow: "visible" }}
    >
      {/* 수평 가이드 라인 (버킷 1~3) */}
      {[1, 2, 3].map((v) => (
        <line
          key={v}
          x1={PADDING.left}
          x2={W - PADDING.right}
          y1={yOf(v)}
          y2={yOf(v)}
          stroke="var(--line)"
          strokeWidth={0.5}
          strokeDasharray="3 4"
        />
      ))}

      {/* 4축 라인 */}
      {DISC_LABELS.map((axis) => (
        <path
          key={axis}
          d={buildPath(axis.toLowerCase() as "d" | "i" | "s" | "c")}
          fill="none"
          stroke={DISC_META[axis].color}
          strokeWidth={2}
          strokeLinejoin="round"
          strokeLinecap="round"
        />
      ))}

      {/* 각 포인트 원 + 마지막 포인트 강조 */}
      {DISC_LABELS.map((axis) =>
        points.map((p, i) => (
          <circle
            key={`${axis}-${i}`}
            cx={xOf(i)}
            cy={yOf(p.buckets[axis.toLowerCase() as "d" | "i" | "s" | "c"])}
            r={i === n - 1 ? 4 : 2.5}
            fill={DISC_META[axis].color}
            stroke="white"
            strokeWidth={1}
          />
        )),
      )}

      {/* x축 날짜 라벨 — 첫/마지막만 표시 */}
      {[0, n - 1].map((i) => (
        <text
          key={i}
          x={xOf(i)}
          y={H - 4}
          textAnchor={i === 0 ? "start" : "end"}
          fontSize={9}
          fill="var(--ink-faint)"
          fontFamily="'JetBrains Mono', monospace"
        >
          {points[i].createdAt.slice(5)} {/* "04-01" 형식 */}
        </text>
      ))}
    </svg>
  );
}

// ── 공용 상태 컴포넌트 ────────────────────────────────────

function SkeletonBox({ height }: { height: number }) {
  return (
    <div
      style={{
        height,
        borderRadius: 12,
        background: "var(--paper-2)",
        animation: "pulse 1.5s ease-in-out infinite",
      }}
    />
  );
}

function EmptyBox({ message }: { message: string }) {
  return (
    <div
      style={{
        padding: "40px 16px",
        textAlign: "center",
        color: "var(--ink-faint)",
        fontSize: 13,
      }}
    >
      {message}
    </div>
  );
}

function ErrorBox({ message }: { message: string }) {
  return (
    <div
      style={{
        padding: "20px 16px",
        textAlign: "center",
        color: "var(--ink-faint)",
        fontSize: 13,
      }}
    >
      {message}
    </div>
  );
}

// ── 페이지 ────────────────────────────────────────────────

export default function InsightsPage() {
  const [activeTab, setActiveTab] = useState<"compare" | "trend">("compare");

  return (
    <div
      style={{
        maxWidth: 480,
        margin: "0 auto",
        background: "var(--paper-2)",
        minHeight: "100vh",
      }}
    >
      <BackBar title="통계 비교 · 변화 추이" />

      {/* 검사 종류 세그먼트 — MVP는 DISC 고정 */}
      <div
        style={{
          background: "white",
          borderBottom: "1px solid var(--line)",
          padding: "10px 16px",
          display: "flex",
          alignItems: "center",
          gap: 10,
        }}
      >
        <span
          style={{
            fontSize: 10,
            color: "var(--ink-faint)",
            fontFamily: "'JetBrains Mono', monospace",
            letterSpacing: "0.04em",
            textTransform: "uppercase",
          }}
        >
          검사 종류
        </span>
        <div
          style={{
            display: "inline-flex",
            padding: 3,
            background: "var(--paper-2)",
            border: "1px solid var(--line)",
            borderRadius: 999,
          }}
        >
          <div
            style={{
              padding: "5px 14px",
              borderRadius: 999,
              background: "var(--ink)",
              color: "white",
              fontSize: 11.5,
              fontWeight: 700,
            }}
          >
            DISC
          </div>
        </div>
      </div>

      <PageTabs active={activeTab} onChange={setActiveTab} />

      {activeTab === "compare" ? <CompareTab /> : <TrendTab />}
    </div>
  );
}
