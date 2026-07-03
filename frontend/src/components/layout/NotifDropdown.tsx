"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";

interface NotificationItem {
  notificationId: number;
  type: "COLLEAGUE_REGISTERED" | "CHEMISTRY_REPORT";
  referenceId: number;
  message: string;
  createdAt: string;
}

// me/notifications/page.tsx와 동일 규칙 — API 응답의 referenceId 의미가 같아서 로직도 같음
function resolveTarget(notif: NotificationItem): string {
  if (notif.type === "CHEMISTRY_REPORT")
    return `/chemistry/${notif.referenceId}`;
  if (notif.type === "COLLEAGUE_REGISTERED") return `/colleagues`;
  return "/";
}

function typeIcon(type: NotificationItem["type"]): string {
  return type === "CHEMISTRY_REPORT" ? "🤝" : "👥";
}

async function deleteNotification(id: number): Promise<void> {
  await fetch(`/api/v1/notifications/${id}`, {
    method: "DELETE",
    credentials: "include",
  });
}

export function NotifDropdown({
  notifications,
  onClose,
}: {
  notifications: NotificationItem[];
  onClose: () => void;
}) {
  const router = useRouter();
  const queryClient = useQueryClient();

  const deleteMutation = useMutation({
    mutationFn: deleteNotification,
    onSuccess: (_, id) => {
      queryClient.setQueryData<{ notifications: NotificationItem[] }>(
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

  function handleClick(notif: NotificationItem) {
    deleteMutation.mutate(notif.notificationId);
    onClose();
    router.push(resolveTarget(notif));
  }

  const preview = notifications.slice(0, 4);

  return (
    <>
      {/* scrim — 바깥 클릭 시 닫힘 */}
      <div className="fixed inset-0 z-40" onClick={onClose} />

      <div className="absolute right-0 top-10 z-50 w-72 bg-white border border-line rounded-xl shadow-dialog overflow-hidden">
        {preview.length === 0 ? (
          <div className="py-8 text-center text-[12px] text-ink-faint">
            아직 알림이 없어요
          </div>
        ) : (
          <div className="max-h-80 overflow-y-auto divide-y divide-line-soft">
            {preview.map((n) => (
              <button
                key={n.notificationId}
                onClick={() => handleClick(n)}
                className="w-full flex items-start gap-2 px-3 py-2.5 text-left hover:bg-paper-2"
              >
                <span className="text-base leading-none mt-0.5">
                  {typeIcon(n.type)}
                </span>
                <span className="text-[12px] text-ink leading-snug">
                  {n.message}
                </span>
              </button>
            ))}
          </div>
        )}
        <button
          onClick={() => {
            onClose();
            router.push("/me/notifications");
          }}
          className="w-full py-2.5 text-[12px] font-semibold text-ink-soft border-t border-line-soft"
        >
          전체 알림 보기 ›
        </button>
      </div>
    </>
  );
}
