// 동료 초대 코드 관련 순수 로직 (colleagues/page에서 추출)

/** 코드 포맷: 입력값을 대문자화 → 비[A-Z0-9] 제거 → 앞 8자로 정규화 */
export function formatCode(raw: string): string {
  return raw
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, "")
    .slice(0, 8);
}
