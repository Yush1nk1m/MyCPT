/**
 * OptionButton
 *
 * 선택지 버튼 원자 컴포넌트.
 * QuestionCard의 Most/Least 섹션에서 사용.
 *
 * 세 가지 시각 상태:
 *   default  — 선택 가능한 기본 상태
 *   selected — 선택된 상태 (Most: member 색, Least: accent 색)
 *   disabled — 비활성 워터마크 (Most에서 선택한 항목이 Least에서 비활성)
 *
 * aria-pressed: 스크린리더에서 선택 상태 인식
 */

type OptionButtonState = "default" | "selected" | "disabled";
type OptionButtonVariant = "most" | "least";

interface OptionButtonProps {
  text: string;
  state: OptionButtonState;
  variant?: OptionButtonVariant; // selected 색상 분기용
  onClick: () => void;
}

export function OptionButton({
  text,
  state,
  variant = "most",
  onClick,
}: OptionButtonProps) {
  const isSelected = state === "selected";
  const isDisabled = state === "disabled";

  // variant별 selected 스타일
  const selectedStyle =
    variant === "most"
      ? "border-member bg-member-bg font-semibold text-ink"
      : "border-accent bg-accent-bg font-semibold text-ink";

  return (
    <button
      onClick={() => !isDisabled && onClick()}
      disabled={isDisabled}
      aria-pressed={isSelected}
      className={[
        "w-full text-left rounded-pill px-4 py-[10px] text-sm border transition-all duration-150",
        isSelected
          ? selectedStyle
          : isDisabled
            ? // 워터마크: 흐리게 + 커서 금지
              "border-line bg-paper-3 text-ink-faint opacity-40 cursor-not-allowed"
            : "border-line bg-white text-ink hover:border-ink-soft active:bg-paper-2",
      ].join(" ")}
    >
      {text}
    </button>
  );
}
