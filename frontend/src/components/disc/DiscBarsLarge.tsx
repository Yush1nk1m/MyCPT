/**
 * DiscBarsLarge
 *
 * DISC 4축 막대 시각화 컴포넌트.
 *
 * 와이어프레임(wf-p2-detail.jsx, 결과 상세.html)의 .disc-bars-lg 구현체.
 *
 * 설계 결정:
 *   - buckets(1~3) 기준으로 막대 높이 계산. 백분율 = (bucket / 3) * 100
 *   - size="lg": Step3Result hero용 (height 140px, 와이어프레임 기준)
 *   - size="md": 결과 이력 카드용 (3주차 ResultCard에서 재사용 예정, height 50px)
 *
 * 재사용처:
 *   - Step3Result (size="lg")
 *   - ResultCard (size="md", 3주차)
 *   - result-detail 페이지 (size="lg", 3주차)
 */

type DiscType = "D" | "I" | "S" | "C";

interface Buckets {
  d: number; // 1~3
  i: number;
  s: number;
  c: number;
}

interface DiscBarsLargeProps {
  buckets: Buckets;
  size?: "md" | "lg";
}

const DISC_KEYS: {
  key: keyof Buckets;
  label: DiscType;
  name: string;
  color: string;
}[] = [
  { key: "d", label: "D", name: "주도", color: "var(--disc-d)" },
  { key: "i", label: "I", name: "사교", color: "var(--disc-i)" },
  { key: "s", label: "S", name: "안정", color: "var(--disc-s)" },
  { key: "c", label: "C", name: "신중", color: "var(--disc-c)" },
];

export function DiscBarsLarge({ buckets, size = "lg" }: DiscBarsLargeProps) {
  const isLg = size === "lg";
  // lg: 140px (와이어프레임 disc-bars-lg 기준), md: 50px (결과 이력 목록용)
  const containerHeight = isLg ? 140 : 50;

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gap: isLg ? 12 : 6,
        alignItems: "end",
        height: containerHeight,
      }}
    >
      {DISC_KEYS.map(({ key, label, name, color }) => {
        const bucket = buckets[key]; // 1~3
        // 막대 높이: 버킷 3 = 100%, 버킷 1 = 33%
        // 최솟값 11%를 보장해 버킷 1도 막대가 보임
        const heightPct = Math.round((bucket / 3) * 100);

        return (
          <div
            key={key}
            style={{
              display: "flex",
              flexDirection: "column",
              gap: isLg ? 6 : 3,
              height: "100%",
            }}
          >
            {/* 막대 래퍼 — 회색 배경 + 하단 경계선 */}
            <div
              style={{
                flex: 1,
                background: "var(--paper-2)",
                borderRadius: "4px 4px 0 0",
                overflow: "hidden",
                display: "flex",
                alignItems: "flex-end",
                borderBottom: "1.5px solid var(--ink)",
              }}
            >
              {/* 실제 색상 막대 */}
              <div
                style={{
                  width: "100%",
                  height: `${heightPct}%`,
                  background: color,
                  borderRadius: "4px 4px 0 0",
                  // 막대가 너무 낮으면 최소 높이 보장
                  minHeight: isLg ? 8 : 4,
                  transition: "height 0.4s cubic-bezier(0.2, 0.8, 0.2, 1)",
                }}
              />
            </div>

            {/* 하단 라벨 영역 */}
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 1,
              }}
            >
              {/* D / I / S / C 축 이름 */}
              <span
                style={{
                  fontFamily: "var(--font-mono)",
                  fontSize: isLg ? 14 : 9.5,
                  fontWeight: 700,
                  color: "var(--ink)",
                  lineHeight: 1,
                }}
              >
                {label}
              </span>
              {/* "주도" / "사교" 등 한국어 이름 */}
              {isLg && (
                <span
                  style={{
                    fontSize: 9.5,
                    color: "var(--ink-soft)",
                    lineHeight: 1,
                  }}
                >
                  {name}
                </span>
              )}
              {/* 버킷 수치 */}
              <span
                style={{
                  fontFamily: "var(--font-mono)",
                  fontSize: isLg ? 11 : 9,
                  color: "var(--ink)",
                  fontWeight: 600,
                  marginTop: 2,
                  lineHeight: 1,
                }}
              >
                {bucket}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
