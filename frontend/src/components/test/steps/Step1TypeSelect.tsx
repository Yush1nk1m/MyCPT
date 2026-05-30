/**
 * Step1TypeSelect
 *
 * 검사 시트 Step 1 — 유형 선택 화면 (screens.yaml: test-sheet-step1).
 * MVP는 DISC 카드 1개 고정 선택 상태.
 * Step 1에서는 자유롭게 닫기 가능 (닫기는 SheetFrame의 onClose가 처리).
 */

interface Step1TypeSelectProps {
  onStart: () => void; // "시작하기" 버튼 → Step 2로 이동
}

export function Step1TypeSelect({ onStart }: Step1TypeSelectProps) {
  return (
    <div className="flex flex-col gap-4 flex-1 pt-2">
      <p className="text-base text-ink-soft leading-relaxed">
        어떤 검사로 알아볼까요?
      </p>

      {/* DISC 유형 카드 — MVP 고정 선택 상태 */}
      <div className="border-[1.5px] border-ink rounded-lg bg-highlight p-4 flex flex-col gap-2">
        <div className="flex items-center justify-between">
          <span className="text-xl font-black text-ink">DISC</span>
          <div className="w-6 h-6 rounded-full bg-ink flex items-center justify-center text-white text-sm font-bold">
            ✓
          </div>
        </div>
        <p className="text-sm text-ink-soft leading-relaxed">
          행동 성향을 4가지 축(주도·사교·안정·신중)으로 나눠 분석해요.
        </p>
        <div className="flex gap-2 flex-wrap">
          <span className="font-mono text-xs text-ink-soft bg-white px-2 py-1 rounded-pill border border-line">
            24문항
          </span>
          <span className="font-mono text-xs text-ink-soft bg-white px-2 py-1 rounded-pill border border-line">
            약 3분
          </span>
          <span className="font-mono text-xs text-member bg-member-bg px-2 py-1 rounded-pill">
            ● 누구나 무료
          </span>
        </div>
      </div>

      {/* 추후 확장 안내 */}
      <div className="bg-paper-2 border border-dashed border-line rounded-lg p-3 text-sm text-ink-soft leading-relaxed">
        <span className="font-semibold text-ink">안내</span> · MVP 단계엔 DISC만
        제공합니다. 추후 다른 검사 유형도 추가될 예정이에요.
      </div>

      <div className="flex-1" />

      {/* 면책 고지 */}
      <p className="text-xs text-ink-faint text-center leading-relaxed">
        참고용 성향 분석이며, 심리·의학적 진단이 아닙니다.
      </p>

      <button
        onClick={onStart}
        className="w-full py-4 rounded-pill bg-ink text-white font-bold text-base active:opacity-80 transition-opacity"
      >
        시작하기 (24문항)
      </button>
    </div>
  );
}
