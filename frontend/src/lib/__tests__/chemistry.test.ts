import { describe, it, expect } from "vitest";
import { peerLabel } from "../chemistry";

describe("peerLabel", () => {
  it("myRole이 REQUESTER면 상대(partner) 닉네임을 사용한다", () => {
    expect(
      peerLabel({
        myRole: "REQUESTER",
        partnerNickname: "파트너",
        requesterNickname: "요청자",
      }),
    ).toBe("나 ↔ 파트너");
  });

  it("myRole이 PARTNER면 요청자(requester) 닉네임을 사용한다", () => {
    expect(
      peerLabel({
        myRole: "PARTNER",
        partnerNickname: "파트너",
        requesterNickname: "요청자",
      }),
    ).toBe("나 ↔ 요청자");
  });
});
