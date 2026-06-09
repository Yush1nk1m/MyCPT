import { useInfiniteQuery } from "@tanstack/react-query";
export type RaterType = "SELF" | "OTHER";

interface DiscBuckets {
  d: number;
  i: number;
  s: number;
  c: number;
}

export interface ResultSummary {
  resultId: number;
  raterType: RaterType;
  label: string | null;
  buckets: DiscBuckets;
  createdAt: string;
}

interface ResultListResponse {
  results: ResultSummary[];
  nextCursor: number | null;
  hasNext: boolean;
}

async function fetchResults(
  raterType: RaterType | null,
  cursor: number | null,
): Promise<ResultListResponse> {
  const params = new URLSearchParams();
  if (raterType) params.set("raterType", raterType);
  if (cursor) params.set("cursor", String(cursor));
  params.set("size", "5");

  const res = await fetch(`/api/v1/results?${params}`, {
    credentials: "include",
  });
  if (res.status === 401) return Promise.reject(new Error("UNAUTHORIZED"));
  if (!res.ok) return Promise.reject(new Error("FETCH_ERROR"));
  return res.json();
}

export function useResults(raterType: RaterType | null) {
  return useInfiniteQuery({
    queryKey: ["results", raterType],
    queryFn: ({ pageParam: cursor }: { pageParam: number | null }) =>
      fetchResults(raterType, cursor),
    initialPageParam: null,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.nextCursor : undefined,
  });
}
