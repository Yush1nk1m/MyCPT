import type { NextConfig } from "next";
import * as dotenv from "dotenv";
import path from "path";

const envPath =
  process.env.NODE_ENV === "production" ? ".env.production" : ".env.dev";
dotenv.config({ path: path.resolve(process.cwd(), envPath) });

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
};

export default nextConfig;
