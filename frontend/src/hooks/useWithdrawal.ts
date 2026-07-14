import { useQuery } from "@tanstack/react-query";

// api-design.md GET /users/me/withdrawal-info — 탈퇴 전 삭제 항목 카운트
interface WithdrawalInfo {
  resultCount: number;
  chemistryCount: number;
  colleagueCount: number;
}

async function fetchWithdrawalInfo(): Promise<WithdrawalInfo> {
  const res = await fetch("/api/v1/users/me/withdrawal-info", {
    credentials: "include",
  });
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

export function useWithdrawalInfo() {
  return useQuery({
    queryKey: ["withdrawalInfo"],
    queryFn: fetchWithdrawalInfo,
    retry: false,
    staleTime: 0, // 탈퇴 화면 진입 시 항상 최신 카운트 조회
  });
}
