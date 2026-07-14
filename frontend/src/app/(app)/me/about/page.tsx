"use client";

import { useRouter } from "next/navigation";
import { useToast } from "@/hooks/useToast";

// wf-p4-me.jsx ⑥ · screens.yaml me-about (auth: public, api: [])
const FEATURES = [
  { i: "①", t: "나는 누구일까?", s: "24문항 DISC 검사 · 누구나 무료" },
  { i: "②", t: "남이 보는 내 모습", s: "친구에게 일회용 링크로 평정 요청" },
  { i: "③", t: "우리 잘 맞을까?", s: "동료와의 케미 보고서 발행 (코인 1)" },
  { i: "④", t: "통계 비교 · 변화 추이", s: "연령대·성별 평균과 시간순 변화 비교" },
];

const LEGAL = [
  "서비스 이용약관",
  "개인정보 처리방침",
  "면책 조항 — 참고용 성향 분석, 심리 진단 아님",
  "오픈소스 라이선스",
];

export default function MeAboutPage() {
  const router = useRouter();
  const show = useToast();

  return (
    <div className="flex flex-col min-h-full bg-paper">
      {/* 뒤로가기 바 */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-line">
        <button onClick={() => router.back()} className="text-ink p-1">
          ‹
        </button>
        <p className="font-bold text-sm text-ink">서비스 소개 &amp; 약관</p>
      </div>

      {/* Hero */}
      <div className="px-5 py-6 bg-white border-b border-line">
        <div className="text-2xl font-extrabold text-ink">MyCPT</div>
        <p className="text-xs text-ink-soft mt-1 leading-relaxed">
          나의 성향을 알아보고, 친구와 동료의 시선과 비교해 보세요. DISC 24문항을
          기반으로 한 친숙한 성향 검사 서비스예요.
        </p>
      </div>

      {/* 기능 소개 */}
      <p className="text-xs font-semibold text-ink-faint px-4 pt-4 pb-1">
        이런 걸 할 수 있어요
      </p>
      <div className="bg-white border-y border-line">
        {FEATURES.map((f, idx) => (
          <div
            key={f.t}
            className={[
              "flex items-center gap-3 px-4 py-3.5",
              idx !== 0 ? "border-t border-line" : "",
            ].join(" ")}
          >
            <div className="w-8 h-8 rounded-full bg-accent-bg text-accent flex items-center justify-center font-bold shrink-0">
              {f.i}
            </div>
            <div>
              <p className="text-sm text-ink">{f.t}</p>
              <p className="text-xs text-ink-faint">{f.s}</p>
            </div>
          </div>
        ))}
      </div>

      {/* 약관 / 정책 */}
      <p className="text-xs font-semibold text-ink-faint px-4 pt-4 pb-1">
        약관 / 정책
      </p>
      <div className="bg-white border-y border-line">
        {LEGAL.map((t, idx) => (
          <button
            key={t}
            onClick={() => show("문서 준비 중이에요")}
            className={[
              "w-full flex items-center justify-between px-4 py-3.5 text-left",
              idx !== 0 ? "border-t border-line" : "",
            ].join(" ")}
          >
            <span className="text-sm text-ink">{t}</span>
            <span className="text-ink-faint">›</span>
          </button>
        ))}
      </div>

      {/* 푸터 */}
      <div className="px-4 py-6 text-center text-xs text-ink-faint font-mono leading-relaxed">
        MyCPT · 2026
        <br />© 2026 MyCPT. All rights reserved.
      </div>
    </div>
  );
}
