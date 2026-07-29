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

/**
 * 코인 잔량 조회 — UI에 표시하는 코인 값의 단일 출처.
 *
 * GET /auth/me 응답에도 coins 필드가 있지만 화면에서 사용하지 않는다.
 * 온디맨드 충전은 GET /coins에서만 일어나므로(CoinService.getBalance),
 * ["me"].coins는 충전이 반영되지 않은 낡은 값일 수 있다.
 *
 * @param enabled 비로그인 상태에서 401을 유발하지 않도록 호출 여부를 제어
 */
export function useCoinBalance(enabled: boolean = true) {
  return useQuery({
    queryKey: ["coins", "balance"],
    queryFn: fetchCoinBalance,
    enabled,
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
