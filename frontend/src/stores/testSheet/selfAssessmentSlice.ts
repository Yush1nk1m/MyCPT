import { StateCreator } from "zustand";
import {
  type Answers,
  type DiscScores,
  calculateScores,
  validateScores,
} from "@/lib/disc/scoring";
import { DiscTag, TOTAL_QUESTIONS } from "@/lib/disc/questions";
import type { TestSheetState } from "../testSheetStore";
import type { ScoreResult, SubmitStatus } from "./types";

export interface SelfAssessmentSlice {
  currentIndex: number;
  answers: Answers;
  submitStatus: SubmitStatus;
  result: ScoreResult | null;
  errorMessage: string | null;

  setAnswer: (questionIndex: number, most: DiscTag, least: DiscTag) => void;
  handleAnswer: (most: DiscTag, least: DiscTag) => void;
  goNext: () => void;
  goPrev: () => void;
  submitScores: () => Promise<void>;
}

export const SELF_INITIAL: Pick<
  SelfAssessmentSlice,
  "currentIndex" | "answers" | "submitStatus" | "result" | "errorMessage"
> = {
  currentIndex: 0,
  answers: {},
  submitStatus: "idle",
  result: null,
  errorMessage: null,
};

export const createSelfAssessmentSlice: StateCreator<
  TestSheetState,
  [],
  [],
  SelfAssessmentSlice
> = (set, get) => ({
  ...SELF_INITIAL,

  setAnswer: (questionIndex, most, least) =>
    set((state) => ({
      answers: { ...state.answers, [questionIndex]: { most, least } },
    })),

  handleAnswer: (most, least) => {
    const { currentIndex } = get();
    get().setAnswer(currentIndex, most, least);
  },

  goNext: () => {
    const { currentIndex } = get();
    const isLastQuestion = currentIndex === TOTAL_QUESTIONS - 1;
    if (!isLastQuestion) {
      set({ currentIndex: currentIndex + 1 });
    } else {
      get().submitScores();
    }
  },

  goPrev: () => {
    const { currentIndex } = get();
    if (currentIndex > 0) set({ currentIndex: currentIndex - 1 });
  },

  submitScores: async () => {
    const { answers } = get();
    if (Object.keys(answers).length < TOTAL_QUESTIONS) return;

    const scores = calculateScores(answers);

    if (!validateScores(scores)) {
      set({
        submitStatus: "error",
        errorMessage:
          "응답 데이터가 올바르지 않습니다. 처음부터 다시 시도해 주세요.",
      });
      return;
    }

    set({ submitStatus: "submitting", step: 3 });

    try {
      const res = await fetch("/api/v1/results/score", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ scores }),
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const data: ScoreResult = await res.json();

      try {
        sessionStorage.setItem(
          "disc_result",
          JSON.stringify({ scores: data.scores, buckets: data.buckets }),
        );
      } catch {
        console.log("세션 스토리지 접근 불가");
      }

      set({ submitStatus: "done", result: data });
    } catch (err) {
      set({
        submitStatus: "error",
        step: 2,
        errorMessage:
          err instanceof Error
            ? err.message
            : "알 수 없는 오류가 발생했습니다.",
      });
    }
  },
});
