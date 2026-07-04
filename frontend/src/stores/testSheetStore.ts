/**
 * 검사 시트 전역 상태 (Zustand) — 조합부
 *
 * 세 슬라이스로 역할 분리:
 *   - chromeSlice          시트 열림/닫힘, 스텝, 모드
 *   - selfAssessmentSlice  자기평정 응시 진행 + 제출
 *   - shareSlice           타인 평정 공유 링크 생성
 *
 * open()/close()/forceClose()/reset()은 여러 슬라이스를 동시에 초기화해야 해서
 * 개별 슬라이스가 아니라 여기 조합부에 둠.
 */

import { create } from "zustand";
import {
  createChromeSlice,
  CHROME_INITIAL,
  type ChromeSlice,
} from "./testSheet/chromeSlice";
import {
  createSelfAssessmentSlice,
  SELF_INITIAL,
  type SelfAssessmentSlice,
} from "./testSheet/selfAssessmentSlice";
import {
  createShareSlice,
  SHARE_INITIAL,
  type ShareSlice,
} from "./testSheet/shareSlice";
import type { SheetMode } from "./testSheet/types";

export type { SheetMode, ScoreResult } from "./testSheet/types";

export type TestSheetState = ChromeSlice &
  SelfAssessmentSlice &
  ShareSlice & {
    open: (mode?: SheetMode) => void;
    close: () => void; // Step 1이면 즉시, share 모드면 항상 즉시, 그 외엔 다이얼로그
    forceClose: () => void;
    reset: () => void;
  };

const FULL_INITIAL = { ...CHROME_INITIAL, ...SELF_INITIAL, ...SHARE_INITIAL };

export const useTestSheetStore = create<TestSheetState>((set, get, store) => ({
  ...createChromeSlice(set, get, store),
  ...createSelfAssessmentSlice(set, get, store),
  ...createShareSlice(set, get, store),

  open: (mode = "self") => set({ ...FULL_INITIAL, isOpen: true, mode }),

  close: () => {
    const { step, mode } = get();
    if (mode === "share" || step === 1) {
      get().forceClose();
    } else {
      set({ isCloseDialogOpen: true });
    }
  },

  forceClose: () => set({ ...FULL_INITIAL }),
  reset: () => set({ ...FULL_INITIAL }),
}));
