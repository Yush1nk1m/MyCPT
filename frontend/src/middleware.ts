import { NextRequest, NextResponse } from "next/server";

// 회원 전용 경로 - access-matrix.md 기준
const MEMBER_ROUTES = ["/me", "/results", "/colleagues", "/chemistry"];

export function middleware(req: NextRequest) {
  const token = req.cookies.get("accessToken")?.value;
  const path = req.nextUrl.pathname;

  const isMemberRoute = MEMBER_ROUTES.some((r) => path.startsWith(r));
  if (isMemberRoute && !token) {
    const loginUrl = new URL("/api/v1/auth/kakao", req.url);
    loginUrl.searchParams.set("returnTo", path);
    return NextResponse.redirect(loginUrl);
  }
}

export const config = {
  // _next 정적 자산, favicon, API 경로는 미들웨어 제외
  matcher: ["/((?!_next|favicon.ico|api).*)"],
};
