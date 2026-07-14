import { describe, it, expect } from "vitest";
import { combineReason } from "../withdrawal";

describe("combineReason", () => {
  it("프리셋만 있으면 ' / '로 연결한다", () => {
    expect(combineReason(["자주 쓰지 않게 됐어요", "기타"], "")).toBe(
      "자주 쓰지 않게 됐어요 / 기타",
    );
  });

  it("자유 입력만 있으면 그대로 반환한다", () => {
    expect(combineReason([], "결과가 부정확해요")).toBe("결과가 부정확해요");
  });

  it("프리셋과 자유 입력을 모두 결합한다", () => {
    expect(combineReason(["기타"], "이유 설명")).toBe("기타 / 이유 설명");
  });

  it("아무것도 없으면 빈 문자열을 반환한다", () => {
    expect(combineReason([], "   ")).toBe("");
  });

  it("결합 결과를 200자로 절단한다", () => {
    const long = "가".repeat(300);
    expect(combineReason([], long)).toHaveLength(200);
  });
});
