// P6 — 전역 컴포넌트 카탈로그
// 다른 페이지에 종속되지 않는 글로벌 UI: 헤더 알림 드롭다운 + 토스트 모음
// (검사 시트 Step 1/3/닫기 → P1, 케미 발행 확인 → P2, 추이/탈퇴 → P4 로 이관됨)

// ─────────────────────────────────────────────────────────
// Shared primitives
// ─────────────────────────────────────────────────────────

const StatusBar = () => (
  <div className="wf-status">
    <span>9:41</span>
    <span>● ● ● ● ●  100%</span>
  </div>
);

const CoinPill = ({ count = 2, refillIn = '23:14' }) => (
  <div className="coin-pill">
    <span className="left">⏱ {count >= 3 ? '00:00' : refillIn}</span>
    <span className="right"><span className="icon">⊙</span><span>{count}</span></span>
  </div>
);

const HeaderMember = ({ bellOpen = false }) => (
  <div className="wf-header">
    <div className="logo">MyCPT</div>
    <div className="right" style={{ gap: 6 }}>
      <CoinPill />
      <div className="wf-bell" style={bellOpen ? { background: 'var(--ink)', color: 'white', borderColor: 'var(--ink)' } : null}>
        🔔{!bellOpen && <span className="dot"></span>}
      </div>
      <div className="wf-chip">
        <span>닉네임</span>
        <span className="avatar">사진</span>
      </div>
    </div>
  </div>
);

const TabBar = ({ active = 0 }) => {
  const tabs = [
    { i: '⌂', label: '홈' },
    { i: '☰', label: '검사 결과' },
    { i: '⇄', label: '동료 & 케미' },
    { i: '◉', label: '내 정보' },
  ];
  return (
    <div className="wf-tabs">
      {tabs.map((t, idx) => (
        <div key={idx} className={"tab" + (idx === active ? " active" : "")}>
          <span className="icon">{t.i}</span>
          <span>{t.label}</span>
        </div>
      ))}
    </div>
  );
};

// ─────────────────────────────────────────────────────────
// 1. 헤더 알림 드롭다운 (M6)
// ─────────────────────────────────────────────────────────

const HEADER_NOTIFS = [
  { ico: '🤝', t: <><b>민지</b>님과의 케미 보고서가 도착했어요</>, w: '방금', unread: true },
  { ico: '👥', t: <><b>도윤</b>님이 동료로 등록을 요청했어요</>, w: '2시간 전', unread: true },
  { ico: '🪞', t: <><b>여자친구</b>님이 보낸 평정 결과가 도착했어요</>, w: '5시간 전', unread: true },
  { ico: '⊙', t: <>코인 1개가 충전됐어요</>, w: '어제', unread: false },
];

const NotifDropdown = () => (
  <div className="wf" data-screen-label="P6 · 헤더 알림 드롭다운">
    <StatusBar />
    <HeaderMember bellOpen={true} />

    <div className="wf-body" style={{ background: 'oklch(0.2 0.01 250 / 0.25)', position: 'relative' }}>
      <div style={{
        position: 'absolute',
        right: 60, top: 6,
        width: 290,
        background: 'white',
        border: '1.5px solid var(--ink)',
        borderRadius: 14,
        overflow: 'hidden',
        boxShadow: '0 8px 24px oklch(0.2 0.01 250 / 0.25)',
      }}>
        {/* tip pointer */}
        <div style={{
          position: 'absolute', right: 18, top: -7,
          width: 12, height: 12, background: 'white',
          borderTop: '1.5px solid var(--ink)', borderLeft: '1.5px solid var(--ink)',
          transform: 'rotate(45deg)',
        }}></div>

        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '12px 14px', background: 'var(--paper-2)',
          borderBottom: '1px solid var(--line)',
        }}>
          <div style={{ fontSize: 12.5, fontWeight: 700 }}>알림</div>
          <div style={{ fontSize: 10, color: 'var(--ink-soft)', textDecoration: 'underline' }}>모두 읽음</div>
        </div>

        {HEADER_NOTIFS.map((n, i) => (
          <div key={i} className={"notif-row" + (n.unread ? " unread" : "")}>
            <div className="ico">{n.ico}</div>
            <div className="text">
              <div style={{ fontWeight: n.unread ? 600 : 500 }}>{n.t}</div>
              <div className="when">{n.w}</div>
            </div>
            {n.unread && <span style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--accent)', marginTop: 6 }}></span>}
          </div>
        ))}

        <div style={{
          padding: '10px 14px', textAlign: 'center',
          fontSize: 11, color: 'var(--accent)', fontWeight: 700,
          borderTop: '1px solid var(--line)', background: 'var(--paper-2)',
        }}>전체 알림 보기 ›</div>
      </div>
    </div>

    <TabBar active={0} />
  </div>
);

// ─────────────────────────────────────────────────────────
// 2. 토스트 모음
// ─────────────────────────────────────────────────────────

const ToastBoard = () => (
  <div className="wf" data-screen-label="P6 · 토스트 모음">
    <StatusBar />
    <HeaderMember />
    <div className="wf-body" style={{
      padding: 16, display: 'flex', flexDirection: 'column', gap: 14,
      background: 'oklch(0.96 0.008 80)',
    }}>
      <div style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.06em', textTransform: 'uppercase' }}>System feedback variants</div>

      <div style={{ background: 'white', border: '1px solid var(--line-soft)', borderRadius: 10, padding: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 10 }}>① 비회원 잠금 — 회원 전용 CTA·탭 탭 시</div>
        <div className="toast">
          <span className="ico">🔒</span>
          <span>로그인이 필요해요</span>
          <span className="action" style={{ background: '#FEE500', color: '#191919' }}>카카오로 시작</span>
        </div>
      </div>

      <div style={{ background: 'white', border: '1px solid var(--line-soft)', borderRadius: 10, padding: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 10 }}>② 케미 발행 시작 — 발행 직후 1회</div>
        <div className="toast">
          <span className="ico">⏳</span>
          <span>케미 보고서를 만들고 있어요…</span>
        </div>

        <div style={{ height: 8 }} />
        <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 10 }}>③ 케미 완료 — SSE 도착 시</div>
        <div className="toast success">
          <span className="ico">🤝</span>
          <span><b style={{ color: 'white' }}>민지</b>님과의 케미 보고서가 도착했어요</span>
          <span className="action">보기</span>
        </div>
      </div>

      <div style={{ background: 'white', border: '1px solid var(--line-soft)', borderRadius: 10, padding: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 10 }}>④ 클립보드 복사</div>
        <div className="toast">
          <span className="ico">✓</span>
          <span>코드를 복사했어요</span>
        </div>

        <div style={{ height: 8 }} />
        <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 10 }}>⑤ 에러 / 네트워크</div>
        <div className="toast" style={{ background: 'var(--danger)' }}>
          <span className="ico">⚠</span>
          <span>잠시 후 다시 시도해 주세요</span>
          <span className="action" style={{ background: 'white', color: 'var(--danger)' }}>재시도</span>
        </div>

        <div style={{ height: 8 }} />
        <div style={{ fontSize: 12, fontWeight: 700, marginBottom: 10 }}>⑥ 평정 결과 도착</div>
        <div className="toast success">
          <span className="ico">🪞</span>
          <span><b style={{ color: 'white' }}>여자친구</b>님의 평정 결과가 도착했어요</span>
          <span className="action">보기</span>
        </div>
      </div>

      <div style={{
        background: 'var(--paper-2)', border: '1px dashed var(--line)',
        borderRadius: 10, padding: '10px 12px',
        fontSize: 10.5, color: 'var(--ink-soft)', lineHeight: 1.6,
      }}>
        모든 토스트는 <b style={{ color: 'var(--ink)' }}>하단 안전 영역 위</b> (탭바 위쪽 ~80px)에 약 3초 노출. 액션 버튼이 있는 토스트는 5초 노출 + 사용자 액션 대기.
      </div>
    </div>
    <TabBar active={0} />
  </div>
);

// ─────────────────────────────────────────────────────────
// Canvas
// ─────────────────────────────────────────────────────────

const ABW = 380, ABH = 990;

const Caption = ({ id, name, sub }) => (
  <div className="variant-cap">
    <span className="id">{id}</span>
    <span className="name">{name}</span> — {sub}
  </div>
);

const Wrap = ({ id, name, sub, note, children }) => (
  <div style={{
    padding: 16, width: ABW, height: ABH,
    display: 'flex', flexDirection: 'column', alignItems: 'stretch',
    background: 'white',
  }}>
    <Caption id={id} name={name} sub={sub} />
    <div style={{ display: 'flex', justifyContent: 'center' }}>{children}</div>
    <div className="variant-note">{note}</div>
  </div>
);

const App = () => (
  <DesignCanvas>
    <DCSection id="global" title="전역 컴포넌트 카탈로그" subtitle="P6 · 페이지에 종속되지 않는 글로벌 UI — 다른 페이지에서 호출/오버레이">
      <DCArtboard id="notif-dropdown" label="① 헤더 알림 드롭다운 (M6)" width={ABW} height={ABH}>
        <Wrap id="·" name="헤더 알림 드롭다운" sub="헤더 종 탭 시"
          note={<><span className="label">헤더 앵커</span>종 아이콘 바로 아래 <b>꼬리(▲) 달린 드롭다운</b>으로 등장. 알림 센터(/me/notifications)와 <b>동일 데이터 공유</b>, 다만 최근 4건만 미리보기. 하단 "전체 알림 보기 ›"로 풀페이지 진입. 본문은 scrim으로 dim해 모달 컨텍스트임을 강조.</>}>
          <NotifDropdown />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="toasts" label="② 토스트 변형 6종" width={ABW} height={ABH}>
        <Wrap id="·" name="토스트 모음" sub="잠금 / 케미 / 복사 / 에러 / 평정 도착"
          note={<><span className="label">패턴</span>① 비회원 잠금(검정 + 카카오 액션) ② 케미 발행 시작(검정, 액션 없음, 짧음) ③ 케미 완료(member 색 = 녹, 액션 "보기") ④ 복사 확인 ⑤ 에러(빨강 + 재시도) ⑥ 평정 결과 도착(member 색). <b>위치</b>는 탭바 위 ~80px 안전영역, 약 3초(액션 있으면 5초).</>}>
          <ToastBoard />
        </Wrap>
      </DCArtboard>
    </DCSection>

    <DCSection id="migrated" title="다른 페이지로 이관된 항목" subtitle="P6 초안에 있던 화면들이 소속 페이지로 흡수됨 — 아래 위치에서 확인">
      <DCArtboard id="migrated-list" label="이관 안내" width={ABW} height={ABH}>
        <div style={{
          padding: 24, width: ABW, height: ABH,
          background: 'white', display: 'flex', flexDirection: 'column', gap: 12,
        }}>
          <div className="variant-cap" style={{ marginBottom: 12 }}>
            <span className="id">migration</span>
            <span className="name">이관 맵</span>
          </div>
          {[
            { from: '검사 시트 Step 1 (유형 선택)', to: 'P1 · ②-b 섹션' },
            { from: '검사 시트 Step 3 (비회원)', to: 'P1 · ②-b 섹션' },
            { from: '검사 시트 Step 3 (회원)', to: 'P1 · ②-b 섹션' },
            { from: '검사 시트 응시 중단 다이얼로그', to: 'P1 · ②-b 섹션' },
            { from: '케미 발행 확인 모달 (M4)', to: 'P2 동료 & 케미 · ④' },
            { from: '인사이트 추이 탭', to: 'P4 · ⑤-b' },
            { from: '회원탈퇴 Step 1 사유', to: 'P4 · ⑧' },
            { from: '회원탈퇴 Step 2 확인', to: 'P4 · ⑨' },
          ].map((m, i) => (
            <div key={i} style={{
              display: 'grid', gridTemplateColumns: '1fr auto 1fr', gap: 12, alignItems: 'center',
              background: 'var(--paper-2)', border: '1px solid var(--line)',
              borderRadius: 10, padding: '12px 14px', fontSize: 12,
            }}>
              <span>{m.from}</span>
              <span style={{ color: 'var(--accent)', fontWeight: 700 }}>→</span>
              <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 11, color: 'var(--ink-soft)' }}>{m.to}</span>
            </div>
          ))}

          <div style={{
            marginTop: 8, padding: '12px 14px',
            background: 'oklch(0.97 0.03 95)', border: '1px dashed var(--accent)',
            borderRadius: 10, fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.6,
          }}>
            <b style={{ color: 'var(--ink)' }}>P6는 이제 전역 컴포넌트만 보관</b>합니다. 각 화면의 흐름은 소속 페이지에서 인접하게 배치돼 한눈에 흐름이 보입니다.
          </div>
        </div>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>
);

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
