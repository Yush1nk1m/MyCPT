"use client";

import { useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { ReportMarkdown } from "@/components/disc/ReportMarkdown";
import { useToast } from "@/hooks/useToast";

interface UserInfo {
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
}

interface ChemistryReportDetail {
  reportId: number;
  requester: UserInfo;
  partner: UserInfo;
  myRole: "REQUESTER" | "PARTNER";
  status: "GENERATING" | "READY" | "ERROR";
  report: string | null;
  createdAt: string;
}

async function fetchChemistryReport(
  id: string,
): Promise<ChemistryReportDetail> {
  const res = await fetch(`/api/v1/chemistry-reports/${id}`, {
    credentials: "include",
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (res.status === 403) throw new Error("FORBIDDEN");
  if (res.status === 404) throw new Error("NOT_FOUND");
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

function Avatar({ user }: { user: UserInfo }) {
  if (user.profileImageUrl) {
    return (
      <img
        src={user.profileImageUrl}
        alt={user.nickname}
        className="w-12 h-12 rounded-full object-cover border border-[var(--line)]"
      />
    );
  }
  return (
    <div className="w-12 h-12 rounded-full bg-[var(--paper-2)] border border-[var(--line)] flex items-center justify-center text-[13px] font-bold text-[var(--ink-soft)]">
      {user.nickname.charAt(0)}
    </div>
  );
}

function AvatarHero({
  me,
  peer,
  date,
}: {
  me: UserInfo;
  peer: UserInfo;
  date?: string;
}) {
  return (
    <div className="bg-white border-b border-[var(--line)] px-4 py-6 flex flex-col items-center gap-4">
      <div className="flex items-center gap-4">
        <div className="flex flex-col items-center gap-1.5">
          <Avatar user={me} />
          <span className="text-[11px] text-[var(--ink-soft)]">나</span>
        </div>
        <span className="text-[var(--ink-faint)] text-base">↔</span>
        <div className="flex flex-col items-center gap-1.5">
          <Avatar user={peer} />
          <span className="text-[11px] text-[var(--ink-soft)]">
            {peer.nickname}
          </span>
        </div>
      </div>
      {date && (
        <span className="text-[11px] text-[var(--ink-faint)] font-mono">
          {formatDate(date)}
        </span>
      )}
    </div>
  );
}

export default function ChemistryDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const router = useRouter();
  const showToast = useToast();

  const report = useQuery({
    queryKey: ["chemistry-report", id],
    queryFn: () => fetchChemistryReport(id),
  });

  const isGenerating = report.data?.status === "GENERATING";

  useEffect(() => {
    if (!report.error) return;
    if (report.error.message === "UNAUTHORIZED") {
      router.replace("/");
    } else if (
      report.error.message === "FORBIDDEN" ||
      report.error.message === "NOT_FOUND"
    ) {
      router.replace("/chemistry");
    }
  }, [report.error, router]);

  // GENERATING 직접 접근 — toast 세팅만 (전역 store 업데이트, ESLint 경고 없음)
  useEffect(() => {
    if (!isGenerating) return;
    showToast("아직 보고서가 생성 중이에요.");
  }, [isGenerating, showToast]);

  useEffect(() => {
    if (!isGenerating) return;
    const timer = setTimeout(() => router.replace("/chemistry"), 2000);
    return () => clearTimeout(timer);
  }, [isGenerating, router]);

  const data = report.data;
  const me = data
    ? data.myRole === "REQUESTER"
      ? data.requester
      : data.partner
    : null;
  const peer = data
    ? data.myRole === "REQUESTER"
      ? data.partner
      : data.requester
    : null;

  return (
    <div className="min-h-full bg-[var(--paper)]">
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-[var(--line)] sticky top-0 z-10">
        <button
          onClick={() => router.back()}
          className="text-[var(--ink)] text-lg leading-none"
        >
          ‹
        </button>
        <span className="text-[14px] font-bold text-[var(--ink)]">
          케미 보고서
        </span>
      </div>

      {report.isPending && (
        <div className="px-4 pt-6 flex flex-col gap-4">
          <div className="flex items-center justify-center gap-4">
            <div className="w-12 h-12 rounded-full bg-[var(--paper-2)] animate-pulse" />
            <span className="text-[var(--ink-faint)] text-sm">↔</span>
            <div className="w-12 h-12 rounded-full bg-[var(--paper-2)] animate-pulse" />
          </div>
          <div className="flex flex-col gap-2 mt-4">
            {[80, 65, 90, 55, 70].map((w, i) => (
              <div
                key={i}
                className="h-3 rounded bg-[var(--paper-2)] animate-pulse"
                style={{ width: `${w}%` }}
              />
            ))}
          </div>
        </div>
      )}

      {data?.status === "READY" && me && peer && (
        <div className="pb-10">
          <AvatarHero me={me} peer={peer} date={data.createdAt} />
          <div className="px-4 pt-5">
            <ReportMarkdown report={data.report ?? ""} />
          </div>
        </div>
      )}

      {data?.status === "ERROR" && (
        <div className="px-4 pt-10 flex flex-col items-center gap-4 text-center">
          <p className="text-[14px] font-semibold text-[var(--ink)]">
            보고서 발행에 실패했어요
          </p>
          <p className="text-[12px] text-[var(--ink-soft)]">
            코인이 환불되었어요. 동료 프로필에서 다시 시도해 주세요.
          </p>
          <Link
            href="/chemistry"
            className="mt-2 text-[12px] text-[var(--accent)] underline"
          >
            보고서 목록으로 가기
          </Link>
        </div>
      )}

      <div className="h-20" />
    </div>
  );
}
