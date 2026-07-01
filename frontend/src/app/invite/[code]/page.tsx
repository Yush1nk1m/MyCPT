"use client";

import { use, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useMe } from "@/hooks/useMe";

// ── 타입 ──────────────────────────────────────────────────────────────────────

interface InviteInfoResponse {
  inviterId: number;
  nickname: string;
  profileImageUrl: string | null;
}

interface ColleagueResponse {
  partnerId: number;
  nickname: string;
  profileImageUrl: string | null;
  connectedAt: string;
}

// ── fetch ─────────────────────────────────────────────────────────────────────

async function fetchInviteInfo(code: string): Promise<InviteInfoResponse> {
  const res = await fetch(`/api/v1/colleagues/invite/${code}`, {
    credentials: "include",
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

async function postColleague(code: string): Promise<ColleagueResponse> {
  const res = await fetch("/api/v1/colleagues", {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code }),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

// ── 아바타 (chemistry/[id]/page.tsx와 동일 패턴 — 로컬 컴포넌트) ──────────────

function Avatar({
  nickname,
  profileImageUrl,
}: {
  nickname: string;
  profileImageUrl: string | null;
}) {
  if (profileImageUrl) {
    return (
      <img
        src={profileImageUrl}
        alt={nickname}
        className="w-20 h-20 rounded-full object-cover border border-[var(--line)]"
      />
    );
  }
  return (
    <div className="w-20 h-20 rounded-full bg-[var(--paper-2)] border border-[var(--line)] flex items-center justify-center text-[24px] font-bold text-[var(--ink-soft)]">
      {nickname.charAt(0)}
    </div>
  );
}

// ── 에러 코드 → 안내 문구 ────────────────────────────────────────────────────

function errorMessage(code: string): string {
  if (code === "EXPIRED_CODE") return "초대장이 만료됐어요";
  if (code === "SELF_INVITE") return "본인의 초대장이에요";
  if (code === "ALREADY_COLLEAGUE") return "이미 동료로 등록된 사람이에요";
  if (code === "NOT_FOUND") return "존재하지 않는 초대장이에요";
  return "잠시 후 다시 시도해 주세요";
}

// ── 페이지 ────────────────────────────────────────────────────────────────────

export default function InviteAcceptPage({
  params,
}: {
  params: Promise<{ code: string }>;
}) {
  const { code } = use(params);
  const router = useRouter();
  const { data: me, isLoading: meLoading } = useMe();
  const [registerError, setRegisterError] = useState<string | null>(null);
  const [registered, setRegistered] = useState(false);

  const invite = useQuery({
    queryKey: ["invite-info", code],
    queryFn: () => fetchInviteInfo(code),
    retry: false,
  });

  const registerMutation = useMutation({
    mutationFn: () => postColleague(code),
    onSuccess: () => setRegistered(true),
    onError: (e: Error) => setRegisterError(e.message),
  });

  // 로그인 완료 후 이 페이지로 돌아왔을 때(returnTo) 자동 등록 트리거
  // — screens.yaml notes: "카카오 로그인 + 자동 동료 등록"
  useEffect(() => {
    if (
      me &&
      invite.data &&
      !registerMutation.isPending &&
      !registerMutation.isSuccess &&
      !registerError
    ) {
      registerMutation.mutate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [me, invite.data]);

  function handleKakaoLogin() {
    window.location.href = `/api/v1/auth/kakao?returnTo=/invite/${code}`;
  }

  // 로딩
  if (meLoading || invite.isPending) {
    return (
      <div className="flex flex-col items-center gap-4 px-6 pt-24">
        <div className="w-20 h-20 rounded-full bg-[var(--paper-2)] animate-pulse" />
        <div className="h-4 w-32 rounded bg-[var(--paper-2)] animate-pulse" />
      </div>
    );
  }

  // 초대 정보 조회 실패 (만료 / 자기초대 / 코드 없음)
  if (invite.isError) {
    return (
      <div className="flex flex-col items-center gap-2 px-6 pt-24 text-center">
        <p className="text-[15px] font-bold text-[var(--ink)]">
          {errorMessage(invite.error.message)}
        </p>
        <button
          onClick={() => router.push("/")}
          className="mt-4 text-[12px] text-[var(--accent)] underline"
        >
          홈으로 가기
        </button>
      </div>
    );
  }

  const inviter = invite.data!;

  // 등록 완료
  if (registered) {
    return (
      <div className="flex flex-col items-center gap-3 px-6 pt-24 text-center">
        <Avatar
          nickname={inviter.nickname}
          profileImageUrl={inviter.profileImageUrl}
        />
        <p className="text-[15px] font-bold text-[var(--ink)]">
          {inviter.nickname}님과 동료가 됐어요 🎉
        </p>
        <button
          onClick={() => router.push(`/colleagues/${inviter.inviterId}`)}
          className="mt-3 px-6 py-2.5 rounded-pill bg-[var(--ink)] text-white text-[13px] font-bold"
        >
          동료 프로필 보기
        </button>
      </div>
    );
  }

  // 등록 실패 (ALREADY_COLLEAGUE 등 — POST 시점에 발견)
  if (registerError) {
    return (
      <div className="flex flex-col items-center gap-2 px-6 pt-24 text-center">
        <Avatar
          nickname={inviter.nickname}
          profileImageUrl={inviter.profileImageUrl}
        />
        <p className="text-[15px] font-bold text-[var(--ink)] mt-2">
          {errorMessage(registerError)}
        </p>
        <button
          onClick={() => router.push("/colleagues")}
          className="mt-4 text-[12px] text-[var(--accent)] underline"
        >
          동료 목록으로 가기
        </button>
      </div>
    );
  }

  // 회원 — 로그인 후 자동 등록 진행 중
  if (me) {
    return (
      <div className="flex flex-col items-center gap-3 px-6 pt-24 text-center">
        <Avatar
          nickname={inviter.nickname}
          profileImageUrl={inviter.profileImageUrl}
        />
        <p className="text-[15px] font-bold text-[var(--ink)]">
          {inviter.nickname}님과 동료로 등록하는 중…
        </p>
      </div>
    );
  }

  // 비회원 — 초대자 프로필 + 혜택 + 카카오 로그인 CTA
  const benefits = [
    "서로의 DISC 성향을 확인할 수 있어요",
    "함께 케미 보고서를 받아볼 수 있어요",
    "언제든 동료 목록에서 관계를 관리할 수 있어요",
  ];

  return (
    <div className="flex flex-col items-center gap-4 px-6 pt-20 pb-10 max-w-[400px] mx-auto">
      <Avatar
        nickname={inviter.nickname}
        profileImageUrl={inviter.profileImageUrl}
      />
      <p className="text-[16px] font-bold text-[var(--ink)] text-center">
        {inviter.nickname}님이 MyCPT 동료로 초대했어요
      </p>

      <div className="w-full flex flex-col gap-2.5 mt-2">
        {benefits.map((b) => (
          <div
            key={b}
            className="flex items-start gap-2 text-[13px] text-[var(--ink-soft)]"
          >
            <span className="text-[var(--accent)]">✓</span>
            <span>{b}</span>
          </div>
        ))}
      </div>

      <button
        onClick={handleKakaoLogin}
        className="w-full mt-4 py-3 rounded-pill flex items-center justify-center gap-2 font-semibold text-sm"
        style={{ background: "var(--kakao)", color: "var(--kakao-ink)" }}
      >
        <span>💬</span>
        <span>카카오로 시작하고 동료 등록하기</span>
      </button>

      <span className="text-[10.5px] text-[var(--ink-faint)] font-mono mt-2">
        초대 코드: {code}
      </span>
    </div>
  );
}
