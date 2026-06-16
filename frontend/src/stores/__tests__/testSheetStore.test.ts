import { describe, it, expect, beforeEach, vi } from "vitest";
import { act } from "react";
import { useTestSheetStore } from "../testSheetStore";
import { TOTAL_QUESTIONS } from "@/lib/disc/questions";

// 테스트마다 스토어를 초기 상태로 리셋
beforeEach(() => {
  useTestSheetStore.getState().reset();
});

describe("open / close", () => {
  it("open() 호출 시 isOpen=true, step=1 이 된다", () => {
    act(() => useTestSheetStore.getState().open());
    const { isOpen, step } = useTestSheetStore.getState();
    expect(isOpen).toBe(true);
    expect(step).toBe(1);
  });

  it("Step 1에서 close()는 즉시 닫힌다 (다이얼로그 없음)", () => {
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().close();
    });
    expect(useTestSheetStore.getState().isOpen).toBe(false);
  });

  it("Step 2에서 close()는 중단 다이얼로그를 연다", () => {
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().goToStep(2);
      useTestSheetStore.getState().close();
    });
    const { isOpen, isCloseDialogOpen } = useTestSheetStore.getState();
    expect(isOpen).toBe(true); // 시트는 아직 열려있음
    expect(isCloseDialogOpen).toBe(true); // 다이얼로그만 열림
  });

  it("forceClose()는 스토어를 완전히 초기화한다", () => {
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().goToStep(2);
      useTestSheetStore.getState().forceClose();
    });
    const { isOpen, step, answers } = useTestSheetStore.getState();
    expect(isOpen).toBe(false);
    expect(step).toBe(1);
    expect(answers).toEqual({});
  });
});

describe("setAnswer / goNext / goPrev", () => {
  it("setAnswer()가 answers에 누적된다", () => {
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().goToStep(2);
      useTestSheetStore.getState().setAnswer(0, "D", "I");
    });
    expect(useTestSheetStore.getState().answers[0]).toEqual({
      most: "D",
      least: "I",
    });
  });

  it("goNext()는 마지막 문항이 아니면 currentIndex를 1 증가시킨다", () => {
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().goToStep(2);
      useTestSheetStore.getState().goNext();
    });
    expect(useTestSheetStore.getState().currentIndex).toBe(1);
  });

  it("goPrev()는 첫 문항에서 호출해도 0 미만으로 가지 않는다", () => {
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().goToStep(2);
      useTestSheetStore.getState().goPrev();
    });
    expect(useTestSheetStore.getState().currentIndex).toBe(0);
  });
});

describe("submitScores", () => {
  it("24문항 미완료 시 제출하지 않는다", async () => {
    const fetchSpy = vi.spyOn(global, "fetch");
    act(() => {
      useTestSheetStore.getState().open();
      useTestSheetStore.getState().setAnswer(0, "D", "I"); // 1문항만
    });
    await act(async () => {
      await useTestSheetStore.getState().submitScores();
    });
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it("fetch 성공 시 submitStatus=done, result에 응답이 담긴다", async () => {
    // 24문항 모두 응답 세팅
    act(() => {
      useTestSheetStore.getState().open();
      for (let i = 0; i < TOTAL_QUESTIONS; i++) {
        useTestSheetStore.getState().setAnswer(i, "D", "I");
      }
    });

    const mockResult = {
      scores: { d: 48, i: -24, s: 0, c: 0 },
      buckets: { d: 3, i: 1, s: 2, c: 2 },
      report: "## 결과 개요\n테스트 보고서",
    };
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      json: async () => mockResult,
    } as Response);

    await act(async () => {
      await useTestSheetStore.getState().submitScores();
    });

    const { submitStatus, result } = useTestSheetStore.getState();
    expect(submitStatus).toBe("done");
    expect(result?.report).toContain("## 결과 개요");
  });

  it("fetch 실패 시 submitStatus=error, step이 2로 되돌아간다", async () => {
    act(() => {
      useTestSheetStore.getState().open();
      for (let i = 0; i < TOTAL_QUESTIONS; i++) {
        useTestSheetStore.getState().setAnswer(i, "D", "I");
      }
    });

    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: false,
      status: 500,
    } as Response);

    await act(async () => {
      await useTestSheetStore.getState().submitScores();
    });

    const { submitStatus, step } = useTestSheetStore.getState();
    expect(submitStatus).toBe("error");
    expect(step).toBe(2);
  });
});
