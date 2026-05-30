import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [react(), tsconfigPaths()],
  test: {
    environment: "jsdom", // DOM API 필요한 컴포넌트 테스트용
    globals: true, // describe/it/expect를 import 없이 사용
    setupFiles: ["./vitest.setup.ts"],
    coverage: {
      provider: "v8",
      include: ["src/lib/**", "src/stores/**", "src/hooks/**"],
      exclude: ["src/**/__tests__/**"],
    },
  },
});
