import { describe, it, expect } from "vitest";
import {
  formatDateDot,
  formatDateTime,
  formatDateKo,
  formatRelative,
} from "../format";

describe("formatDateDot", () => {
  it("ISO 문자열의 앞 10자를 취한다", () => {
    expect(formatDateDot("2026-05-24T14:30:00")).toBe("2026.05.24");
  });

  it("하이픈을 점으로 치환한다", () => {
    expect(formatDateDot("2026-01-01T00:00:00")).toBe("2026.01.01");
  });
});

describe("formatDateTime", () => {
  it("날짜와 시각을 조합해 반환한다", () => {
    // Z 없는 로컬 시각 문자열 — 시스템 로컬 타임존 기준으로 결정적 파싱됨
    expect(formatDateTime("2026-05-24T14:30:00")).toBe("2026.05.24 14:30");
  });

  it("분 단위를 0으로 패딩한다", () => {
    expect(formatDateTime("2026-05-24T09:05:00")).toBe("2026.05.24 09:05");
  });
});

describe("formatDateKo", () => {
  it("한국어 긴 날짜 포맷으로 변환한다", () => {
    expect(formatDateKo("2026-07-14T00:00:00")).toBe("2026년 7월 14일");
  });
});

describe("formatRelative", () => {
  const now = new Date("2026-07-14T12:00:00").getTime();

  it("1분 미만이면 '방금 전'을 반환한다", () => {
    const iso = new Date(now - 30_000).toISOString();
    expect(formatRelative(iso, now)).toBe("방금 전");
  });

  it("1시간 미만이면 'N분 전'을 반환한다", () => {
    const iso = new Date(now - 5 * 60_000).toISOString();
    expect(formatRelative(iso, now)).toBe("5분 전");
  });

  it("24시간 미만이면 'N시간 전'을 반환한다", () => {
    const iso = new Date(now - 3 * 3_600_000).toISOString();
    expect(formatRelative(iso, now)).toBe("3시간 전");
  });

  it("7일 미만이면 'N일 전'을 반환한다", () => {
    const iso = new Date(now - 2 * 86_400_000).toISOString();
    expect(formatRelative(iso, now)).toBe("2일 전");
  });

  it("7일 이상이면 절대 날짜(YYYY.MM.DD)를 반환한다", () => {
    const iso = new Date(now - 8 * 86_400_000).toISOString();
    const d = new Date(now - 8 * 86_400_000);
    const pad = (n: number) => String(n).padStart(2, "0");
    const expected = `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
    expect(formatRelative(iso, now)).toBe(expected);
  });
});
