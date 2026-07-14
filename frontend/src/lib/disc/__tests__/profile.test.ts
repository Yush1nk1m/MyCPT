import { describe, it, expect } from "vitest";
import { getDiscProfile } from "../profile";

describe("getDiscProfile", () => {
  it("전 축이 2이면 balanced를 반환한다", () => {
    expect(getDiscProfile({ d: 2, i: 2, s: 2, c: 2 })).toEqual({
      kind: "balanced",
    });
  });

  it("단일 최댓값이면 typed에 단일 유형만 담는다", () => {
    expect(getDiscProfile({ d: 3, i: 1, s: 2, c: 1 })).toEqual({
      kind: "typed",
      types: ["D"],
    });
  });

  it("공동 최댓값이면 typed에 복수 유형을 담는다", () => {
    expect(getDiscProfile({ d: 3, i: 3, s: 1, c: 1 })).toEqual({
      kind: "typed",
      types: ["D", "I"],
    });
  });
});
