import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/autoStore";
import { useEffect } from "react";

interface MeResponse {
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  coins: number;
  nextCoinAt: string | null;
  birthYear: number | null;
  gender: "M" | "F" | "N" | null;
}

async function fetchMe(): Promise<MeResponse> {
  const res = await fetch("/api/v1/auth/me", { credentials: "include" });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

export function useMe() {
  const setUser = useAuthStore((s) => s.setUser);
  const clear = useAuthStore((s) => s.clear);

  const query = useQuery({
    queryKey: ["me"],
    queryFn: fetchMe,
    retry: false, // 401은 재시도 의미 없음
    staleTime: 1000 * 60, // 1분 캐싱
  });

  useEffect(() => {
    if (query.data) setUser(query.data);
    if (query.error) clear();
  }, [query.data, query.error, setUser, clear]);

  return query;
}
