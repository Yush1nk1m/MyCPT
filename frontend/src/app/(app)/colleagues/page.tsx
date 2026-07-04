// frontend/src/app/colleagues/page.tsx
"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useToast } from "@/hooks/useToast";
import Link from "next/link";

// ── 타입 ──────────────────────────────────────────────────────────────────────

interface PeerCodeResponse {
  code: string;
  expiresAt: string;
}

interface ColleagueResponse {
  partnerId: number; // 상대방 userId
  nickname: string;
  profileImageUrl: string | null;
  connectedAt: string;
}

interface ColleagueListResponse {
  colleagues: ColleagueResponse[];
}

// ── fetch ─────────────────────────────────────────────────────────────────────

async function fetchPeerCode(): Promise<PeerCodeResponse> {
  const res = await fetch("/api/v1/peer-code", { credentials: "include" });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) throw new Error("FETCH_ERROR");
  return res.json();
}

async function refreshPeerCode(): Promise<PeerCodeResponse> {
  const res = await fetch("/api/v1/peer-code/refresh", {
    method: "POST",
    credentials: "include",
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) throw new Error("FETCH_ERROR");
  return res.json();
}

async function fetchColleagues(): Promise<ColleagueListResponse> {
  const res = await fetch("/api/v1/colleagues", { credentials: "include" });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  if (!res.ok) throw new Error("FETCH_ERROR");
  return res.json();
}

async function postColleague(code: string): Promise<ColleagueResponse> {
  const res = await fetch("/api/v1/colleagues", {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code }),
  });
  if (res.status === 401) throw new Error("UNAUTHORIZED");
  // 에러 응답 바디에서 code 추출 (message는 백엔드 범용 문구라 화면에 직접 노출 안 함)
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

// ── 유틸 ─────────────────────────────────────────────────────────────────────

// 서버 에러 코드 → 서비스 말투 안내 문구
// POST /colleagues가 던질 수 있는 코드: NOT_FOUND, EXPIRED_CODE, SELF_INVITE, ALREADY_COLLEAGUE
function errorMessage(code: string): string {
  if (code === "NOT_FOUND") return "동료 코드가 유효하지 않아요.";
  if (code === "EXPIRED_CODE") return "만료된 코드예요.";
  if (code === "SELF_INVITE") return "본인의 코드는 사용할 수 없어요.";
  if (code === "ALREADY_COLLEAGUE") return "이미 동료로 등록된 코드예요.";
  return "등록에 실패했어요";
}

// 코드 포맷: 입력값을 MYCPT-XXXX-XXX 형태로 자동 변환
function formatCode(raw: string): string {
  return raw
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, "")
    .slice(0, 8);
}

// 코드 유효성: 대문자+숫자 8자리
const CODE_PATTERN = /^[A-Z0-9]{8}$/;

function avatarLetter(nickname: string): string {
  return nickname.charAt(0).toUpperCase();
}

// ── 동료 등록 바텀시트 ────────────────────────────────────────────────────────

function RegisterSheet({
  myCode,
  onClose,
  onSuccess,
}: {
  myCode: string;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [input, setInput] = useState("");
  const showToast = useToast();

  const mutation = useMutation({
    mutationFn: postColleague,
    onSuccess: () => {
      onSuccess();
      onClose();
    },
    onError: (e: Error) => {
      showToast(errorMessage(e.message));
    },
  });

  function handleChange(value: string) {
    setInput(formatCode(value));
  }

  function handleSubmit() {
    if (!CODE_PATTERN.test(input)) {
      showToast("올바른 형식의 코드를 입력해 주세요.");
      return;
    }
    mutation.mutate(input);
  }

  function handleCopyMyCode() {
    navigator.clipboard.writeText(myCode);
  }

  return (
    <>
      {/* 스크림 */}
      <div
        className="fixed inset-0 z-40"
        style={{ background: "oklch(0.2 0.01 250 / 0.4)" }}
        onClick={onClose}
      />

      {/* 시트 */}
      <div className="fixed bottom-0 left-0 right-0 z-50 bg-white rounded-t-[22px] shadow-xl flex flex-col max-h-[92%]">
        {/* 핸들 */}
        <div className="flex justify-center pt-3 pb-1">
          <div className="w-10 h-1 rounded-full bg-[var(--line)]" />
        </div>

        {/* 헤더 */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-[var(--line-soft)]">
          <button
            onClick={onClose}
            className="w-7 text-[var(--ink-soft)] text-lg"
          >
            ✕
          </button>
          <span className="text-[13.5px] font-bold text-[var(--ink)]">
            동료 등록
          </span>
          <div className="w-7" />
        </div>

        <div className="p-4 flex flex-col gap-5 overflow-y-auto">
          {/* ① 친구 코드 입력 */}
          <div className="flex flex-col gap-2">
            <span className="text-[12px] font-semibold text-[var(--ink-soft)]">
              ① 친구가 알려준 코드 입력
            </span>
            <div className="flex items-center justify-between border-[1.5px] border-[var(--ink)] rounded-[10px] px-3.5 py-3">
              <input
                className="flex-1 font-mono text-[15px] font-bold tracking-widest bg-transparent outline-none text-[var(--ink)] placeholder:text-[var(--ink-faint)]"
                placeholder="□□□□□□□□"
                value={input}
                onChange={(e) => handleChange(e.target.value)}
                maxLength={8}
                autoComplete="off"
                spellCheck={false}
              />
              <button
                className="text-[10px] text-[var(--ink-faint)] font-medium ml-2"
                onClick={async () => {
                  const text = await navigator.clipboard
                    .readText()
                    .catch(() => "");
                  if (text) handleChange(text);
                }}
              >
                붙여넣기
              </button>
            </div>
            <button
              onClick={handleSubmit}
              disabled={mutation.isPending}
              className="w-full py-3 rounded-[10px] bg-[var(--ink)] text-white text-[13px] font-bold disabled:opacity-50"
            >
              {mutation.isPending ? "등록 중…" : "동료로 등록하기"}
            </button>
          </div>

          {/* 구분선 */}
          <div className="flex items-center gap-3">
            <div className="flex-1 h-px bg-[var(--line-soft)]" />
            <span className="text-[11px] text-[var(--ink-faint)]">또는</span>
            <div className="flex-1 h-px bg-[var(--line-soft)]" />
          </div>

          {/* ② 내 코드 공유 */}
          <div className="flex flex-col gap-2">
            <span className="text-[12px] font-semibold text-[var(--ink-soft)]">
              ② 내 코드로 친구 초대
            </span>
            <div className="flex items-center justify-between px-3.5 py-3 rounded-[10px] border border-dashed border-[var(--line)] bg-[var(--paper-2)]">
              <span className="font-mono text-[14px] font-bold tracking-widest text-[var(--ink)]">
                {myCode}
              </span>
              <button
                onClick={handleCopyMyCode}
                className="text-[12px] font-semibold text-[var(--accent)] ml-2"
              >
                복사
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

// ── 페이지 ───────────────────────────────────────────────────────────────────

export default function ColleaguesPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [sheetOpen, setSheetOpen] = useState(false);
  const [copied, setCopied] = useState(false);

  const peerCode = useQuery({
    queryKey: ["peer-code"],
    queryFn: fetchPeerCode,
  });

  const colleagues = useQuery({
    queryKey: ["colleagues"],
    queryFn: fetchColleagues,
  });

  const refreshMutation = useMutation({
    mutationFn: refreshPeerCode,
    onSuccess: (data) => {
      queryClient.setQueryData(["peer-code"], data);
    },
  });

  // UNAUTHORIZED 처리
  useEffect(() => {
    if (
      peerCode.error?.message === "UNAUTHORIZED" ||
      colleagues.error?.message === "UNAUTHORIZED"
    ) {
      router.replace("/");
    }
  }, [peerCode.error, colleagues.error, router]);

  if (
    peerCode.error?.message === "UNAUTHORIZED" ||
    colleagues.error?.message === "UNAUTHORIZED"
  ) {
    return null;
  }

  const myCode = peerCode.data?.code ?? "";
  const list = colleagues.data?.colleagues ?? [];

  function handleCopyCode() {
    navigator.clipboard.writeText(myCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div className="flex flex-col min-h-full bg-[var(--paper)]">
      {/* 탭 헤더 */}
      <div className="flex border-b border-[var(--line)] bg-white sticky top-0 z-10">
        <div className="flex-1 py-3 text-center text-[13px] font-bold text-[var(--ink)] border-b-2 border-[var(--ink)]">
          동료 목록
        </div>
        <Link
          href="/chemistry"
          className="flex-1 py-3 text-center text-[13px] text-[var(--ink-soft)]"
        >
          보고서 목록
        </Link>
      </div>

      {/* 내 동료 코드 카드 */}
      <div className="px-4 pt-4">
        <div className="bg-white border-[1.5px] border-[var(--ink)] rounded-[14px] p-4 flex flex-col gap-3">
          <div className="flex items-baseline justify-between">
            <span className="text-[11px] text-[var(--ink-faint)] font-mono tracking-widest uppercase">
              내 동료 코드
            </span>
            <button
              onClick={() => refreshMutation.mutate()}
              disabled={refreshMutation.isPending}
              className="text-[10.5px] font-semibold text-[var(--accent)] disabled:opacity-50"
            >
              {refreshMutation.isPending ? "갱신 중…" : "↻ 새 코드"}
            </button>
          </div>

          {/* 코드 표시 */}
          <div className="flex items-center justify-between bg-[var(--paper-2)] border border-dashed border-[var(--line)] rounded-[10px] px-3.5 py-2.5">
            {peerCode.isPending ? (
              <div className="h-5 w-36 bg-[var(--line-soft)] rounded animate-pulse" />
            ) : (
              <span className="font-mono text-[15px] font-bold tracking-widest text-[var(--ink)]">
                {myCode}
              </span>
            )}
            <button
              onClick={handleCopyCode}
              className="text-[12px] font-semibold text-[var(--accent)] ml-2"
            >
              {copied ? "복사됨" : "복사"}
            </button>
          </div>

          {/* 액션 버튼 */}
          <div className="grid grid-cols-2 gap-2">
            <button
              className="py-2.5 rounded-[10px] text-[12px] font-bold text-[var(--ink)] border border-[var(--line)]"
              style={{ background: "#FEE500" }}
              onClick={() => {
                // 카카오톡 공유 — KakaoSDK 미연동 시 URL 복사로 대체
                navigator.clipboard.writeText(
                  `안녕하세요! MyCPT 동료 코드를 알려드릴게요: ${myCode}`,
                );
              }}
            >
              💬 카카오톡 공유
            </button>
            <button
              onClick={() => setSheetOpen(true)}
              className="py-2.5 rounded-[10px] text-[12px] font-bold text-white bg-[var(--ink)]"
            >
              + 친구 코드 입력
            </button>
          </div>
        </div>
      </div>

      {/* 동료 목록 */}
      <div className="text-[10.5px] text-[var(--ink-faint)] font-mono tracking-widest uppercase px-4 pt-5 pb-2">
        내 동료 · {list.length}명
      </div>

      <div className="px-4 flex flex-col gap-2">
        {colleagues.isPending ? (
          [0, 1, 2].map((i) => (
            <div
              key={i}
              className="h-16 bg-white rounded-[12px] border-[1.5px] border-[var(--line)] animate-pulse"
            />
          ))
        ) : list.length === 0 ? (
          <div className="py-12 text-center text-sm text-[var(--ink-faint)]">
            아직 등록된 동료가 없어요
            <br />
            <button
              onClick={() => setSheetOpen(true)}
              className="mt-3 text-[12px] text-[var(--accent)] underline"
            >
              친구 코드 입력하기
            </button>
          </div>
        ) : (
          list.map((c) => (
            <Link
              key={c.partnerId}
              href={`/colleagues/${c.partnerId}`}
              className="bg-white border-[1.5px] border-[var(--ink)] rounded-[12px] px-3.5 py-3 grid items-center gap-3"
              style={{ gridTemplateColumns: "auto 1fr" }}
            >
              {/* 아바타 */}
              <div
                className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold text-white"
                style={{ background: "var(--member)" }}
              >
                {c.profileImageUrl ? (
                  <img
                    src={c.profileImageUrl}
                    alt={c.nickname}
                    className="w-10 h-10 rounded-full object-cover"
                  />
                ) : (
                  avatarLetter(c.nickname)
                )}
              </div>

              {/* 정보 */}
              <div className="flex flex-col min-w-0">
                <span className="text-[13.5px] font-bold text-[var(--ink)] leading-snug">
                  {c.nickname}
                </span>
                <span className="text-[11px] text-[var(--ink-soft)]">
                  동료 등록{" "}
                  {new Date(c.connectedAt).toLocaleDateString("ko-KR")}
                </span>
              </div>
            </Link>
          ))
        )}
      </div>

      {/* 바텀시트 */}
      {sheetOpen && (
        <RegisterSheet
          myCode={myCode}
          onClose={() => setSheetOpen(false)}
          onSuccess={() =>
            queryClient.invalidateQueries({ queryKey: ["colleagues"] })
          }
        />
      )}

      <div className="h-20" />
    </div>
  );
}
