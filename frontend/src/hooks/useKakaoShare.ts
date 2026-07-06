"use client";

import { useToast } from "@/hooks/useToast";

interface KakaoShareOptions {
  title: string;
  description: string;
  imageUrl: string;
  url: string;
  buttonTitle: string;
}

/**
 * Kakao.Share.sendDefault 공통 래퍼.
 * SDK 미초기화 시 토스트로 안내하고, 초기화됐으면 feed 타입 메시지를 전송한다.
 * 컨텐츠(title/description/url 등)는 호출부마다 다르므로 옵션으로 받는다.
 */
export function useKakaoShare() {
  const showToast = useToast();

  function share({
    title,
    description,
    imageUrl,
    url,
    buttonTitle,
  }: KakaoShareOptions) {
    if (!window.Kakao?.isInitialized()) {
      showToast("카카오 공유 준비 중이에요. 링크 복사를 이용해 주세요.");
      return;
    }
    window.Kakao.Share.sendDefault({
      objectType: "feed",
      content: {
        title,
        description,
        imageUrl,
        link: { mobileWebUrl: url, webUrl: url },
      },
      buttons: [
        { title: buttonTitle, link: { mobileWebUrl: url, webUrl: url } },
      ],
    });
  }

  return { share };
}
