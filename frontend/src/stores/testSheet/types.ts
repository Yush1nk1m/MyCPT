export type SheetMode = "self" | "share";
export type SubmitStatus = "idle" | "submitting" | "done" | "error";
export type ShareStatus = "idle" | "creating" | "ready" | "error";

// POST /results/score 응답 타입 (api-design.md §2 참고)
export interface ScoreResult {
  scores: { d: number; i: number; s: number; c: number };
  buckets: { d: number; i: number; s: number; c: number };
  report: string;
}
