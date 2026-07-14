import { describe, it, expect, beforeEach } from "vitest";
import { act } from "react";
import { useAuthStore } from "../authStore";

const sampleUser = {
  userId: 1,
  nickname: "철수",
  profileImageUrl: null,
  coins: 3,
  nextCoinAt: null,
  birthYear: 2000,
  gender: "M" as const,
};

beforeEach(() => {
  act(() => useAuthStore.getState().clear());
});

describe("setUser", () => {
  it("user를 설정하면 isAuthenticated가 true가 된다", () => {
    act(() => useAuthStore.getState().setUser(sampleUser));
    const { user, isAuthenticated } = useAuthStore.getState();
    expect(user).toEqual(sampleUser);
    expect(isAuthenticated).toBe(true);
  });

  it("null을 설정하면 isAuthenticated가 false가 된다", () => {
    act(() => {
      useAuthStore.getState().setUser(sampleUser);
      useAuthStore.getState().setUser(null);
    });
    const { user, isAuthenticated } = useAuthStore.getState();
    expect(user).toBeNull();
    expect(isAuthenticated).toBe(false);
  });
});

describe("clear", () => {
  it("호출하면 초기 상태로 되돌아간다", () => {
    act(() => {
      useAuthStore.getState().setUser(sampleUser);
      useAuthStore.getState().clear();
    });
    const { user, isAuthenticated } = useAuthStore.getState();
    expect(user).toBeNull();
    expect(isAuthenticated).toBe(false);
  });
});
