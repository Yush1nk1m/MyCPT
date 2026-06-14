// frontend/src/hooks/useStatistics.ts
import { useQuery } from "@tanstack/react-query";

// ── 타입 ──────────────────────────────────────────────────

interface DiscBuckets {
  d: number;
  i: number;
  s: number;
  c: number;
}

interface DiscAverage {
  d: number;
  i: number;
  s: number;
  c: number;
}

export interface ComparisonResponse {
  my: { buckets: DiscBuckets | null };
  average: {
    ageGroup: string;
    gender: string;
    buckets: DiscAverage;
    sampleCount: number;
  } | null;
}

export interface TrendResponse {
  summary: {
    period: string;
    average: DiscAverage | null;
    count: number;
  };
  trend: Array<{
    buckets: DiscBuckets;
    createdAt: string; // "2026-04-01"
  }>;
}

// ── fetch 함수 ────────────────────────────────────────────

async function fetchComparison(): Promise<ComparisonResponse> {
  const res = await fetch("/api/v1/statistics/comparison", {
    credentials: "include",
  });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

async function fetchTrend(days: number): Promise<TrendResponse> {
  const res = await fetch(`/api/v1/statistics/trend?days=${days}`, {
    credentials: "include",
  });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

// ── 훅 ───────────────────────────────────────────────────

export function useComparison() {
  return useQuery({
    queryKey: ["statistics", "comparison"],
    queryFn: fetchComparison,
  });
}

export function useTrend(days: number) {
  return useQuery({
    queryKey: ["statistics", "trend", days],
    queryFn: () => fetchTrend(days),
  });
}
