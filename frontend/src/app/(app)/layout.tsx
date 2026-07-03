import { Header } from "@/components/layout/Header";
import { TabBar } from "@/components/layout/TabBar";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-paper">
      <Header />

      {/* 헤더~탭바 사이만 스크롤되는 독립 영역 — 내부 sticky(top:0) 요소가
          뷰포트가 아닌 이 컨테이너 기준으로 붙도록 높이를 명시적으로 제한 */}
      <main
        className="overflow-y-auto"
        style={{
          marginTop: "var(--header-height)",
          marginBottom: "var(--tab-height)",
          height: "calc(100dvh - var(--header-height) - var(--tab-height))",
        }}
      >
        {children}
        <p className="px-4 py-6 text-center text-[10.5px] text-ink-faint">
          본 결과는 참고용 성향 분석이며 심리 진단이 아닙니다.
        </p>
      </main>

      <TabBar />
    </div>
  );
}
