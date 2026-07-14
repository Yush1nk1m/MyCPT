import { describe, it, expect } from "vitest";
import { errorMessage } from "../assessment";

describe("errorMessage", () => {
  it("4가지 에러코드를 각각의 안내 문구로 매핑한다", () => {
    expect(errorMessage("EXPIRED_CODE")).toBe("초대장이 만료됐어요");
    expect(errorMessage("TOKEN_USED")).toBe("이미 응시가 완료된 링크예요");
    expect(errorMessage("NOT_FOUND")).toBe("존재하지 않는 링크예요");
    expect(errorMessage("INVALID_SCORE")).toBe(
      "응답 데이터가 올바르지 않아요. 처음부터 다시 시도해 주세요.",
    );
  });

  it("미지정 코드는 기본 메시지를 반환한다", () => {
    expect(errorMessage("FETCH_ERROR")).toBe(
      "제출에 실패했어요. 다시 시도해 주세요.",
    );
  });
});
