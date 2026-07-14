// 회원탈퇴 사유 결합 유틸
// screens.yaml me-leave-step1: 체크 프리셋(복수) + 자유 입력을 단일 reason 문자열로 결합.
// 서버 계약(api-design.md DELETE /users/me): reason 최대 200자.

export const CONFIRM_PHRASE = "탈퇴할게요";
const MAX_REASON_LENGTH = 200;

/**
 * 선택된 사유 프리셋 라벨과 자유 입력을 하나의 reason 문자열로 합친다.
 * 빈 항목은 제외하고 " / "로 연결한 뒤 200자로 절단한다.
 */
export function combineReason(presets: string[], freeText: string): string {
  return [...presets, freeText.trim()]
    .filter(Boolean)
    .join(" / ")
    .slice(0, MAX_REASON_LENGTH);
}
