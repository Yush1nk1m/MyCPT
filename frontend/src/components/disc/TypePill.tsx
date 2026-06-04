/**
 * TypePill
 *
 * DISC 유형 칩. 4축 색상 자동 적용.
 *
 * components.yaml 명세:
 *   props: { type: 'D' | 'I' | 'S' | 'C', mini?: boolean }
 *   token refs: --disc-d, --disc-i, --disc-s, --disc-c
 *
 * 재사용처:
 *   - Step3Result (결과 화면 hero)
 *   - ResultCard (결과 이력 카드, 3주차)
 *   - PeerCard (동료 목록, 3주차)
 */

type DiscType = "D" | "I" | "S" | "C";

interface TypePillProps {
  type: DiscType;
  mini?: boolean;
}

// 와이어프레임 CSS 기준 색상 매핑
// globals.css의 --disc-* 토큰 사용
const TYPE_CONFIG: Record<
  DiscType,
  { label: string; bg: string; color: string; dotColor: string }
> = {
  D: {
    label: "주도형",
    bg: "oklch(0.96 0.05 30)",
    color: "var(--disc-d)",
    dotColor: "var(--disc-d)",
  },
  I: {
    label: "사교형",
    bg: "oklch(0.97 0.05 80)",
    color: "oklch(0.45 0.14 80)",
    dotColor: "var(--disc-i)",
  },
  S: {
    label: "안정형",
    bg: "oklch(0.96 0.04 150)",
    color: "var(--disc-s)",
    dotColor: "var(--disc-s)",
  },
  C: {
    label: "신중형",
    bg: "oklch(0.96 0.04 240)",
    color: "var(--disc-c)",
    dotColor: "var(--disc-c)",
  },
};

export function TypePill({ type, mini = false }: TypePillProps) {
  const config = TYPE_CONFIG[type];

  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: mini ? 4 : 5,
        padding: mini ? "2px 7px" : "3px 9px",
        borderRadius: 999,
        background: config.bg,
        color: config.color,
        fontSize: mini ? 10 : 11,
        fontWeight: 700,
        fontFamily: "var(--font-sans)",
        lineHeight: 1,
      }}
    >
      {/* 색상 dot */}
      <span
        style={{
          width: mini ? 6 : 8,
          height: mini ? 6 : 8,
          borderRadius: "50%",
          background: config.dotColor,
          flexShrink: 0,
        }}
      />
      {/* "D · 주도형" */}
      <span>
        {type} · {config.label}
      </span>
    </span>
  );
}

export function BalancedPill() {
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 5,
        padding: "3px 9px",
        borderRadius: 999,
        background: "oklch(0.96 0.02 300)",
        color: "oklch(0.45 0.08 300)",
        fontSize: 11,
        fontWeight: 700,
        fontFamily: "var(--font-sans)",
        lineHeight: 1,
      }}
    >
      <span
        style={{
          width: 8,
          height: 8,
          borderRadius: "50%",
          background: "oklch(0.65 0.08 300)",
          flexShrink: 0,
        }}
      />
      <span>균형형</span>
    </span>
  );
}
