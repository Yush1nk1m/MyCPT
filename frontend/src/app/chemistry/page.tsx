"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useInfiniteQuery } from "@tanstack/react-query";
import Link from "next/link";

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

async function fetchChemistryReports(
  cursor: number | null,
): Promise<ChemistryReportListResponse> {
  const params = new URLSearchParams({ size: "20" });
  if (cursor) params.set("cursor", String(cursor));
  const res = await fetch(`/api/v1/chemistry-reports?${params}`, {
    credentials: "include",
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) throw new Error("FETCH_ERROR");
  return res.json();
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function peerLabel(report: ChemistryReportSummary): string {
  const partnerName =
    report.myRole === "REQUESTER"
      ? report.partnerNickname
      : report.requesterNickname;
  return `나 ↔ ${partnerName}`;
}

function GeneratingCard({ report }: { report: ChemistryReportSummary }) {
  return (
    <div className="bg-white border-[1.5px] border-dashed border-[var(--accent)] rounded-[12px] p-4 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <div className="flex flex-col gap-0.5 min-w-0">
          <span className="text-[13.5px] font-bold text-[var(--ink)] truncate">
            {peerLabel(report)}
          </span>
          <span className="text-[11px] text-[var(--ink-faint)] font-mono">
            {formatDate(report.createdAt)}
          </span>
        </div>
        <span className="shrink-0 text-[10px] font-bold text-[var(--accent)] border border-[var(--accent)] rounded-full px-2.5 py-0.5 animate-pulse">
          발행 중…
        </span>
      </div>
      <div className="flex flex-col gap-1.5">
        <div className="h-2 rounded bg-[var(--paper-2)] w-[88%]" />
        <div className="h-2 rounded bg-[var(--paper-2)] w-[65%]" />
      </div>
      <p className="text-[11.5px] text-[var(--ink-soft)]">
        완료되면 알림으로 알려드릴게요.
      </p>
    </div>
  );
}

function ReadyCard({ report }: { report: ChemistryReportSummary }) {
  return (
    <Link
      href={`/chemistry/${report.reportId}`}
      className="bg-white border-[1.5px] border-[var(--ink)] rounded-[12px] px-4 py-3.5 flex items-center gap-3"
    >
      <div className="flex flex-col flex-1 min-w-0 gap-0.5">
        <span className="text-[13.5px] font-bold text-[var(--ink)] truncate">
          {peerLabel(report)}
        </span>
        <span className="text-[11px] text-[var(--ink-soft)] font-mono">
          {formatDate(report.createdAt)}
        </span>
      </div>
      <span className="text-[var(--ink-faint)] text-sm shrink-0">›</span>
    </Link>
  );
}

export default function ChemistryPage() {
  const router = useRouter();

  const reports = useInfiniteQuery({
    queryKey: ["chemistry-reports"],
    queryFn: ({ pageParam }: { pageParam: number | null }) =>
      fetchChemistryReports(pageParam),
    initialPageParam: null,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.nextCursor : undefined,
  });

  useEffect(() => {
    if (reports.error?.message === "UNAUTHORIZED") {
      router.replace("/");
    }
  }, [reports.error, router]);

  const allReports = reports.data?.pages.flatMap((p) => p.reports) ?? [];

  return (
    <div className="min-h-screen bg-[var(--paper)]">
      <div className="flex border-b border-[var(--line)] bg-white sticky top-0 z-10">
        <Link
          href="/colleagues"
          className="flex-1 py-3 text-center text-[13px] font-semibold text-[var(--ink-soft)]"
        >
          동료 목록
        </Link>
        <div className="flex-1 py-3 text-center text-[13px] font-bold text-[var(--ink)] border-b-2 border-[var(--ink)]">
          보고서 목록
        </div>
      </div>

      <div className="px-4 pt-4 pb-8 flex flex-col gap-3">
        {reports.isPending ? (
          [0, 1, 2].map((i) => (
            <div
              key={i}
              className="h-20 rounded-[12px] bg-white border border-[var(--line)] animate-pulse"
            />
          ))
        ) : allReports.length === 0 ? (
          <div className="py-16 text-center">
            <p className="text-[14px] font-semibold text-[var(--ink-soft)] mb-1">
              아직 발행한 보고서가 없어요
            </p>
            <p className="text-[12px] text-[var(--ink-faint)]">
              동료 프로필에서 케미 보고서를 발행해 보세요
            </p>
            <Link
              href="/colleagues"
              className="mt-4 inline-block text-[12px] text-[var(--accent)] underline"
            >
              동료 목록으로 가기
            </Link>
          </div>
        ) : (
          allReports.map((r) =>
            r.status === "GENERATING" ? (
              <GeneratingCard key={r.reportId} report={r} />
            ) : (
              <ReadyCard key={r.reportId} report={r} />
            ),
          )
        )}

        {reports.hasNextPage && (
          <button
            onClick={() => reports.fetchNextPage()}
            disabled={reports.isFetchingNextPage}
            className="py-3 text-[12px] text-[var(--ink-soft)] underline disabled:opacity-50"
          >
            {reports.isFetchingNextPage ? "불러오는 중…" : "더 보기"}
          </button>
        )}
      </div>

      <div className="h-20" />
    </div>
  );
}
