import { Header } from "@/components/layout/Header";
import { TabBar } from "@/components/layout/TabBar";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-paper">
      <Header />

      {/* 헤더~탭바 사이 고정 영역. flex-col로 스크롤 영역과 푸터를 분리 */}
      <div
        className="flex flex-col"
        style={{
          marginTop: "var(--header-height)",
          marginBottom: "var(--tab-height)",
          height: "calc(100dvh - var(--header-height) - var(--tab-height))",
        }}
      >
        {/* min-h-0가 핵심 — flex 자식은 기본이 min-height:auto라
            내용이 넘쳐도 부모를 밀어내며 커짐. 0으로 잡아야 overflow-y-auto가 실제로 먹힘 */}
        <main className="flex-1 min-h-0 overflow-y-auto">{children}</main>
      </div>

      <TabBar />
    </div>
  );
}
