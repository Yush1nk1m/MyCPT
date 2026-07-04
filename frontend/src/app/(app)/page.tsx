"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { TestSheet } from "@/components/test/TestSheet";
import { useTestSheetStore } from "@/stores/testSheetStore";
import { useMe } from "@/hooks/useMe";
import { useToast } from "@/hooks/useToast";
import { AboutSheet } from "@/components/home/AboutSheet";

const PILLS = [
  { key: "self", title: "나는 누구일까?", sub: "심리검사", memberOnly: false },
  {
    key: "share",
    title: "남이 보는 내 모습",
    sub: "친구에게 공유하기",
    memberOnly: true,
  },
  {
    key: "peers",
    title: "우리 잘 맞을까?",
    sub: "동료와의 케미 검사",
    memberOnly: true,
  },
  {
    key: "about",
    title: "이 서비스는 무엇인가요?",
    sub: "서비스 소개 & 약관",
    memberOnly: false,
  },
] as const;

export default function Home() {
  const router = useRouter();
  const { data: me } = useMe();
  const isAuthenticated = !!me;
  const openTestSheet = useTestSheetStore((s) => s.open);
  const showToast = useToast();
  const [aboutOpen, setAboutOpen] = useState(false);

  function handlePillClick(key: (typeof PILLS)[number]["key"]) {
    if (key === "self") return openTestSheet("self");
    if (key === "about") return setAboutOpen(true);

    if (!isAuthenticated) {
      showToast("🔒 로그인이 필요해요", {
        action: {
          label: "카카오로 시작",
          onClick: () => {
            window.location.href = "/api/v1/auth/kakao?returnTo=/";
          },
        },
      });
      return;
    }
    if (key === "share") openTestSheet("share");
    if (key === "peers") router.push("/colleagues");
  }

  return (
    <div className="min-h-full flex flex-col px-5 pt-8 pb-6 relative overflow-hidden">
      <div
        className="home-blob"
        style={{ top: -60, right: -60, width: 220, height: 220 }}
        aria-hidden
      />

      <div className="flex flex-col gap-1 mb-7 z-10">
        <p className="text-2xl font-black text-ink">MyCPT</p>
        <p className="text-[13px] text-ink-soft">나의 역량을 발견하는 첫걸음</p>
      </div>

      <div className="flex flex-col gap-3.5 z-10">
        {PILLS.map((pill, i) => {
          const locked = pill.memberOnly && !isAuthenticated;
          const isHero = pill.key === "self";
          return (
            <button
              key={pill.key}
              onClick={() => handlePillClick(pill.key)}
              className="pill-enter"
              style={{
                padding: "16px 18px",
                borderRadius: 999,
                textAlign: "center",
                animationDelay: `${i * 80}ms`,
                ["--target-opacity" as string]: locked ? 0.55 : 1,
                background: isHero
                  ? "var(--accent-bg)"
                  : pill.key === "about"
                    ? "var(--paper-2)"
                    : "white",
                border: isHero
                  ? "1.5px solid var(--accent)"
                  : "1.5px solid var(--line)",
              }}
            >
              <div className="flex flex-col items-center gap-0.5">
                <span className="text-[15px] font-bold text-ink">
                  {pill.title} {locked && "🔒"}
                </span>
                <span className="text-[11.5px] text-ink-soft">{pill.sub}</span>
              </div>
            </button>
          );
        })}
      </div>

      <TestSheet />
      {aboutOpen && <AboutSheet onClose={() => setAboutOpen(false)} />}
    </div>
  );
}
