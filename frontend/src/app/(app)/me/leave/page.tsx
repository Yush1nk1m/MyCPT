"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useMe } from "@/hooks/useMe";
import { useWithdrawalInfo } from "@/hooks/useWithdrawal";
import { useAuthStore } from "@/stores/authStore";
import { useToast } from "@/hooks/useToast";
import { combineReason, CONFIRM_PHRASE } from "@/lib/withdrawal";

// wf-p4-me.jsx ⑧⑨ · screens.yaml me-leave-step1 / me-leave-step2 (auth: member)
const REASON_PRESETS = [
  "필요한 기능이 없어요",
  "검사 결과가 정확하지 않은 것 같아요",
  "자주 쓰지 않게 됐어요",
  "개인 정보가 걱정돼요",
  "새로운 계정으로 다시 시작하고 싶어요",
  "기타",
];

export default function MeLeavePage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const show = useToast();
  const clearAuth = useAuthStore((s) => s.clear);
  const { data: me } = useMe();
  const { data: info } = useWithdrawalInfo();

  const [selected, setSelected] = useState<string[]>([]);
  const [freeText, setFreeText] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [confirmText, setConfirmText] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function toggle(preset: string) {
    setSelected((prev) =>
      prev.includes(preset)
        ? prev.filter((p) => p !== preset)
        : [...prev, preset],
    );
  }

  const canWithdraw = confirmText === CONFIRM_PHRASE && !submitting;

  async function handleWithdraw() {
    if (!canWithdraw) return;
    setSubmitting(true);
    try {
      const res = await fetch("/api/v1/users/me", {
        method: "DELETE",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason: combineReason(selected, freeText) }),
      });
      if (!res.ok) throw new Error("WITHDRAW_FAILED");

      // 로그아웃과 동일 후처리 — useMe() 사용처(헤더 등)에 즉시 전파
      clearAuth();
      queryClient.setQueryData(["me"], null);
      show("탈퇴가 완료됐어요");
      router.push("/");
    } catch {
      show("탈퇴 처리에 실패했어요. 잠시 후 다시 시도해 주세요.");
      setSubmitting(false);
    }
  }

  const counts = info
    ? `검사 결과 ${info.resultCount}회 · 케미 보고서 ${info.chemistryCount}건`
    : "삭제 항목 확인 중…";

  return (
    <div className="flex flex-col min-h-full bg-paper">
      {/* 뒤로가기 바 */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-line">
        <button onClick={() => router.back()} className="text-ink p-1">
          ‹
        </button>
        <div>
          <p className="font-bold text-sm text-ink">회원탈퇴</p>
          <p className="text-xs text-ink-soft">Step 1 / 2 · 사유 선택</p>
        </div>
      </div>

      <div className="px-4 pt-5 pb-6 flex flex-col">
        <h2 className="text-lg font-extrabold text-ink mb-2">
          떠나기 전에, 알려주세요
        </h2>
        <p className="text-xs text-ink-soft leading-relaxed mb-5">
          더 나은 서비스를 만드는 데 큰 도움이 돼요. 익명으로 처리됩니다.
        </p>

        {/* 사유 체크리스트 */}
        <p className="text-xs font-semibold text-ink-faint mb-2">
          탈퇴 사유 (복수 선택)
        </p>
        <div className="flex flex-col gap-1.5 mb-5">
          {REASON_PRESETS.map((r) => {
            const checked = selected.includes(r);
            return (
              <button
                key={r}
                onClick={() => toggle(r)}
                className={[
                  "flex items-center gap-3 px-4 py-3 rounded-xl border text-left transition-colors",
                  checked
                    ? "border-ink bg-white"
                    : "border-line bg-white",
                ].join(" ")}
              >
                <span
                  className={[
                    "w-5 h-5 rounded-md border flex items-center justify-center text-xs shrink-0",
                    checked
                      ? "bg-ink text-white border-ink"
                      : "border-line text-transparent",
                  ].join(" ")}
                >
                  ✓
                </span>
                <span className="text-sm text-ink">{r}</span>
              </button>
            );
          })}
        </div>

        {/* 자유 입력 */}
        <p className="text-xs font-semibold text-ink-faint mb-2">
          자세히 알려주기 (선택)
        </p>
        <textarea
          value={freeText}
          onChange={(e) => setFreeText(e.target.value)}
          rows={3}
          placeholder="예: 검사 결과가 실제 나와 다르게 나오는 것 같아요…"
          className="bg-white border border-line rounded-xl px-4 py-3 text-sm text-ink outline-none placeholder:text-ink-faint resize-none"
        />

        {/* 삭제 항목 경고 */}
        <div className="mt-5 bg-danger-bg border border-danger rounded-xl px-4 py-3">
          <p className="text-sm font-bold text-danger mb-1">
            ⚠ 탈퇴 시 함께 삭제돼요
          </p>
          <ul className="list-disc pl-5 text-xs text-ink-soft leading-relaxed">
            <li>{counts}</li>
            <li>
              동료 관계 {info ? `${info.colleagueCount}명` : "…"} (상대방 목록에서도
              사라져요)
            </li>
            <li>코인 잔량 · 사용 이력</li>
          </ul>
          <p className="mt-1.5 text-xs font-semibold text-danger">
            한 번 삭제된 데이터는 복구할 수 없습니다.
          </p>
        </div>

        <button
          onClick={() => router.back()}
          className="mt-5 w-full py-3.5 rounded-2xl font-bold text-sm border border-line text-ink"
        >
          취소하고 돌아가기
        </button>
        <button
          onClick={() => setDialogOpen(true)}
          className="mt-2 w-full py-3.5 rounded-2xl font-bold text-sm bg-danger text-white"
        >
          다음 — 최종 확인 ›
        </button>
      </div>

      {/* Step 2 — 최종 확인 다이얼로그 */}
      {dialogOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-6">
          <div className="w-full max-w-sm bg-white rounded-2xl p-6 text-center">
            <div className="text-3xl mb-2">🚪</div>
            <h3 className="text-base font-extrabold text-danger mb-2">
              정말로 탈퇴할까요?
            </h3>
            <p className="text-xs text-ink-soft leading-relaxed mb-4">
              <b className="text-ink">{me?.nickname ?? "회원"}</b>님의 모든
              데이터가 <b className="text-danger">영구 삭제</b>됩니다.
              <br />
              진행하려면 아래에{" "}
              <b className="text-ink">&quot;{CONFIRM_PHRASE}&quot;</b>를 입력해
              주세요.
            </p>

            <input
              type="text"
              value={confirmText}
              onChange={(e) => setConfirmText(e.target.value)}
              placeholder={CONFIRM_PHRASE}
              className="w-full bg-paper-2 border border-line rounded-xl px-3 py-2.5 text-sm text-ink text-center outline-none placeholder:text-ink-faint mb-4"
            />

            <div className="flex flex-col gap-2">
              <button
                onClick={handleWithdraw}
                disabled={!canWithdraw}
                className={[
                  "w-full py-3 rounded-2xl font-bold text-sm transition-opacity",
                  canWithdraw
                    ? "bg-danger text-white"
                    : "bg-danger text-white opacity-30",
                ].join(" ")}
              >
                {submitting ? "처리 중…" : "탈퇴 진행"}
              </button>
              <button
                onClick={() => {
                  setDialogOpen(false);
                  setConfirmText("");
                }}
                className="w-full py-3 rounded-2xl font-bold text-sm border border-line text-ink"
              >
                계정 유지하기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
