import { describe, it, expect } from "vitest";
import { formatCode } from "../colleague";

describe("formatCode", () => {
  it("소문자를 대문자화한다", () => {
    expect(formatCode("abcd1234")).toBe("ABCD1234");
  });

  it("특수문자를 제거한다", () => {
    expect(formatCode("AB-CD 12!34")).toBe("ABCD1234");
  });

  it("8자를 초과하면 앞 8자로 절단한다", () => {
    expect(formatCode("ABCD1234EFGH")).toBe("ABCD1234");
  });
});
