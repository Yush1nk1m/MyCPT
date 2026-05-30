import { describe, it, expect, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useTestSheet } from "../useTestSheet";
import { useTestSheetStore } from "@/stores/testSheetStore";

beforeEach(() => {
  useTestSheetStore.getState().reset();
});

describe("useTestSheet", () => {
  it("초기 상태에서 isOpen=false, step=1 이다", () => {
    const { result } = renderHook(() => useTestSheet());
    expect(result.current.isOpen).toBe(false);
    expect(result.current.step).toBe(1);
  });

  it("open() 호출 후 isOpen=true 가 된다", () => {
    const { result } = renderHook(() => useTestSheet());
    act(() => result.current.open());
    expect(result.current.isOpen).toBe(true);
  });

  it("handleAnswer 호출 시 currentAnswer가 갱신된다", () => {
    const { result } = renderHook(() => useTestSheet());
    act(() => {
      result.current.open();
      result.current.goToStep(2);
      result.current.handleAnswer("D", "I");
    });
    expect(result.current.currentAnswer).toEqual({ most: "D", least: "I" });
  });

  it("isCurrentAnswered는 Most와 Least 모두 선택돼야 true가 된다", () => {
    const { result } = renderHook(() => useTestSheet());
    act(() => {
      result.current.open();
      result.current.goToStep(2);
    });
    expect(result.current.isCurrentAnswered).toBe(false);

    act(() => result.current.handleAnswer("D", "I"));
    expect(result.current.isCurrentAnswered).toBe(true);
  });

  it("answeredCount는 응답한 문항 수를 정확히 반환한다", () => {
    const { result } = renderHook(() => useTestSheet());
    act(() => {
      result.current.open();
      result.current.goToStep(2);
      result.current.handleAnswer("D", "I");
      result.current.goNext();
      result.current.handleAnswer("S", "C");
    });
    expect(result.current.answeredCount).toBe(2);
  });
});
