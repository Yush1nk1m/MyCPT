import { StateCreator } from "zustand";
import type { TestSheetState } from "../testSheetStore";
import type { SheetMode } from "./types";

export interface ChromeSlice {
  isOpen: boolean;
  step: 1 | 2 | 3; // 1: 유형선택, 2: 응시중/라벨입력, 3: 결과/링크발급
  mode: SheetMode;
  isCloseDialogOpen: boolean;

  goToStep: (step: 1 | 2 | 3) => void;
  dismissCloseDialog: () => void;
}

// open()/forceClose()/reset()은 다른 슬라이스 상태도 함께 초기화해야 해서
// 이 슬라이스가 아니라 testSheetStore.ts(조합부)에 정의됨
export const CHROME_INITIAL: Pick<
  ChromeSlice,
  "isOpen" | "step" | "mode" | "isCloseDialogOpen"
> = {
  isOpen: false,
  step: 1,
  mode: "self",
  isCloseDialogOpen: false,
};

export const createChromeSlice: StateCreator<
  TestSheetState,
  [],
  [],
  ChromeSlice
> = (set) => ({
  ...CHROME_INITIAL,
  goToStep: (step) => set({ step }),
  dismissCloseDialog: () => set({ isCloseDialogOpen: false }),
});
