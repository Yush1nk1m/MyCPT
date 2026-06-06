"use client";

import { useMe } from "@/hooks/useMe";
import { useRouter } from "next/navigation";
import Image from "next/image";

interface MenuItem {
  label: string;
  href: string | null;
  action?: string;
  style?: "muted" | "danger" | string;
}

interface MenuSection {
  title: string;
  items: MenuItem[];
}

// 메뉴 항목 정의 — screens.yaml me-hub menu_items
const MENU_SECTIONS: MenuSection[] = [
  {
    title: "내 정보",
    items: [
      { label: "내 정보 수정", href: "/me/profile" },
      { label: "알림 센터", href: "/me/notifications" },
      { label: "코인 / 사용 이력", href: "/me/coins" },
      { label: "통계 비교 · 변화 추이", href: "/me/insights" },
    ],
  },
  {
    title: "기타",
    items: [
      { label: "서비스 소개 & 약관", href: "/me/about" },
      { label: "고객 문의 / FAQ", href: "/me/help" },
    ],
  },
  {
    title: "계정",
    items: [
      { label: "로그아웃", href: null, action: "logout", style: "muted" },
      { label: "회원탈퇴", href: "/me/leave", style: "danger" },
    ],
  },
];

export default function MeHubPage() {
  const { data: me, isLoading } = useMe();
  const router = useRouter();

  async function handleLogout() {
    await fetch("/api/v1/auth/logout", {
      method: "POST",
      credentials: "include",
    });
    // authStore.clear()는 useMe의 error 핸들러가 처리
    router.push("/");
  }

  if (isLoading) return <MeHubSkeleton />;

  return (
    <div className="flex flex-col min-h-screen bg-paper">
      {/* 프로필 영역 */}
      <div className="flex items-center gap-4 px-4 py-6 bg-white border-b border-line">
        <button
          onClick={() => router.push("/me/profile")}
          className="relative shrink-0"
        >
          <div className="w-16 h-16 rounded-full overflow-hidden bg-paper-2 border border-line">
            {me?.profileImageUrl ? (
              <Image
                src={me.profileImageUrl}
                alt="프로필 이미지"
                width={64}
                height={64}
                className="object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-ink-faint text-xs">
                없음
              </div>
            )}
          </div>
          {/* 펜 아이콘 배지 */}
          <span className="absolute bottom-0 right-0 w-5 h-5 rounded-full bg-ink text-white flex items-center justify-center text-[10px]">
            ✎
          </span>
        </button>
        <div className="flex flex-col gap-0.5">
          <span className="font-bold text-base text-ink">{me?.nickname}</span>
          <span className="text-xs text-ink-soft">코인 {me?.coins ?? 0}개</span>
        </div>
      </div>

      {/* 메뉴 섹션 */}
      <div className="flex flex-col gap-4 px-4 py-4">
        {MENU_SECTIONS.map((section) => (
          <div key={section.title}>
            <p className="text-xs font-semibold text-ink-faint mb-1 px-1">
              {section.title}
            </p>
            <div className="bg-white rounded-xl border border-line overflow-hidden">
              {section.items.map((item, idx) => (
                <button
                  key={item.label}
                  onClick={() => {
                    if (item.action === "logout") {
                      handleLogout();
                      return;
                    }
                    if (item.href) router.push(item.href);
                  }}
                  className={[
                    "w-full flex items-center justify-between px-4 py-3.5 text-sm",
                    idx !== 0 ? "border-t border-line" : "",
                    item.style === "danger" ? "text-red-500" : "text-ink",
                    item.style === "muted" ? "text-ink-soft" : "",
                  ].join(" ")}
                >
                  <span>{item.label}</span>
                  {item.style !== "muted" && item.style !== "danger" && (
                    <span className="text-ink-faint">›</span>
                  )}
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function MeHubSkeleton() {
  return (
    <div className="flex flex-col min-h-screen bg-paper animate-pulse">
      <div className="flex items-center gap-4 px-4 py-6 bg-white border-b border-line">
        <div className="w-16 h-16 rounded-full bg-paper-2" />
        <div className="flex flex-col gap-2">
          <div className="w-24 h-4 bg-paper-2 rounded" />
          <div className="w-16 h-3 bg-paper-2 rounded" />
        </div>
      </div>
    </div>
  );
}
