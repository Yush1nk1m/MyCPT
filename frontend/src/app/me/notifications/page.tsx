// frontend/src/app/me/notifications/page.tsx
"use client";

import { useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import Link from "next/link";

// ── 타입 ──────────────────────────────────────────────────────────────────────

interface NotificationResponse {
  notificationId: number;
  type: "COLLEAGUE_REGISTERED" | "CHEMISTRY_REPORT";
  referenceId: number;
  message: string;
  createdAt: string;
}

interface NotificationListResponse {
  notifications: NotificationResponse[];
}

// ── fetch ─────────────────────────────────────────────────────────────────────

async function fetchNotifications(): Promise<NotificationListResponse> {
  const res = await fetch("/api/v1/notifications", { credentials: "include" });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

async function deleteNotification(id: number): Promise<void> {
  const res = await fetch(`/api/v1/notifications/${id}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
}

// ── 유틸 ─────────────────────────────────────────────────────────────────────

// type별 클릭 시 이동 경로
// COLLEAGUE_REGISTERED: referenceId = colleague.id (현재 /colleagues로 이동, 향후 /colleagues/[id])
// CHEMISTRY_REPORT: referenceId = chemistryReport.id → /chemistry/[id]
function resolveTarget(notif: NotificationResponse): string {
  if (notif.type === "CHEMISTRY_REPORT")
    return `/chemistry/${notif.referenceId}`;
  if (notif.type === "COLLEAGUE_REGISTERED") return `/colleagues`;
  return "/";
}

function typeIcon(type: NotificationResponse["type"]): string {
  if (type === "CHEMISTRY_REPORT") return "🤝";
  if (type === "COLLEAGUE_REGISTERED") return "👥";
  return "ⓘ";
}

function formatDate(iso: string): string {
  const d = new Date(iso);
  const now = Date.now();
  const diff = now - d.getTime();

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return "방금 전";
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (days < 7) return `${days}일 전`;

  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
}

// ── 페이지 ───────────────────────────────────────────────────────────────────

export default function NotificationsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  const { data, isPending, isError, error } = useQuery({
    queryKey: ["notifications"],
    queryFn: fetchNotifications,
  });

  // 클릭 시 삭제 후 해당 referenceId로 이동
  const deleteMutation = useMutation({
    mutationFn: deleteNotification,
    onSuccess: (_, id) => {
      // 낙관적 업데이트: 목록에서 즉시 제거
      queryClient.setQueryData<NotificationListResponse>(
        ["notifications"],
        (old) =>
          old
            ? {
                notifications: old.notifications.filter(
                  (n) => n.notificationId !== id,
                ),
              }
            : old,
      );
    },
  });

  useEffect(() => {
    if (error?.message === "UNAUTHORIZED") {
      router.replace("/");
    }
  }, [error, router]);

  const notifications = data?.notifications ?? [];

  function handleClick(notif: NotificationResponse) {
    const target = resolveTarget(notif);
    // 삭제 후 이동 — 삭제 실패해도 이동은 진행
    deleteMutation.mutate(notif.notificationId);
    router.push(target);
  }

  return (
    <div className="flex flex-col min-h-screen bg-[var(--paper)]">
      {/* BackBar */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-[var(--line)] sticky top-0 z-10">
        <Link
          href="/me"
          className="text-[var(--ink-soft)] text-lg leading-none"
          aria-label="뒤로가기"
        >
          ‹
        </Link>
        <div className="flex flex-col min-w-0 flex-1">
          <span className="text-[13.5px] font-bold text-[var(--ink)] leading-tight">
            알림
          </span>
          {notifications.length > 0 && (
            <span className="text-[10.5px] text-[var(--ink-faint)] font-mono">
              {notifications.length}건
            </span>
          )}
        </div>
      </div>

      {/* 목록 */}
      <div className="bg-white flex-1">
        {isPending ? (
          <div className="py-16 text-center text-sm text-[var(--ink-faint)]">
            로딩 중…
          </div>
        ) : isError ? (
          <div className="py-16 text-center text-sm text-[var(--ink-faint)]">
            알림을 불러오지 못했어요
          </div>
        ) : notifications.length === 0 ? (
          <div className="py-16 text-center text-sm text-[var(--ink-faint)]">
            아직 알림이 없어요
          </div>
        ) : (
          notifications.map((notif) => (
            <button
              key={notif.notificationId}
              onClick={() => handleClick(notif)}
              disabled={deleteMutation.isPending}
              className="w-full flex items-center gap-3 px-4 py-3.5 border-t border-[var(--line-soft)] text-left disabled:opacity-60"
            >
              {/* 아이콘 */}
              <div className="w-8 h-8 rounded-lg bg-[var(--paper-2)] border border-[var(--line)] flex items-center justify-center text-sm flex-shrink-0">
                {typeIcon(notif.type)}
              </div>

              {/* 텍스트 */}
              <div className="flex flex-col min-w-0 flex-1">
                <span className="text-[12.5px] text-[var(--ink)] leading-snug font-medium">
                  {notif.message}
                </span>
                <span className="text-[11px] text-[var(--ink-soft)] mt-0.5">
                  {formatDate(notif.createdAt)}
                </span>
              </div>

              {/* 미확인 점 */}
              <span className="w-2 h-2 rounded-full bg-[var(--accent)] flex-shrink-0" />
            </button>
          ))
        )}
      </div>

      {/* 안내 */}
      {notifications.length > 0 && (
        <div className="py-5 text-center text-[11px] text-[var(--ink-faint)] font-mono">
          · 알림은 클릭 시 삭제됩니다 ·
        </div>
      )}

      <div className="h-20" />
    </div>
  );
}
