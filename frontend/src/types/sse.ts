// 알림 타입별 referenceId 의미가 다름
//   CHEMISTRY_REPORT  → referenceId = chemistry_reports.id
//   CHEMISTRY_ERROR   → referenceId = chemistry_reports.id
//   COLLEAGUE_REGISTERED → referenceId = colleagues.id
export type SseEventType =
  | "CHEMISTRY_REPORT"
  | "CHEMISTRY_ERROR"
  | "COLLEAGUE_REGISTERED";

export interface SseEventPayload {
  referenceId: number;
  message: string;
}
