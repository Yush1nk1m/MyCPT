import { describe, it, expect } from "vitest";
import { calculateScores, validateScores, shuffleOptions } from "../scoring";
import type { Answers } from "../scoring";

describe("calculateScores", () => {
  it("Most +2, Least -1 규칙을 정확히 적용한다", () => {
    // 24문항 전부 D=Most, I=Least 로 응답한 극단 케이스
    const answers: Answers = {};
    for (let i = 0; i < 24; i++) {
      answers[i] = { most: "D", least: "I" };
    }
    const scores = calculateScores(answers);
    expect(scores.d).toBe(48); // 24 × +2
    expect(scores.i).toBe(-24); // 24 × -1
    expect(scores.s).toBe(0);
    expect(scores.c).toBe(0);
  });

  it("빈 응답이면 모든 척도가 0이다", () => {
    const scores = calculateScores({});
    expect(scores).toEqual({ d: 0, i: 0, s: 0, c: 0 });
  });
});

describe("validateScores", () => {
  it("D+I+S+C=24 이면 true를 반환한다", () => {
    // Most 한 번에 +2, Least 한 번에 -1 → 24문항 합계는 항상 24
    expect(validateScores({ d: 24, i: 0, s: 0, c: 0 })).toBe(true);
    expect(validateScores({ d: 10, i: 8, s: 4, c: 2 })).toBe(true);
  });

  it("합계가 24가 아니면 false를 반환한다", () => {
    expect(validateScores({ d: 10, i: 10, s: 10, c: 10 })).toBe(false);
    expect(validateScores({ d: 0, i: 0, s: 0, c: 0 })).toBe(false);
  });
});

describe("shuffleOptions", () => {
  it("원본 배열 길이를 유지한다", () => {
    const input = [
      { id: "0-D", text: "A", tag: "D" as const },
      { id: "0-I", text: "B", tag: "I" as const },
      { id: "0-S", text: "C", tag: "S" as const },
      { id: "0-C", text: "D", tag: "C" as const },
    ];
    expect(shuffleOptions(input)).toHaveLength(4);
  });

  it("원본 배열을 변형하지 않는다 (불변성)", () => {
    const input = [{ id: "0-D", text: "A", tag: "D" as const }];
    const original = [...input];
    shuffleOptions(input);
    expect(input).toEqual(original);
  });
});
