// 날짜·시각 포맷터 모음
// 여러 페이지에 흩어져 있던 동일/유사 로직을 추출해 재사용·단위 테스트가
// 가능하도록 순수 함수로 정리한다.

/** "2026-05-24T14:30:00" → "2026.05.24" (앞 10자 slice) */
export function formatDateDot(iso: string): string {
  return iso.slice(0, 10).replace(/-/g, ".");
}

/** "2026-05-24T14:30:00" → "2026.05.24 14:30" (로컬 Date 기준) */
export function formatDateTime(iso: string): string {
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** "2026-05-24T14:30:00" → "2026년 5월 24일" (ko-KR long) */
export function formatDateKo(iso: string): string {
  return new Date(iso).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

/** 상대 시간 표기: "방금 전"/"N분 전"/"N시간 전"/"N일 전"/그 외엔 "YYYY.MM.DD" */
export function formatRelative(iso: string, now: number = Date.now()): string {
  const d = new Date(iso);
  const diff = now - d.getTime();

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return "방금 전";
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (days < 7) return `${days}일 전`;

  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
}
