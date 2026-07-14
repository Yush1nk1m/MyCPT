// 코인 관련 순수 로직 모음 (me/coins, colleagues/[id]에서 추출)

/** nextCoinAt까지 남은 시간을 "HH : MM : SS"로 반환. diff<=0이면 "00 : 00 : 00" */
export function calcRemaining(
  nextCoinAt: string,
  now: number = Date.now(),
): string {
  const diff = new Date(nextCoinAt).getTime() - now;
  if (diff <= 0) return "00 : 00 : 00";
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  const s = Math.floor((diff % 60000) / 1000);
  return `${String(h).padStart(2, "0")} : ${String(m).padStart(2, "0")} : ${String(s).padStart(2, "0")}`;
}

/** 코인 거래 사유 코드 → 한글 라벨. SIGNUP/RECHARGE/CHEMISTRY_REPORT 외엔 원문 그대로(passthrough) */
export function reasonLabel(reason: string): string {
  if (reason === "SIGNUP") return "신규 가입 보너스";
  if (reason === "RECHARGE") return "자동 충전";
  if (reason === "CHEMISTRY_REPORT") return "케미 보고서 발행";
  return reason;
}

/**
 * 다음 충전까지 남은 시간을 안내 문구로 변환.
 * nextCoinAt이 null(만충)이면 "만충 상태", diff<=0이면 "곧 충전 예정",
 * 그 외엔 "약 N시간 M분 후"
 */
export function formatNextCharge(
  nextCoinAt: string | null,
  now: number = Date.now(),
): string {
  if (!nextCoinAt) return "만충 상태";
  const diff = new Date(nextCoinAt).getTime() - now;
  if (diff <= 0) return "곧 충전 예정";
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  return `약 ${h}시간 ${m}분 후`;
}
