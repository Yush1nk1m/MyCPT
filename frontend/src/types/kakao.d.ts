export {};

declare global {
  interface Window {
    Kakao?: {
      init: (key: string) => void;
      isInitialized: () => boolean;
      Share: {
        sendDefault: (opts: Record<string, unknown>) => void;
      };
    };
  }
}
