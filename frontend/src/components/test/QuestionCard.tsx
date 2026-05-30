/**
 * QuestionCard
 *
 * 단일 문항 카드 (와이어프레임 P1 #test-b 확정안).
 *
 * 레이아웃:
 *   ┌──────────────────────┐
 *   │ 문항 번호 + 도입부   │
 *   ├──────────────────────┤
 *   │ ● 가장 가까운        │  Most 섹션
 *   │  [A] [B] [C] [D]     │
 *   ├──────────────────────┤
 *   │ ● 가장 먼            │  Least 섹션
 *   │  [A] [B] [C] [D]     │
 *   └──────────────────────┘
 *
 * 인터랙션 규칙 (screens.yaml):
 *   - Most 에서 선택한 옵션은 Least 에서 비활성(워터마크)으로 표시
 *   - Most 와 Least 에서 동일 옵션 선택 불가
 *   - 두 선택이 모두 완료되면 onAnswer 호출
 */

import { useMemo, useState, useEffect } from "react";
import { type DiscOption, type DiscTag } from "@/lib/disc/questions";
import { shuffleOptions } from "@/lib/disc/scoring";

interface QuestionCardProps {
  questionIndex: number; // 0-based (표시는 +1)
  stem: string; // 문항 도입부
  options: DiscOption[]; // 원본 순서 (셔플은 내부적으로 처리)
  initialMost?: DiscTag; // 이미 답한 문항으로 돌아왔을 때 복원
  initialLeast?: DiscTag;
  onAnswer: (most: DiscTag, least: DiscTag) => void;
}

export function QuestionCard({
  questionIndex,
  stem,
  options,
  initialMost,
  initialLeast,
  onAnswer,
}: QuestionCardProps) {
  // 문항이 바뀔 때만 셔플 (questionIndex를 dep으로)
  const shuffled = useMemo(() => shuffleOptions(options), [questionIndex]); // eslint-disable-line react-hooks/exhaustive-deps

  // key로 리마운트되므로 초기값이 props에서 한 번만 읽힘 - useEffect 불필요
  const [most, setMost] = useState<DiscTag | null>(initialMost ?? null);
  const [least, setLeast] = useState<DiscTag | null>(initialLeast ?? null);

  // Most 선택 -> 만약 Least와 같은 값이면 Least 초기화
  const handleMost = (tag: DiscTag) => {
    const nextMost = tag === most ? null : tag; // 재클릭 시 선택 해제
    setMost(nextMost);
    const nextLeast = nextMost === least ? null : least;
    setLeast(nextLeast);
    if (nextMost && nextLeast) onAnswer(nextMost, nextLeast);
  };

  // Least 선택 → Most 와 같은 값이면 선택 불가
  const handleLeast = (tag: DiscTag) => {
    if (tag === most) return; // Most 선택 옵션은 Least 선택 불가
    const nextLeast = tag === least ? null : tag;
    setLeast(nextLeast);
    if (most && nextLeast) onAnswer(most, nextLeast);
  };

  return (
    <div className="flex flex-col gap-3 flex-1">
      {/* 문항 번호 + 도입부 */}
      <div className="flex flex-col gap-1">
        <span className="font-mono text-xs text-ink-faint">
          Q.{questionIndex + 1}
        </span>
        <p className="text-md font-semibold leading-snug text-ink">{stem}</p>
      </div>

      {/* Most 섹션 */}
      <OptionSection
        label="가장 가까운"
        color="member"
        options={shuffled}
        selected={most}
        disabledTag={null} // Most는 모두 선택 가능
        onSelect={handleMost}
      />

      {/* Least 섹션 */}
      <OptionSection
        label="가장 먼"
        color="accent"
        options={shuffled}
        selected={least}
        disabledTag={most} // Most 선택 옵션은 비활성
        onSelect={handleLeast}
      />
    </div>
  );
}

// ─── 내부 컴포넌트 ───────────────────────────────────────

interface OptionSectionProps {
  label: string;
  color: "member" | "accent";
  options: DiscOption[];
  selected: DiscTag | null;
  disabledTag: DiscTag | null; // 이 태그는 선택 불가 (워터마크 처리)
  onSelect: (tag: DiscTag) => void;
}

function OptionSection({
  label,
  color,
  options,
  selected,
  disabledTag,
  onSelect,
}: OptionSectionProps) {
  const dotColor = color === "member" ? "bg-member" : "bg-accent";
  const labelColor = color === "member" ? "text-member" : "text-accent";
  const selectedBorder =
    color === "member"
      ? "border-member bg-member-bg"
      : "border-accent bg-accent-bg";

  return (
    <div
      className={[
        "rounded-lg border p-3 flex flex-col gap-2",
        color === "member"
          ? "border-member/30 bg-member-bg/40"
          : "border-accent/30 bg-accent-bg/40",
      ].join(" ")}
    >
      {/* 섹션 레이블 */}
      <div
        className={`flex items-center gap-1 text-xs font-bold ${labelColor}`}
      >
        <span className={`w-2 h-2 rounded-full ${dotColor}`} />
        {label}
      </div>

      {/* 선택지 목록 */}
      <div className="flex flex-col gap-[6px]">
        {options.map((opt) => {
          const isSelected = selected === opt.tag;
          const isDisabled = disabledTag === opt.tag;

          return (
            <button
              key={opt.id}
              onClick={() => !isDisabled && onSelect(opt.tag)}
              disabled={isDisabled}
              className={[
                "w-full text-left rounded-pill px-4 py-2 text-sm border tansition-all duration-150",
                isSelected
                  ? `${selectedBorder} border font-semibold text-ink`
                  : isDisabled
                    ? "border-line bg-paper-3 text-ink-faint opacity-40 cursor-not-allowed" // 워터마크
                    : "border-line bg-white text-ink hover:border-ink-soft active:bg-paper-2",
              ].join(" ")}
            >
              {opt.text}
            </button>
          );
        })}
      </div>
    </div>
  );
}
