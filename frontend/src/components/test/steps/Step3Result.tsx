/**
 * Step3Result
 *
 * 검사 시트 Step 3 — 결과 화면.
 * screens.yaml: test-sheet-step3-guest / test-sheet-step3-member
 *
 * 상태 분기:
 *   submitting — 로딩 스피너 (POST /results/score 응답 대기)
 *   error      — 에러 메시지 + 재시도 버튼
 *   done       — 결과 표시
 *
 * 인증 분기 (done 상태):
 *   비회원 — "사용자"로 이름 표시, 카카오 저장 CTA (test-sheet-step3-guest)
 *   회원   — authStore 구현 후 3주차에 추가 (test-sheet-step3-member)
 *            현재는 항상 비회원 분기로 렌더링
 *
 * TODO (3주차):
 *   - useAuthStore() 훅 연동
 *   - isAuthenticated=true 시 회원 분기: POST /results 자동 저장 + "결과 상세로 가기" CTA
 */

import ReactMarkdown from "react-markdown";
import type { ScoreResult } from "@/stores/testSheetStore";
import { TypePill, BalancedPill } from "@/components/disc/TypePill";
import { DiscBarsLarge } from "@/components/disc/DiscBarsLarge";

interface Step3ResultProps {
  submitStatus: "idle" | "submitting" | "done" | "error";
  result: ScoreResult | null;
  errorMessage: string | null;
  onRetry: () => void; // 에러 시 Step 2로 복귀
  onClose: () => void; // 닫기
}

export function Step3Result({
  submitStatus,
  result,
  errorMessage,
  onRetry,
  onClose,
}: Step3ResultProps) {
  if (submitStatus === "submitting") {
    return <SubmittingState />;
  }

  if (submitStatus === "error") {
    return <ErrorState message={errorMessage} onRetry={onRetry} />;
  }

  if (submitStatus === "done" && result) {
    return <DoneState result={result} onClose={onClose} />;
  }

  return null;
}

// ─── submitting ───────────────────────────────────────────

function SubmittingState() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4">
      <div className="w-10 h-10 rounded-full border-4 border-line border-t-accent animate-spin" />
      <p className="text-sm text-ink-soft">결과를 분석하고 있어요…</p>
    </div>
  );
}

// ─── error ────────────────────────────────────────────────

function ErrorState({
  message,
  onRetry,
}: {
  message: string | null;
  onRetry: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4 text-center px-4">
      <span className="text-4xl">😥</span>
      <p className="text-base font-semibold text-ink">
        결과를 불러오지 못했어요
      </p>
      <p className="text-sm text-ink-soft">{message}</p>
      <button
        onClick={onRetry}
        className="px-6 py-3 rounded-pill bg-ink text-white font-semibold text-sm"
      >
        다시 시도하기
      </button>
    </div>
  );
}

// ─── done ─────────────────────────────────────────────────

type DiscProfile =
  | { kind: "balanced" }
  | { kind: "typed"; types: ("D" | "I" | "S" | "C")[] };

function getDiscProfile(buckets: ScoreResult["buckets"]): DiscProfile {
  const entries = [
    { type: "D" as const, value: buckets.d },
    { type: "I" as const, value: buckets.i },
    { type: "S" as const, value: buckets.s },
    { type: "C" as const, value: buckets.c },
  ];

  // 전부 2이면 균형형
  if (entries.every((e) => e.value === 2)) return { kind: "balanced" };

  // 최댓값과 같은 모든 유형 반환
  const max = Math.max(...entries.map((e) => e.value));
  const types = entries.filter((e) => e.value === max).map((e) => e.type);

  return { kind: "typed", types };
}

function DoneState({
  result,
  onClose,
}: {
  result: ScoreResult;
  onClose: () => void;
}) {
  const profile = getDiscProfile(result.buckets);

  return (
    <div className="flex-1 flex flex-col overflow-y-auto">
      {/* ── hero 블록 ── */}
      <div className="flex flex-col gap-3 px-5 pt-5 pb-4 bg-paper">
        {/* 유형 칩 — 최댓값 공동인 경우 복수 표시 */}
        <div className="flex gap-2 flex-wrap">
          {profile.kind === "typed" ? (
            profile.types.map((type) => <TypePill key={type} type={type} />)
          ) : (
            <BalancedPill />
          )}
        </div>

        {/* 제목: 보고서 내용과 충돌 방지 — 유형 명시 제거 */}
        <h1 className="text-2xl font-black text-ink leading-tight">
          사용자님의 DISC 보고서
        </h1>

        <p className="text-sm text-ink-soft">
          24문항 응시 결과를 4축으로 정리했어요.
        </p>

        <DiscBarsLarge buckets={result.buckets} size="lg" />
      </div>

      {/* ── 보고서 본문 ── */}
      <div className="flex-1 px-5 py-5 bg-paper-2">
        <ReactMarkdown
          components={{
            h2: ({ children }) => (
              <h2
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 8,
                  fontFamily: "var(--font-sans)",
                  fontSize: 15,
                  fontWeight: 800,
                  margin: "22px 0 8px",
                  color: "var(--ink)",
                }}
              >
                <span
                  style={{
                    display: "inline-block",
                    width: 8,
                    height: 8,
                    background: "var(--accent)",
                    borderRadius: 2,
                    flexShrink: 0,
                  }}
                />
                {children}
              </h2>
            ),
            p: ({ children }) => (
              <p
                style={{
                  margin: "0 0 10px",
                  fontSize: 13,
                  lineHeight: 1.7,
                  color: "var(--ink-soft)",
                }}
              >
                {children}
              </p>
            ),
            ul: ({ children }) => (
              <ul
                style={{
                  margin: "0 0 12px",
                  paddingLeft: 18,
                  color: "var(--ink-soft)",
                  fontSize: 13,
                  lineHeight: 1.7,
                }}
              >
                {children}
              </ul>
            ),
            li: ({ children }) => (
              <li style={{ margin: "4px 0" }}>{children}</li>
            ),
            blockquote: ({ children }) => (
              <blockquote
                style={{
                  margin: "8px 0 14px",
                  borderLeft: "3px solid var(--accent)",
                  padding: "4px 12px",
                  background: "oklch(0.97 0.03 40)",
                  color: "var(--ink)",
                  borderRadius: "0 6px 6px 0",
                  fontSize: 12.5,
                }}
              >
                {children}
              </blockquote>
            ),
            strong: ({ children }) => (
              <strong style={{ color: "var(--ink)", fontWeight: 700 }}>
                {children}
              </strong>
            ),
            hr: () => (
              <hr
                style={{
                  border: "none",
                  borderTop: "1px dashed var(--line)",
                  margin: "18px 0",
                }}
              />
            ),
          }}
        >
          {result.report}
        </ReactMarkdown>
      </div>

      {/* ── CTA 영역 ── */}
      <div className="flex flex-col gap-3 px-5 py-5 bg-paper border-t border-line-soft">
        <a
          href="/api/v1/auth/kakao"
          className="w-full py-3 rounded-pill flex items-center justify-center gap-2 font-semibold text-sm"
          style={{ background: "var(--kakao)", color: "var(--kakao-ink)" }}
        >
          <span>💬</span>
          <span>카카오로 결과 저장하기</span>
        </a>
        <button onClick={onClose} className="w-full py-3 text-sm text-ink-soft">
          저장하지 않고 닫기
        </button>
      </div>
    </div>
  );
}
