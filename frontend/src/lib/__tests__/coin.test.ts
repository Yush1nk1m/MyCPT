import { describe, it, expect } from "vitest";
import { calcRemaining, reasonLabel, formatNextCharge } from "../coin";

describe("calcRemaining", () => {
  const now = new Date("2026-07-14T00:00:00Z").getTime();

  it("양수 차이면 'HH : MM : SS' 형태로 반환한다", () => {
    const nextCoinAt = new Date(now + (1 * 3600_000 + 2 * 60_000 + 3_000)).toISOString();
    expect(calcRemaining(nextCoinAt, now)).toBe("01 : 02 : 03");
  });

  it("diff가 0 이하면 '00 : 00 : 00'을 반환한다", () => {
    const nextCoinAt = new Date(now - 1_000).toISOString();
    expect(calcRemaining(nextCoinAt, now)).toBe("00 : 00 : 00");
  });

  it("한 자리 수는 0으로 패딩한다", () => {
    const nextCoinAt = new Date(now + (5_000)).toISOString();
    expect(calcRemaining(nextCoinAt, now)).toBe("00 : 00 : 05");
  });
});

describe("reasonLabel", () => {
  it("SIGNUP/RECHARGE/CHEMISTRY_REPORT를 한글 라벨로 매핑한다", () => {
    expect(reasonLabel("SIGNUP")).toBe("신규 가입 보너스");
    expect(reasonLabel("RECHARGE")).toBe("자동 충전");
    expect(reasonLabel("CHEMISTRY_REPORT")).toBe("케미 보고서 발행");
  });

  it("미지정 코드는 원문 그대로 반환한다(passthrough)", () => {
    expect(reasonLabel("UNKNOWN_REASON")).toBe("UNKNOWN_REASON");
  });
});

describe("formatNextCharge", () => {
  const now = new Date("2026-07-14T00:00:00Z").getTime();

  it("nextCoinAt이 null이면 '만충 상태'를 반환한다", () => {
    expect(formatNextCharge(null, now)).toBe("만충 상태");
  });

  it("diff가 0 이하면 '곧 충전 예정'을 반환한다", () => {
    const nextCoinAt = new Date(now - 1_000).toISOString();
    expect(formatNextCharge(nextCoinAt, now)).toBe("곧 충전 예정");
  });

  it("양수면 '약 N시간 M분 후'를 반환한다", () => {
    const nextCoinAt = new Date(now + (2 * 3600_000 + 15 * 60_000)).toISOString();
    expect(formatNextCharge(nextCoinAt, now)).toBe("약 2시간 15분 후");
  });
});
