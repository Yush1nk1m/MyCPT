import { StateCreator } from "zustand";
import type { TestSheetState } from "../testSheetStore";
import type { ShareStatus } from "./types";

export interface ShareSlice {
  shareLabel: string;
  shareStatus: ShareStatus;
  shareToken: string | null;
  shareExpiresAt: string | null;
  shareError: string | null;

  setShareLabel: (label: string) => void;
  createShareLink: () => Promise<void>;
}

export const SHARE_INITIAL: Pick<
  ShareSlice,
  "shareLabel" | "shareStatus" | "shareToken" | "shareExpiresAt" | "shareError"
> = {
  shareLabel: "",
  shareStatus: "idle",
  shareToken: null,
  shareExpiresAt: null,
  shareError: null,
};

export const createShareSlice: StateCreator<
  TestSheetState,
  [],
  [],
  ShareSlice
> = (set, get) => ({
  ...SHARE_INITIAL,

  setShareLabel: (label) => set({ shareLabel: label }),

  createShareLink: async () => {
    const { shareLabel } = get();
    if (!shareLabel.trim()) return;

    set({ step: 3, shareStatus: "creating", shareError: null });

    try {
      const res = await fetch("/api/v1/assessments", {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ label: shareLabel.trim() }),
      });
      if (!res.ok) throw new Error("FAILED");
      const data: { token: string; expiresAt: string } = await res.json();
      set({
        shareStatus: "ready",
        shareToken: data.token,
        shareExpiresAt: data.expiresAt,
      });
    } catch {
      set({
        shareStatus: "error",
        shareError: "링크 생성에 실패했어요. 다시 시도해 주세요.",
      });
    }
  },
});
