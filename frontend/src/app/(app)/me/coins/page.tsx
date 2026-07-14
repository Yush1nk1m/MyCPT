// frontend/src/app/me/coins/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { calcRemaining, reasonLabel } from "@/lib/coin";
import { formatDateTime } from "@/lib/format";

// ── 타입 ──────────────────────────────────────────────────────────────────────

interface CoinBalanceResponse {
  coins: number;
  nextCoinAt: string | null;
}

interface CoinTransactionResponse {
  transactionId: number;
  amount: number;
  reason: string;
  balanceAfter: number;
  createdAt: string;
}

interface CoinHistoryResponse {
  history: CoinTransactionResponse[];
  nextCursor: number | null;
  hasNext: boolean;
}

// ── fetch ─────────────────────────────────────────────────────────────────────

async function fetchCoinBalance(): Promise<CoinBalanceResponse> {
  const res = await fetch("/api/v1/coins", { credentials: "include" });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

async function fetchCoinHistory(
  cursor: number | null,
): Promise<CoinHistoryResponse> {
  const params = new URLSearchParams({ size: "20" });
  if (cursor) params.set("cursor", String(cursor));
  const res = await fetch(`/api/v1/coins/history?${params}`, {
    credentials: "include",
  });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

// ── 카운트다운 ────────────────────────────────────────────────────────────────

// nextCoinAt이 null(만충)이면 null 반환
// CoinContent가 balance.data 확정 후 마운트되므로 lazy initializer가 항상 실제 값으로 초기화됨
function useCountdown(nextCoinAt: string | null): string | null {
  const [remaining, setRemaining] = useState<string | null>(() =>
    nextCoinAt ? calcRemaining(nextCoinAt) : null,
  );

  useEffect(() => {
    // null이면 interval 없이 종료 — effect 내 setState 호출 없음
    if (!nextCoinAt) return;

    const id = setInterval(() => {
      setRemaining(calcRemaining(nextCoinAt));
    }, 1000);

    return () => clearInterval(id);
  }, [nextCoinAt]);

  return remaining;
}

// ── CoinContent (balance 확정 후에만 마운트) ──────────────────────────────────

function CoinContent({ balance }: { balance: CoinBalanceResponse }) {
  const countdown = useCountdown(balance.nextCoinAt);

  const history = useInfiniteQuery({
    queryKey: ["coins", "history"],
    queryFn: ({ pageParam: cursor }: { pageParam: number | null }) =>
      fetchCoinHistory(cursor),
    initialPageParam: null,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.nextCursor : undefined,
  });

  const allHistory = history.data?.pages.flatMap((p) => p.history) ?? [];

  return (
    <div className="flex flex-col min-h-full bg-[var(--paper)]">
      {/* BackBar */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-[var(--line)] sticky top-0 z-10">
        <Link
          href="/me"
          className="text-[var(--ink-soft)] text-lg leading-none"
          aria-label="뒤로가기"
        >
          ‹
        </Link>
        <div className="flex flex-col min-w-0">
          <span className="text-[13.5px] font-bold text-[var(--ink)] leading-tight">
            코인
          </span>
          <span className="text-[10.5px] text-[var(--ink-faint)] font-mono">
            케미 1회 = 1 코인
          </span>
        </div>
      </div>

      {/* Hero — 잔량 + 슬롯 + 카운트다운 */}
      <div className="bg-white border-b border-[var(--line)] px-4 py-6 text-center">
        <div className="text-[10.5px] text-[var(--ink-faint)] font-mono tracking-widest uppercase mb-2">
          현재 잔량
        </div>
        <div className="flex items-baseline justify-center gap-1.5">
          <span className="text-[56px] font-extrabold text-[var(--accent)] leading-none font-mono">
            {balance.coins}
          </span>
          <span className="text-lg font-semibold text-[var(--ink-soft)]">
            / 3
          </span>
        </div>

        {/* 3슬롯 */}
        <div className="flex gap-2 justify-center mt-3.5">
          {[0, 1, 2].map((i) => {
            const filled = i < balance.coins;
            return (
              <div
                key={i}
                className="w-9 h-9 rounded-full border-[1.5px] border-[var(--ink)] flex items-center justify-center text-base font-bold"
                style={{
                  background: filled ? "var(--accent)" : "var(--paper-2)",
                  color: filled ? "white" : "var(--ink-faint)",
                }}
              >
                ⊙
              </div>
            );
          })}
        </div>

        {/* 카운트다운 — 만충이면 숨김 */}
        {countdown !== null && (
          <div className="mt-3.5 text-[11.5px] text-[var(--ink-soft)] leading-relaxed">
            다음 충전까지{" "}
            <b className="text-[var(--ink)] font-mono">{countdown}</b>
          </div>
        )}
      </div>

      {/* 안내 배너 */}
      <div
        className="px-4 py-3 text-[11px] text-[var(--ink-soft)] leading-relaxed border-b border-dashed border-[var(--accent)]"
        style={{ background: "oklch(0.97 0.03 95)" }}
      >
        <b className="text-[var(--ink)]">안내</b> · 코인은 매일 자정에 1개씩
        자동 충전되며, 최대 3개까지 보관됩니다. 케미 보고서 1회 발행에 1 코인이
        사용돼요.
      </div>

      {/* 사용 이력 */}
      <div className="text-[10.5px] text-[var(--ink-faint)] font-mono tracking-widest uppercase px-4 pt-4 pb-2">
        사용 이력
      </div>

      <div className="bg-white">
        {history.isPending ? (
          <div className="py-10 text-center text-sm text-[var(--ink-faint)]">
            로딩 중…
          </div>
        ) : history.isError ? (
          <div className="py-10 text-center text-sm text-[var(--ink-faint)]">
            이력을 불러오지 못했어요
          </div>
        ) : allHistory.length === 0 ? (
          <div className="py-10 text-center text-sm text-[var(--ink-faint)]">
            이용 이력이 없어요
          </div>
        ) : (
          allHistory.map((tx) => {
            const isPositive = tx.amount > 0;
            return (
              <div
                key={tx.transactionId}
                className="flex items-center gap-3 px-4 py-3.5 border-t border-[var(--line-soft)]"
              >
                <div
                  className="w-8 h-8 rounded-lg border border-[var(--line)] flex items-center justify-center text-xs font-bold font-mono flex-shrink-0"
                  style={{
                    background: isPositive
                      ? "var(--member-bg)"
                      : "var(--accent-2)",
                    color: isPositive ? "var(--member)" : "var(--accent)",
                  }}
                >
                  {isPositive ? "+" : "−"}
                </div>
                <div className="flex flex-col min-w-0 flex-1">
                  <span className="text-[12.5px] text-[var(--ink)] leading-snug">
                    {reasonLabel(tx.reason)}
                  </span>
                  <span className="text-[11px] text-[var(--ink-soft)] mt-0.5">
                    {formatDateTime(tx.createdAt)}
                  </span>
                </div>
                <span
                  className="font-mono font-bold text-sm flex-shrink-0"
                  style={{
                    color: isPositive ? "var(--member)" : "var(--accent)",
                  }}
                >
                  {isPositive ? `+${tx.amount}` : tx.amount}
                </span>
              </div>
            );
          })
        )}
      </div>

      {/* 더 보기 */}
      {history.hasNextPage && (
        <div className="py-4 flex justify-center">
          <button
            onClick={() => history.fetchNextPage()}
            disabled={history.isFetchingNextPage}
            className="text-[12px] text-[var(--ink-soft)] underline disabled:opacity-50"
          >
            {history.isFetchingNextPage ? "불러오는 중…" : "더 보기"}
          </button>
        </div>
      )}

      <div className="h-20" />
    </div>
  );
}

// ── 페이지 (로딩 게이트) ──────────────────────────────────────────────────────

export default function CoinsPage() {
  const router = useRouter();

  const balance = useQuery({
    queryKey: ["coins", "balance"],
    queryFn: fetchCoinBalance,
  });

  useEffect(() => {
    if (balance.error?.message === "UNAUTHORIZED") {
      router.replace("/");
    }
  }, [balance.error, router]);

  if (balance.isPending) {
    return (
      <div className="flex items-center justify-center min-h-full text-sm text-[var(--ink-faint)]">
        로딩 중…
      </div>
    );
  }

  if (balance.isError) {
    return (
      <div className="flex items-center justify-center min-h-full text-sm text-[var(--ink-faint)]">
        잔액을 불러오지 못했어요
      </div>
    );
  }

  // balance.data 확정 후 CoinContent 마운트
  // → useCountdown lazy initializer가 실제 nextCoinAt으로 초기화됨
  return <CoinContent balance={balance.data} />;
}
