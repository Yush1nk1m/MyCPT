import { useQuery, useInfiniteQuery } from "@tanstack/react-query";

// ── 타입 ──────────────────────────────────────────────────

export interface CoinBalanceResponse {
  coins: number;
  nextCoinAt: string | null; // ISO LocalDateTime, null이면 만충
}

export interface CoinTransactionResponse {
  transactionId: number;
  amount: number; // 양수: 충전, 음수: 차감
  reason: string; // "SIGNUP" | "RECHARGE" | "CHEMISTRY_REPORT"
  balanceAfter: number;
  createdAt: string; // ISO LocalDateTime
}

interface CoinHistoryResponse {
  history: CoinTransactionResponse[];
  nextCursor: number | null;
  hasNext: boolean;
}

// ── fetch 함수 ────────────────────────────────────────────

async function fetchCoinBalance(): Promise<CoinBalanceResponse> {
  const res = await fetch("/api/v1/coins", { credentials: "include" });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

async function fetchCoinHistory(
  cursor: number | null,
): Promise<CoinHistoryResponse> {
  const params = new URLSearchParams();
  params.set("size", "20");
  if (cursor) params.set("cursor", String(cursor));

  const res = await fetch(`/api/v1/coins/history?${params}`, {
    credentials: "include",
  });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

// ── 훅 ───────────────────────────────────────────────────

export function useCoinBalance() {
  return useQuery({
    queryKey: ["coins", "balance"],
    queryFn: fetchCoinBalance,
  });
}

export function useCoinHistory() {
  return useInfiniteQuery({
    queryKey: ["coins", "history"],
    queryFn: ({ pageParam: cursor }: { pageParam: number | null }) =>
      fetchCoinHistory(cursor),
    initialPageParam: null,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.nextCursor : undefined,
  });
}
