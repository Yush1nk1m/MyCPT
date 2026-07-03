"use client";

import Link from "next/link";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useMe } from "@/hooks/useMe";
import { NotifDropdown } from "./NotifDropdown";

async function fetchNotifications() {
  const res = await fetch("/api/v1/notifications", { credentials: "include" });
  if (!res.ok) return { notifications: [] };
  return res.json();
}

export function Header() {
  const { data: me } = useMe();
  const isAuthenticated = !!me;
  const [bellOpen, setBellOpen] = useState(false);

  // queryKey ["notifications"]는 /me/notifications 페이지와 공유 — 캐시 재사용, 중복 호출 없음
  const { data } = useQuery({
    queryKey: ["notifications"],
    queryFn: fetchNotifications,
    enabled: isAuthenticated,
    staleTime: 1000 * 30,
  });
  const notifications = data?.notifications ?? [];

  return (
    <header
      className="fixed top-0 left-0 right-0 z-30 flex items-center justify-between px-4 bg-white border-b border-line"
      style={{ height: "var(--header-height)" }}
    >
      <Link href="/" className="font-bold text-base text-ink">
        MyCPT
      </Link>

      {isAuthenticated && me ? (
        <div className="flex items-center gap-2">
          {/* 코인 알약 — 카운트다운은 /me/coins 전용, 헤더는 잔량만 */}
          <Link
            href="/me/coins"
            className="flex items-center gap-1 px-2.5 py-1 rounded-pill border border-line text-[11px] font-semibold text-ink-soft"
          >
            ⊙ {me.coins}
          </Link>

          <div className="relative">
            <button
              onClick={() => setBellOpen((v) => !v)}
              className="relative w-8 h-8 flex items-center justify-center rounded-full"
              aria-label="알림"
            >
              🔔
              {notifications.length > 0 && (
                <span className="absolute top-1 right-1.5 w-2 h-2 rounded-full bg-accent" />
              )}
            </button>
            {bellOpen && (
              <NotifDropdown
                notifications={notifications}
                onClose={() => setBellOpen(false)}
              />
            )}
          </div>

          <Link
            href="/me"
            className="flex items-center gap-1.5 pl-2.5 pr-1 py-1 rounded-pill border border-line bg-white"
          >
            <span className="text-[11px] font-semibold text-ink">
              {me.nickname}
            </span>
            <span className="w-5 h-5 rounded-full overflow-hidden bg-paper-2 border border-line">
              {me.profileImageUrl && (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={me.profileImageUrl}
                  alt=""
                  className="w-full h-full object-cover"
                />
              )}
            </span>
          </Link>
        </div>
      ) : (
        <a
          href="/api/v1/auth/kakao"
          className="px-3 py-1.5 rounded-pill text-[12px] font-bold"
          style={{ background: "var(--kakao)", color: "var(--kakao-ink)" }}
        >
          카카오로 시작
        </a>
      )}
    </header>
  );
}
