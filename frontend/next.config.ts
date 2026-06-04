import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  async rewrites() {
    return [
      {
        // 클라이언트가 /api 로 요청하면
        source: "/api/:path*",
        // 백엔드 서버로 프록시
        destination: `${process.env.BACKEND_URL}/api/:path*`,
      },
    ];
  },

  // 프록시 타임아웃 설정 (ms) - LLM 응답 대기 고려
  experimental: {
    proxyTimeout: 60000, // 60초
  },
};

export default nextConfig;
