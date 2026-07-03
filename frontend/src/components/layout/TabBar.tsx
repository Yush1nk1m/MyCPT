"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuthStore } from "@/stores/autoStore";
import { useToast } from "@/hooks/useToast";

const TABS = [
  { key: "home", label: "홈", icon: "⌂", href: "/", memberOnly: false },
  {
    key: "results",
    label: "검사 결과",
    icon: "☰",
    href: "/results",
    memberOnly: true,
  },
  {
    key: "peers",
    label: "동료 & 케미",
    icon: "⇄",
    href: "/colleagues",
    memberOnly: true,
  },
  {
    key: "account",
    label: "내 정보",
    icon: "◉",
    href: "/me",
    memberOnly: true,
  },
] as const;

function resolveActiveTab(pathname: string): (typeof TABS)[number]["key"] {
  if (pathname === "/") return "home";
  if (pathname.startsWith("/results")) return "results";
  if (pathname.startsWith("/colleagues") || pathname.startsWith("/chemistry"))
    return "peers";
  if (pathname.startsWith("/me")) return "account";
  return "home";
}

export function TabBar() {
  const pathname = usePathname();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const showToast = useToast();
  const active = resolveActiveTab(pathname);

  function handleClick(e: React.MouseEvent, tab: (typeof TABS)[number]) {
    if (tab.memberOnly && !isAuthenticated) {
      // access-matrix.md: 비회원 잠김 탭 → 토스트만, 페이지 이동 없음
      e.preventDefault();
      showToast("🔒 로그인이 필요해요", {
        action: {
          label: "카카오로 시작",
          onClick: () => {
            window.location.href = `/api/v1/auth/kakao?returnTo=${encodeURIComponent(tab.href)}`;
          },
        },
      });
    }
  }

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-30 grid grid-cols-4 bg-white border-t border-line"
      style={{ height: "var(--tab-height)" }}
    >
      {TABS.map((tab) => {
        const isActive = active === tab.key;
        const locked = tab.memberOnly && !isAuthenticated;
        return (
          <Link
            key={tab.key}
            href={tab.href}
            onClick={(e) => handleClick(e, tab)}
            className="flex flex-col items-center justify-center gap-0.5 text-[10.5px] font-bold"
            style={{
              color: isActive ? "var(--accent)" : "var(--ink-soft)",
              opacity: locked ? 0.55 : 1,
            }}
          >
            <span className="text-base leading-none">{tab.icon}</span>
            <span>
              {tab.label}
              {locked && " 🔒"}
            </span>
          </Link>
        );
      })}
    </nav>
  );
}
