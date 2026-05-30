/**
 * 검사 시트 전역 상태 (Zustand)
 *
 * 관리 범위:
 *   - 시트 열림/닫힘, 현재 스텝
 *   - 현재 문항 인덱스 (0-based)
 *   - 문항별 응답 (Answers)
 *   - 중단 확인 다이얼로그 표시 여부
 *   - 제출 상태 (idle | submitting | done | error)
 *   - 제출 결과 (POST /results/score 응답)
 *
 * 비회원 sessionStorage 저장:
 *   제출 완료 후 scores를 sessionStorage['disc_scores']에 저장.
 *   로그인 후 결과 저장 연계 흐름(UC-01)에서 사용.
 */

import { create } from "zustand";
import {
  type Answers,
  type DiscScores,
  calculateScores,
  validateScores,
} from "@/lib/disc/scoring";
import { DiscTag } from "@/lib/disc/questions";
import { TOTAL_QUESTIONS } from "@/lib/disc/questions";

// POST /results/score 응답 타입 (api-design.md §2 참고)
export interface ScoreResult {
  testType: string;
  scores: { d: number; i: number; s: number; c: number };
  buckets: { d: number; i: number; s: number; c: number };
  report: {
    topType: "D" | "I" | "S" | "C";
    sections: Record<string, string>; // 6개 섹션 마크다운
  };
}

type SubmitStatus = "idle" | "submitting" | "done" | "error";

// 시트가 열리는 맥락 - 자기 평정 vs 타인 평정 공유 흐름
export type SheetMode = "self" | "share";

interface TestSheetState {
  // -- 시트 열림 제어 --
  isOpen: boolean;
  step: 1 | 2 | 3; // 1: 유형선택, 2: 응시중, 3: 결과
  mode: SheetMode;

  // -- 응시 진행 --
  currentIndex: number; // 현재 문항 인덱스(0-based)
  answers: Answers; // { [questionIndex]: { most, least } }

  // -- 중단 다이얼로그 --
  isCloseDialogOpen: boolean;

  // -- 제출 --
  submitStatus: SubmitStatus;
  result: ScoreResult | null;
  errorMessage: string | null;

  // -- 액션 --
  open: (mode?: SheetMode) => void;
  close: () => void; // Step 1이면 즉시, Step 2+ 이면 다이얼로그 열기
  forceClose: () => void; // 다이얼로그 "중단하고 닫기"
  dismissCloseDialog: () => void; // 다이얼로그 "계속 응시하기"

  goToStep: (step: 1 | 2 | 3) => void;
  setAnswer: (
    questionIndex: number,
    most: "D" | "I" | "S" | "C",
    least: "D" | "I" | "S" | "C",
  ) => void;
  handleAnswer: (most: DiscTag, least: DiscTag) => void;
  goNext: () => void; // 다음 문항 or 제출 트리거
  goPrev: () => void;

  submitScores: () => Promise<void>;
  reset: () => void;
}

const INITIAL: Pick<
  TestSheetState,
  | "isOpen"
  | "step"
  | "mode"
  | "currentIndex"
  | "answers"
  | "isCloseDialogOpen"
  | "submitStatus"
  | "result"
  | "errorMessage"
> = {
  isOpen: false,
  step: 1,
  mode: "self",
  currentIndex: 0,
  answers: {},
  isCloseDialogOpen: false,
  submitStatus: "idle",
  result: null,
  errorMessage: null,
};

export const useTestSheetStore = create<TestSheetState>((set, get) => ({
  ...INITIAL,

  open: (mode = "self") => set({ ...INITIAL, isOpen: true, mode }),

  close: () => {
    const { step } = get();
    if (step === 1) {
      // Step 1은 자유롭게 닫기 가능 (screens.yaml 명세)
      get().forceClose();
    } else {
      // Step 2+는 중단 확인 다이얼로그 표시
      set({ isCloseDialogOpen: true });
    }
  },

  forceClose: () => set({ ...INITIAL }),

  dismissCloseDialog: () => set({ isCloseDialogOpen: false }),

  goToStep: (step) => set({ step }),

  setAnswer: (questionIndex, most, least) =>
    set((state) => ({
      answers: {
        ...state.answers,
        [questionIndex]: { most, least },
      },
    })),

  handleAnswer: (most: DiscTag, least: DiscTag) => {
    const { currentIndex } = get();
    get().setAnswer(currentIndex, most, least);
  },

  goNext: () => {
    const { currentIndex, answers } = get();
    const isLastQuestion = currentIndex === TOTAL_QUESTIONS - 1;

    if (!isLastQuestion) {
      set({ currentIndex: currentIndex + 1 });
    } else {
      // 마지막 문항 -> 제출
      get().submitScores();
    }
  },

  goPrev: () => {
    const { currentIndex } = get();
    if (currentIndex > 0) set({ currentIndex: currentIndex - 1 });
  },

  submitScores: async () => {
    const { answers } = get();

    // 24문항 모두 응답했는지 확인
    if (Object.keys(answers).length < TOTAL_QUESTIONS) return;

    const scores = calculateScores(answers);

    // 불변 조건 검증 (D+I+S+C=24)
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
        body: JSON.stringify({ testType: "DISC", scores }),
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const data: ScoreResult = await res.json();

      // 비회원 sessionStorage 임시 저장 (UC-01: 로그인 후 결과 저장 연계)
      try {
        sessionStorage.setItem("disc_scores", JSON.stringify(scores));
      } catch {
        // sessionStorage 접근 불가 환경 무시 (예: 시크릿 모드 설정)
        console.log("세션 스토리지 접근 불가");
      }

      set({ submitStatus: "done", result: data });
    } catch (err) {
      set({
        submitStatus: "error",
        step: 2, // 에러 시 응시 화면으로 되돌아감
        errorMessage:
          err instanceof Error
            ? err.message
            : "알 수 없는 오류가 발생했습니다.",
      });
    }
  },

  reset: () => set({ ...INITIAL }),
}));
