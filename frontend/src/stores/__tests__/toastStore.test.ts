import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { act } from "react";
import { useToastStore } from "../toastStore";

beforeEach(() => {
  vi.useFakeTimers();
  act(() => useToastStore.getState().hide());
});

afterEach(() => {
  vi.useRealTimers();
});

describe("show", () => {
  it("메시지를 설정한다", () => {
    act(() => useToastStore.getState().show("안내 메시지"));
    const { message, action } = useToastStore.getState();
    expect(message).toBe("안내 메시지");
    expect(action).toBeNull();
  });

  it("action이 없으면 2초 후 자동으로 hide된다", () => {
    act(() => useToastStore.getState().show("안내 메시지"));
    act(() => vi.advanceTimersByTime(1999));
    expect(useToastStore.getState().message).toBe("안내 메시지");
    act(() => vi.advanceTimersByTime(1));
    expect(useToastStore.getState().message).toBeNull();
  });

  it("action이 있으면 5초 후 자동으로 hide된다", () => {
    const onClick = vi.fn();
    act(() =>
      useToastStore.getState().show("액션 메시지", {
        action: { label: "실행", onClick },
      }),
    );
    act(() => vi.advanceTimersByTime(4999));
    expect(useToastStore.getState().message).toBe("액션 메시지");
    act(() => vi.advanceTimersByTime(1));
    expect(useToastStore.getState().message).toBeNull();
  });

  it("duration을 명시하면 우선 적용된다", () => {
    act(() =>
      useToastStore.getState().show("커스텀 메시지", { duration: 1000 }),
    );
    act(() => vi.advanceTimersByTime(999));
    expect(useToastStore.getState().message).toBe("커스텀 메시지");
    act(() => vi.advanceTimersByTime(1));
    expect(useToastStore.getState().message).toBeNull();
  });

  it("연속으로 show하면 이전 타이머가 취소된다", () => {
    act(() => useToastStore.getState().show("첫 메시지"));
    act(() => vi.advanceTimersByTime(1500));
    act(() => useToastStore.getState().show("두번째 메시지"));
    // 첫 타이머(2000ms) 시점이 지나도 두번째 메시지는 유지되어야 함
    act(() => vi.advanceTimersByTime(600));
    expect(useToastStore.getState().message).toBe("두번째 메시지");
  });

  it("hide 호출 시 즉시 초기화된다", () => {
    act(() => useToastStore.getState().show("메시지"));
    act(() => useToastStore.getState().hide());
    const { message, action } = useToastStore.getState();
    expect(message).toBeNull();
    expect(action).toBeNull();
  });
});
