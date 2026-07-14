// me/insights 페이지의 순수 계산 로직 추출

/** 버킷값(1~3)을 막대 높이 퍼센트로 변환 (1→33%, 2→66%, 3→100%) */
export function bucketToPercent(bucket: number): number {
  return Math.round((bucket / 3) * 100);
}

type Axis4 = { d: number; i: number; s: number; c: number };

const AXES = ["D", "I", "S", "C"] as const;

/**
 * 내 버킷값과 또래 평균 버킷값을 비교해 편차 절대값이 가장 큰 축을 반환.
 * 최대 편차가 0이면(전 축이 평균과 동일) null 반환.
 */
export function dominantDeviation(
  my: Axis4,
  avg: Axis4,
):
  | { axis: "D" | "I" | "S" | "C"; diff: number; direction: "높아요" | "낮아요" }
  | null {
  const diffs = AXES.map((axis) => {
    const key = axis.toLowerCase() as "d" | "i" | "s" | "c";
    return { axis, diff: my[key] - avg[key] };
  });
  const max = diffs.reduce((a, b) =>
    Math.abs(a.diff) > Math.abs(b.diff) ? a : b,
  );

  if (Math.abs(max.diff) === 0) return null;

  return {
    axis: max.axis,
    diff: max.diff,
    direction: max.diff > 0 ? "높아요" : "낮아요",
  };
}
