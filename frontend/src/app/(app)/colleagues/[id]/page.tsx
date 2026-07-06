// frontend/src/app/colleagues/[id]/page.tsx
"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  useQuery,
  useMutation,
  useQueryClient,
  useInfiniteQuery,
} from "@tanstack/react-query";
import { useToast } from "@/hooks/useToast";
import Link from "next/link";

// ── 타입 ──────────────────────────────────────────────────────────────────────

interface ColleagueResponse {
  partnerId: number;
  nickname: string;
  profileImageUrl: string | null;
  connectedAt: string;
}

interface CoinBalanceResponse {
  coins: number;
  nextCoinAt: string | null;
}

interface ChemistryReportSummary {
  reportId: number;
  requesterId: number;
  requesterNickname: string;
  partnerId: number;
  partnerNickname: string;
  myRole: "REQUESTER" | "PARTNER";
  status: "GENERATING" | "READY" | "ERROR";
  createdAt: string;
}

interface ChemistryReportListResponse {
  reports: ChemistryReportSummary[];
  nextCursor: number | null;
  hasNext: boolean;
}

// ── fetch ─────────────────────────────────────────────────────────────────────

async function fetchColleague(id: string): Promise<ColleagueResponse> {
  const res = await fetch(`/api/v1/colleagues/${id}`, {
    credentials: "include",
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (res.status === 403) throw new Error("FORBIDDEN");
  if (res.status === 404) throw new Error("NOT_FOUND");
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

async function fetchCoinBalance(): Promise<CoinBalanceResponse> {
  const res = await fetch("/api/v1/coins", { credentials: "include" });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

async function fetchChemistryReports(
  partnerId: string,
  cursor: number | null,
): Promise<ChemistryReportListResponse> {
  const params = new URLSearchParams({ size: "5", partnerId });
  if (cursor) params.set("cursor", String(cursor));
  const res = await fetch(`/api/v1/chemistry-reports?${params}`, {
    credentials: "include",
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

async function postChemistryReport(partnerId: number): Promise<void> {
  const res = await fetch("/api/v1/chemistry-reports", {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ partnerId }),
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (res.status === 422) throw new Error("INSUFFICIENT_COINS");
  if (res.status === 403) throw new Error("FORBIDDEN");
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
}

async function deleteColleague(id: string): Promise<void> {
  const res = await fetch(`/api/v1/colleagues/${id}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
}

// ── 케미 발행 확인 모달 ───────────────────────────────────────────────────────

function formatNextCharge(nextCoinAt: string | null): string {
  if (!nextCoinAt) return "만충 상태";
  const diff = new Date(nextCoinAt).getTime() - Date.now();
  if (diff <= 0) return "곧 충전 예정";
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  return `약 ${h}시간 ${m}분 후`;
}

function ChemistryConfirmModal({
  coins,
  nextCoinAt,
  onConfirm,
  onClose,
  isPending,
}: {
  coins: number;
  nextCoinAt: string | null;
  onConfirm: () => void;
  onClose: () => void;
  isPending: boolean;
}) {
  const insufficient = coins === 0;

  // 다음 충전까지 남은 시간 (정적 표시 — 카운트다운은 coins 페이지에만)
  const [chargeLabel] = useState(() => formatNextCharge(nextCoinAt));

  return (
    <>
      <div
        className="fixed inset-0 z-40"
        style={{ background: "oklch(0.2 0.01 250 / 0.5)" }}
        onClick={onClose}
      />
      <div className="fixed inset-0 z-50 flex items-center justify-center px-6">
        <div className="bg-white rounded-[18px] w-full max-w-sm p-5 flex flex-col gap-4 shadow-xl">
          <div className="flex flex-col gap-1">
            <span className="text-[15px] font-bold text-[var(--ink)]">
              케미 보고서 발행
            </span>
            <span className="text-[12px] text-[var(--ink-soft)]">
              AI가 두 사람의 DISC 성향을 분석해 보고서를 생성합니다.
              <br />
              발행까지 약 30초~1분 소요돼요.
            </span>
          </div>

          {/* 코인 현황 표 */}
          <div className="rounded-[10px] border border-[var(--line-soft)] overflow-hidden text-[12.5px]">
            {[
              { label: "사용", value: "코인 1개" },
              {
                label: "잔량",
                value: `${coins}개`,
                accent: insufficient,
              },
              { label: "다음 충전까지", value: chargeLabel },
            ].map((row) => (
              <div
                key={row.label}
                className="flex justify-between items-center px-3.5 py-2.5 border-b border-[var(--line-soft)] last:border-0"
              >
                <span className="text-[var(--ink-soft)]">{row.label}</span>
                <span
                  className="font-semibold"
                  style={{
                    color: row.accent ? "var(--accent)" : "var(--ink)",
                  }}
                >
                  {row.value}
                </span>
              </div>
            ))}
          </div>

          {insufficient && (
            <p className="text-[11.5px] text-[var(--accent)] text-center">
              코인이 부족해요. 충전 후 다시 시도해 주세요.
            </p>
          )}

          <div className="flex gap-2">
            <button
              onClick={onClose}
              className="flex-1 py-2.5 rounded-[10px] border border-[var(--line)] text-[13px] text-[var(--ink-soft)]"
            >
              취소
            </button>
            <button
              onClick={onConfirm}
              disabled={insufficient || isPending}
              className="flex-1 py-2.5 rounded-[10px] bg-[var(--ink)] text-white text-[13px] font-bold disabled:opacity-40"
            >
              {isPending ? "발행 중…" : "발행하기"}
            </button>
          </div>
        </div>
      </div>
    </>
  );
}

// ── 페이지 ───────────────────────────────────────────────────────────────────

export default function ColleagueDetailPage() {
  const params = useParams();
  const id = params.id as string; // 상대방 userId
  const router = useRouter();
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const showToast = useToast();

  const colleague = useQuery({
    queryKey: ["colleague", id],
    queryFn: () => fetchColleague(id),
  });

  const coinBalance = useQuery({
    queryKey: ["coins", "balance"],
    queryFn: fetchCoinBalance,
  });

  const reports = useInfiniteQuery({
    queryKey: ["chemistry-reports", id],
    queryFn: ({ pageParam: cursor }: { pageParam: number | null }) =>
      fetchChemistryReports(id, cursor),
    initialPageParam: null,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.nextCursor : undefined,
  });

  const issueMutation = useMutation({
    mutationFn: () => postChemistryReport(Number(id)),
    onSuccess: () => {
      setModalOpen(false);
      // 코인 잔량 갱신
      queryClient.invalidateQueries({ queryKey: ["coins", "balance"] });
      // 보고서 목록 갱신
      queryClient.invalidateQueries({ queryKey: ["chemistry-reports", id] });
      router.push("/chemistry");
    },
    onError: (e: Error) => {
      setModalOpen(false);
      if (e.message === "INSUFFICIENT_COINS") {
        showToast("코인이 부족해요.");
      } else if (e.message === "NO_RESULT") {
        showToast(
          "두 사람 모두 DISC 검사를 완료해야 케미 보고서를 만들 수 있어요.",
        );
      } else {
        showToast("발행에 실패했어요. 다시 시도해 주세요.");
      }
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteColleague(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["colleagues"] });
      router.replace("/colleagues");
    },
  });

  // UNAUTHORIZED → / 리다이렉트 (렌더 중 setState 방지를 위해 useEffect로 분리)
  useEffect(() => {
    if (
      colleague.error?.message === "UNAUTHORIZED" ||
      coinBalance.error?.message === "UNAUTHORIZED"
    ) {
      router.replace("/");
    }
  }, [colleague.error, coinBalance.error, router]);

  if (
    colleague.error?.message === "UNAUTHORIZED" ||
    coinBalance.error?.message === "UNAUTHORIZED"
  ) {
    return null;
  }

  if (colleague.error?.message === "NOT_FOUND") {
    return (
      <div className="flex items-center justify-center min-h-full text-sm text-[var(--ink-faint)]">
        존재하지 않는 동료예요
      </div>
    );
  }

  if (colleague.error?.message === "FORBIDDEN") {
    return (
      <div className="flex items-center justify-center min-h-full text-sm text-[var(--ink-faint)]">
        동료 관계가 아니에요
      </div>
    );
  }

  const allReports = reports.data?.pages.flatMap((p) => p.reports) ?? [];
  const coins = coinBalance.data?.coins ?? 0;

  function avatarLetter(nickname: string) {
    return nickname.charAt(0).toUpperCase();
  }

  return (
    <div className="flex flex-col min-h-full bg-[var(--paper)]">
      {/* BackBar */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-[var(--line)] sticky top-0 z-10">
        <Link
          href="/colleagues"
          className="text-[var(--ink-soft)] text-lg leading-none"
        >
          ‹
        </Link>
        <span className="flex-1 text-[13.5px] font-bold text-[var(--ink)]">
          {colleague.data?.nickname ?? "동료 프로필"}
        </span>

        {/* ⋯ 메뉴 */}
        <div className="relative">
          <button
            onClick={() => setMenuOpen((v) => !v)}
            className="text-[var(--ink-soft)] text-lg px-1"
          >
            ⋯
          </button>
          {menuOpen && (
            <>
              <div
                className="fixed inset-0 z-20"
                onClick={() => setMenuOpen(false)}
              />
              <div className="absolute right-0 top-8 z-30 bg-white border border-[var(--line)] rounded-[10px] shadow-lg overflow-hidden min-w-[120px]">
                <button
                  onClick={() => {
                    setMenuOpen(false);
                    if (confirm("동료를 삭제할까요?")) {
                      deleteMutation.mutate();
                    }
                  }}
                  className="w-full px-4 py-3 text-left text-[13px] text-[var(--accent)]"
                >
                  동료 삭제
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* 프로필 Hero */}
      <div className="bg-white border-b border-[var(--line)] px-4 py-6 flex flex-col items-center gap-3">
        {colleague.isPending ? (
          <div className="w-16 h-16 rounded-full bg-[var(--line-soft)] animate-pulse" />
        ) : (
          <>
            <div
              className="w-16 h-16 rounded-full flex items-center justify-center text-2xl font-bold text-white"
              style={{ background: "var(--member)" }}
            >
              {colleague.data?.profileImageUrl ? (
                <img
                  src={colleague.data.profileImageUrl}
                  alt={colleague.data.nickname}
                  className="w-16 h-16 rounded-full object-cover"
                />
              ) : (
                avatarLetter(colleague.data?.nickname ?? "?")
              )}
            </div>
            <span className="text-[17px] font-bold text-[var(--ink)]">
              {colleague.data?.nickname}
            </span>
          </>
        )}
      </div>

      {/* 케미 발행 카드 */}
      <div className="px-4 pt-4">
        <div className="bg-white border-[1.5px] border-[var(--ink)] rounded-[14px] p-4 flex flex-col gap-3">
          <div className="flex flex-col gap-0.5">
            <span className="text-[13.5px] font-bold text-[var(--ink)]">
              우리 잘 맞을까? 🤝
            </span>
            <span className="text-[11.5px] text-[var(--ink-soft)]">
              두 사람의 DISC 성향을 AI가 분석해 드려요 · 코인 1개 사용
            </span>
          </div>

          <button
            onClick={() => {
              setModalOpen(true);
            }}
            disabled={coins === 0 || coinBalance.isPending}
            className="w-full py-2.5 rounded-[10px] bg-[var(--ink)] text-white text-[13px] font-bold disabled:opacity-40"
          >
            {coins === 0
              ? `코인 부족 (잔량 ${coins}개)`
              : `케미 발행하기 (코인 ${coins}개 보유)`}
          </button>
        </div>
      </div>

      {/* 이전 보고서 목록 */}
      <div className="text-[10.5px] text-[var(--ink-faint)] font-mono tracking-widest uppercase px-4 pt-5 pb-2">
        케미 보고서 이력
      </div>

      <div className="bg-white">
        {reports.isPending ? (
          <div className="py-10 text-center text-sm text-[var(--ink-faint)]">
            로딩 중…
          </div>
        ) : allReports.length === 0 ? (
          <div className="py-10 text-center text-sm text-[var(--ink-faint)]">
            아직 발행한 보고서가 없어요
          </div>
        ) : (
          allReports.map((r) => (
            <Link
              key={r.reportId}
              href={`/chemistry/${r.reportId}`}
              className="flex items-center gap-3 px-4 py-3.5 border-t border-[var(--line-soft)]"
            >
              <div className="flex flex-col flex-1 min-w-0">
                <span className="text-[12.5px] text-[var(--ink)] font-medium leading-snug">
                  {r.status === "GENERATING" ? "발행 중…" : "케미 보고서"}
                </span>
                <span className="text-[11px] text-[var(--ink-soft)] mt-0.5">
                  {new Date(r.createdAt).toLocaleDateString("ko-KR")}
                </span>
              </div>
              {r.status === "GENERATING" && (
                <span className="text-[11px] text-[var(--ink-faint)] font-mono animate-pulse">
                  생성 중
                </span>
              )}
              <span className="text-[var(--ink-faint)] text-sm">›</span>
            </Link>
          ))
        )}
      </div>

      {reports.hasNextPage && (
        <div className="py-4 flex justify-center">
          <button
            onClick={() => reports.fetchNextPage()}
            disabled={reports.isFetchingNextPage}
            className="text-[12px] text-[var(--ink-soft)] underline disabled:opacity-50"
          >
            {reports.isFetchingNextPage ? "불러오는 중…" : "더 보기"}
          </button>
        </div>
      )}

      {/* 케미 발행 확인 모달 */}
      {modalOpen && (
        <ChemistryConfirmModal
          coins={coins}
          nextCoinAt={coinBalance.data?.nextCoinAt ?? null}
          onConfirm={() => issueMutation.mutate()}
          onClose={() => setModalOpen(false)}
          isPending={issueMutation.isPending}
        />
      )}

      <div className="h-20" />
    </div>
  );
}
