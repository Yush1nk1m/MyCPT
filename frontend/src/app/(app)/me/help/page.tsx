"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useToast } from "@/hooks/useToast";

// wf-p4-me.jsx ⑦ · screens.yaml me-help (auth: public, api: [])
const SUPPORT_EMAIL = "help@mycpt.kr";

const FAQS = [
  {
    q: "코인은 어떻게 충전되나요?",
    a: "매일 자정에 1개씩 자동 충전되며, 최대 3개까지 보관할 수 있어요. 별도의 결제는 없습니다.",
  },
  {
    q: "검사 결과를 친구에게 공유해도 안전한가요?",
    a: '"남이 보는 내 모습"은 일회용 링크로 발급되며, 응시가 완료되면 자동 만료됩니다.',
  },
  {
    q: "동료를 삭제하면 케미 보고서도 사라지나요?",
    a: "동료 관계는 끊어지지만, 이미 발행된 케미 보고서는 본인의 이력에 남아 있어요.",
  },
  {
    q: "왜 출생연도와 성별을 입력해야 하나요?",
    a: "연령대·성별 평균과 비교하는 통계 기능에만 사용되며, 다른 사용자에게는 보이지 않아요.",
  },
  {
    q: "검사 결과가 매번 조금씩 다른데 정상인가요?",
    a: "성향은 시기와 상황에 따라 자연스럽게 변동돼요. 변화 추이 화면에서 흐름을 확인해 보세요.",
  },
];

export default function MeHelpPage() {
  const router = useRouter();
  const show = useToast();
  const [openIdx, setOpenIdx] = useState<number | null>(0);

  return (
    <div className="flex flex-col min-h-full bg-paper">
      {/* 뒤로가기 바 */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-line">
        <button onClick={() => router.back()} className="text-ink p-1">
          ‹
        </button>
        <p className="font-bold text-sm text-ink">고객 문의 / FAQ</p>
      </div>

      {/* 연락 카드 */}
      <div className="px-4 pt-4">
        <div className="bg-white border border-ink rounded-2xl p-4">
          <p className="text-sm font-bold text-ink mb-1">도움이 필요하신가요?</p>
          <p className="text-xs text-ink-soft leading-relaxed mb-3">
            평일 10:00 ~ 18:00 응대 · 1~2 영업일 내 회신
          </p>
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => show("준비 중이에요")}
              className="py-2.5 text-xs font-semibold border border-line rounded-lg text-ink"
            >
              💬 카카오 채널
            </button>
            <a
              href={`mailto:${SUPPORT_EMAIL}`}
              className="py-2.5 text-xs font-semibold border border-line rounded-lg text-ink text-center"
            >
              ✉ 이메일 문의
            </a>
          </div>
          <p className="mt-2.5 text-xs text-ink-faint font-mono text-center">
            {SUPPORT_EMAIL}
          </p>
        </div>
      </div>

      {/* FAQ 아코디언 */}
      <p className="text-xs font-semibold text-ink-faint px-4 pt-5 pb-1">
        자주 묻는 질문
      </p>
      <div className="px-4 flex flex-col gap-2 pb-6">
        {FAQS.map((f, idx) => {
          const open = openIdx === idx;
          return (
            <div
              key={f.q}
              className="bg-white border border-line rounded-xl overflow-hidden"
            >
              <button
                onClick={() => setOpenIdx(open ? null : idx)}
                className="w-full flex items-center justify-between gap-2 px-4 py-3 text-left"
                aria-expanded={open}
              >
                <span className="text-sm text-ink">Q. {f.q}</span>
                <span className="text-ink-faint shrink-0">{open ? "▴" : "▾"}</span>
              </button>
              {open && (
                <p className="px-4 pb-3.5 text-xs text-ink-soft leading-relaxed border-t border-line-soft pt-3">
                  {f.a}
                </p>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
