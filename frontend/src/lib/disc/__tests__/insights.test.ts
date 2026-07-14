import { describe, it, expect } from "vitest";
import { bucketToPercent, dominantDeviation } from "../insights";

describe("bucketToPercent", () => {
  it("3은 100을 반환한다", () => {
    expect(bucketToPercent(3)).toBe(100);
  });

  it("1은 33을 반환한다", () => {
    expect(bucketToPercent(1)).toBe(33);
  });

  it("반올림해 정수를 반환한다", () => {
    expect(bucketToPercent(2)).toBe(67); // (2/3)*100 = 66.66... → 반올림
  });
});

describe("dominantDeviation", () => {
  it("편차 절대값이 가장 큰 축을 반환한다", () => {
    const my = { d: 3, i: 2, s: 1, c: 2 };
    const avg = { d: 1, i: 2, s: 2, c: 2 };
    // d: +2, s: -1 → d가 최대 편차
    expect(dominantDeviation(my, avg)).toEqual({
      axis: "D",
      diff: 2,
      direction: "높아요",
    });
  });

  it("음수 편차면 direction이 '낮아요'다", () => {
    const my = { d: 1, i: 2, s: 2, c: 2 };
    const avg = { d: 3, i: 2, s: 2, c: 2 };
    expect(dominantDeviation(my, avg)).toEqual({
      axis: "D",
      diff: -2,
      direction: "낮아요",
    });
  });

  it("전 축의 편차가 0이면 null을 반환한다", () => {
    const my = { d: 2, i: 2, s: 2, c: 2 };
    const avg = { d: 2, i: 2, s: 2, c: 2 };
    expect(dominantDeviation(my, avg)).toBeNull();
  });
});
