/**
 * DotProgressBar
 *
 * 24개 dot으로 진행 상태 표시 (와이어프레임 P1 #test-b 확정안).
 *   - 완료 dot: accent 색상
 *   - 현재 dot: ink 색상 + 약간 큰 사이즈
 *   - 미응시 dot: paper-3 (회색)
 */

interface DotProgressBarProps {
  total: number; // 전체 문항 수 (24)
  currentIndex: number; // 현재 문항 인덱스 (0-based)
  answeredCount: number; // 완료한 문항 수
}

export function DotProgressBar({
  total,
  currentIndex,
  answeredCount,
}: DotProgressBarProps) {
  return (
    <div className="flex flex-wrap justify-center gap-[3px] py-1">
      {Array.from({ length: total }, (_, i) => {
        const isAnswered = i < answeredCount;
        const isCurrent = i === currentIndex;

        return (
          <div
            key={i}
            className={[
              "rounded-full border transition-all duration-150",
              isCurrent
                ? "w-[10px] h-[10px] bg-ink border-ink" // 현재: 강조
                : isAnswered
                  ? "w-[9px] h-[9px] bg-accent border-accent" // 완료: accent
                  : "w-[9px] h-[9px] bg-paper-3 border-line", // 미응시: 흐림
            ].join(" ")}
          />
        );
      })}
    </div>
  );
}
