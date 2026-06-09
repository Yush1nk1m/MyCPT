/**
 * DISC 버킷값으로 유형 프로필 산출
 *
 * - 모든 축이 2이면 균형형
 * - 그 외 최댓값을 가진 축이 주 유형 (공동 1위 가능)
 *
 * 재사용처:
 *   - DoneState (검사 완료 결과)
 *   - results/page.tsx (이력 카드)
 *   - results/[id]/page.tsx (결과 상세)
 */

export type DiscType = "D" | "I" | "S" | "C";

export type DiscProfile =
  | { kind: "balanced" }
  | { kind: "typed"; types: DiscType[] };

export function getDiscProfile(buckets: {
  d: number;
  i: number;
  s: number;
  c: number;
}): DiscProfile {
  const entries = [
    { type: "D" as DiscType, value: buckets.d },
    { type: "I" as DiscType, value: buckets.i },
    { type: "S" as DiscType, value: buckets.s },
    { type: "C" as DiscType, value: buckets.c },
  ];
  if (entries.every((e) => e.value === 2)) return { kind: "balanced" };
  const max = Math.max(...entries.map((e) => e.value));
  const types = entries.filter((e) => e.value === max).map((e) => e.type);
  return { kind: "typed", types };
}
