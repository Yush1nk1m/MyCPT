"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";

export function Providers({ children }: { children: React.ReactNode }) {
  // useState로 생성 - 컴포넌트 외부에 두면 서버/클라이언트 간 공유되어 데이터 오염 발생
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 1000 * 60, // 기본 1분 캐싱
            retry: false, // 실패 시 재시도 없음 (401 등 즉시 처리)
          },
        },
      }),
  );

  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}
