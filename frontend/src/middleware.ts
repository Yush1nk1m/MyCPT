import { NextRequest, NextResponse } from "next/server";

// 회원 전용 경로 - access-matrix.md 기준
const MEMBER_ROUTES = ["/me", "/results", "/colleagues", "/chemistry"];
// MEMBER_ROUTES 내에 있지만 비회원도 접근 가능한 예외 (access-matrix.md 의사코드 기준)
const PUBLIC_EXCEPTIONS = ["/me/about", "/me/help"];

export function middleware(req: NextRequest) {
  const token = req.cookies.get("accessToken")?.value;
  const path = req.nextUrl.pathname;

  if (PUBLIC_EXCEPTIONS.some((r) => path.startsWith(r))) return;

  const isMemberRoute = MEMBER_ROUTES.some((r) => path.startsWith(r));
  if (isMemberRoute && !token) {
    const loginUrl = new URL("/api/v1/auth/kakao", req.url);
    loginUrl.searchParams.set("returnTo", path);
    return NextResponse.redirect(loginUrl);
  }
}

export const config = {
  matcher: ["/((?!_next|favicon.ico|api).*)"],
};
