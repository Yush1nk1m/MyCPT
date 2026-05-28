import type { Metadata } from "next";
import { Inter, JetBrains_Mono } from "next/font/google";
import "./globals.css";

// Inter — 기본 UI 폰트 (한글은 Pretendard로 대체 예정, 지금은 Inter fallback)
const inter = Inter({
  subsets: ["latin"],
  variable: "--font-sans", // globals.css의 --font-sans CSS 변수에 주입
});

// JetBrains Mono — 메타·캡션·코드용
const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-mono", // globals.css의 --font-mono CSS 변수에 주입
});

export const metadata: Metadata = {
  title: "MyCPT",
  description: "DISC 기반 역량 분석 서비스",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    // 두 폰트의 CSS 변수를 html 태그에 등록
    <html lang="ko" className={`${inter.variable} ${jetbrainsMono.variable}`}>
      <body>{children}</body>
    </html>
  );
}
