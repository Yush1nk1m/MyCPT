"use client";

import Script from "next/script";

export function KakaoSdkLoader() {
  return (
    <Script
      src="https://t1.kakaocdn.net/kakao_js_sdk/2.7.4/kakao.min.js"
      crossOrigin="anonymous"
      onLoad={() => {
        if (window.Kakao && !window.Kakao.isInitialized()) {
          window.Kakao.init(process.env.NEXT_PUBLIC_KAKAO_JS_KEY || "");
        }
      }}
    />
  );
}
