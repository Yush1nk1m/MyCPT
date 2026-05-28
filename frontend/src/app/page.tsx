export default function Home() {
  return (
    <div className="min-h-screen bg-paper p-6 flex flex-col gap-4">
      {/* 색상 토큰 확인 */}
      <div className="bg-ink text-paper p-4 rounded-lg">
        bg-ink / text-paper / rounded-lg
      </div>
      <div className="bg-accent text-paper p-4 rounded-lg">bg-accent</div>
      <div className="bg-member text-paper p-4 rounded-lg">bg-member</div>
      <div className="bg-danger text-paper p-4 rounded-lg">bg-danger</div>

      {/* 텍스트 크기 확인 */}
      <div className="text-3xl text-ink">text-3xl (28px)</div>
      <div className="text-base text-ink">text-base (13px)</div>
      <div className="text-xs text-ink-faint">text-xs (10.5px)</div>

      {/* 그림자·반경 확인 */}
      <div className="bg-paper-2 p-4 rounded-xl shadow-pop">
        rounded-xl / shadow-pop
      </div>

      <div className="font-mono text-base text-ink p-4 bg-paper-2 rounded">
        font-mono: JetBrains Mono 확인용 0Oo 1Il
      </div>
    </div>
  );
}
