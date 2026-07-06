"use client";

import { useState } from "react";

const SLIDES = [
  {
    emoji: "🧭",
    title: "역량, 어떻게 나타날까요?",
    body: "역량은 지식, 기술, 태도 세 가지가 합쳐져 만들어져요.\nMyCPT(나의 역량)는 이 중에서도 스스로 알아차리기 어려운 태도적 역량을 발견하도록 돕는 걸 목표로 해요.",
  },
  {
    emoji: "🪞",
    title: "첫 걸음, 나 알아가기",
    body: "직무 적성 검사로 자주 쓰이는 DISC 척도로 24개 질문에 답해주세요.\n내 성향의 강점과 주의할 점을 정리해서 보여드려요.",
  },
  {
    emoji: "🤝",
    title: "두 번째 걸음, 동료와의 케미",
    body: "나와 동료의 DISC 성향을 분석한 후,\n함께 일할 때 어떤 방식으로 협업하면 좋을지 방향을 제안해 드려요.",
  },
  {
    emoji: "🌱",
    title: "함께 성장하는 플랫폼으로",
    body: "MyCPT는 앞으로 나의 역량을 더 다양한 각도에서 발견하도록 돕는 플랫폼으로 계속 발전할 예정이에요.",
  },
];

export function AboutSheet({ onClose }: { onClose: () => void }) {
  const [index, setIndex] = useState(0);
  const isLast = index === SLIDES.length - 1;
  const slide = SLIDES[index];

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/40" onClick={onClose} />
      <div className="fixed inset-x-0 bottom-0 z-50 bg-white rounded-t-3xl px-6 pt-5 pb-8 flex flex-col gap-6 animate-sheet-up">
        <div className="flex justify-end">
          <button
            onClick={onClose}
            className="text-ink-faint text-lg leading-none"
            aria-label="닫기"
          >
            ✕
          </button>
        </div>

        <div className="flex flex-col items-center text-center gap-3 min-h-[220px] justify-center">
          <span className="text-4xl">{slide.emoji}</span>
          <h2 className="text-lg font-bold text-ink">{slide.title}</h2>
          <p className="text-[13px] text-ink-soft leading-relaxed whitespace-pre-line">
            {slide.body}
          </p>
        </div>

        <div className="flex items-center justify-center gap-1.5">
          {SLIDES.map((_, i) => (
            <span
              key={i}
              className="rounded-full transition-all"
              style={{
                width: i === index ? 18 : 6,
                height: 6,
                background: i === index ? "var(--accent)" : "var(--line)",
              }}
            />
          ))}
        </div>

        <div className="flex gap-2">
          {index > 0 && (
            <button
              onClick={() => setIndex((i) => i - 1)}
              className="flex-1 py-3 rounded-2xl border border-line text-ink-soft text-sm font-semibold"
            >
              이전
            </button>
          )}
          <button
            onClick={() => (isLast ? onClose() : setIndex((i) => i + 1))}
            className="flex-1 py-3 rounded-2xl bg-ink text-white text-sm font-bold"
          >
            {isLast ? "마침" : "다음"}
          </button>
        </div>

        {isLast && (
          <p className="text-center text-[10.5px] text-ink-faint">
            본 결과는 참고용 성향 분석이며 심리 진단이 아닙니다.
          </p>
        )}
      </div>
    </>
  );
}
