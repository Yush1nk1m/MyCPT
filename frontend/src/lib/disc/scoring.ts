/**
 * DISC 채점 유틸리티 (docs/service-design.md §3.4 기준)
 *
 * 채점 규칙:
 *   Most  → 해당 척도 +2점
 *   Least → 해당 척도 -1점
 *   미선택 → 0점
 *
 * 원점수 범위: 각 척도 -24 ~ +48
 * 불변 조건: D + I + S + C = 24 (24문항 × Most+2 + Least-1 = 24)
 */

import type { DiscTag } from "./questions";

export interface QuestionAnswer {
  most: DiscTag;
  least: DiscTag;
}

/** key: question index (0-based) */
export type Answers = Record<number, QuestionAnswer>;

export interface DiscScores {
  d: number;
  i: number;
  s: number;
  c: number;
}

/** 24문항 응답으로부터 DISC 원점수 산출 */
export function calculateScores(answers: Answers): DiscScores {
  const scores: DiscScores = { d: 0, i: 0, s: 0, c: 0 };

  for (const ans of Object.values(answers)) {
    scores[ans.most.toLowerCase() as keyof DiscScores] += 2;
    scores[ans.least.toLowerCase() as keyof DiscScores] -= 1;
  }

  return scores;
}

/**
 * D+I+S+C = 24 검증
 * 24문항 완료 후 서버 전송 직전에 호출
 * false면 응답 데이터 손상 - 처음부터 재시작 필요
 */
export function validateScores(scores: DiscScores): boolean {
  return scores.d + scores.i + scores.s + scores.c === 24;
}

/**
 * 문항별 Fisher-Yates 셔플
 * seed 없이 매 렌더마다 다른 순서 -> DISC 태그 추론 방지
 * QuestionCard에서 useMemo와 함께 사용 (문항 index가 바뀔 때만 재계산)
 */
export function shuffleOptions<T>(arr: T[]): T[] {
  const result = [...arr];
  for (let i = result.length - 1; i > 0; --i) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
}
