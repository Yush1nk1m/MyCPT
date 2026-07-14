import { describe, it, expect, vi } from "vitest";
import { toRaterStem, DISC_QUESTIONS, TOTAL_QUESTIONS } from "../questions";

describe("toRaterStem", () => {
  it("문미의 '나는…'을 '{대상자}님은…'으로 1인칭 치환한다", () => {
    expect(toRaterStem("친구들과 여행 계획을 세울 때, 나는…", 0, "철수")).toBe(
      "친구들과 여행 계획을 세울 때, 철수님은…",
    );
  });

  it("Q22(index=22)는 불규칙 override를 사용한다", () => {
    expect(
      toRaterStem("누군가 내 결과물을 칭찬했을 때, 나는…", 22, "영희"),
    ).toBe("누군가 영희님의 결과물을 칭찬했을 때, 영희님은…");
  });

  it("'나는…'으로 끝나지 않는 비매칭 패턴은 원문을 그대로 반환하고 경고를 남긴다", () => {
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
    const stem = "패턴에 맞지 않는 문항입니다";
    expect(toRaterStem(stem, 99, "철수")).toBe(stem);
    expect(warnSpy).toHaveBeenCalled();
    warnSpy.mockRestore();
  });
});

describe("DISC_QUESTIONS", () => {
  it("문항이 24개다", () => {
    expect(DISC_QUESTIONS).toHaveLength(24);
    expect(TOTAL_QUESTIONS).toBe(24);
  });

  it("각 문항은 D/I/S/C 보기를 정확히 1개씩 포함한다", () => {
    for (const q of DISC_QUESTIONS) {
      expect(q.options).toHaveLength(4);
      const tags = q.options.map((o) => o.tag).sort();
      expect(tags).toEqual(["C", "D", "I", "S"]);
    }
  });

  it("optionId는 '{questionIndex}-{tag}' 포맷을 따른다", () => {
    for (const q of DISC_QUESTIONS) {
      for (const opt of q.options) {
        expect(opt.id).toBe(`${q.index}-${opt.tag}`);
      }
    }
  });
});
