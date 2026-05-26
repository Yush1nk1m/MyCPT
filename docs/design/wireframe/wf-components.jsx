// Shared wireframe primitives — mobile frame, header, tabs.
// Loaded as Babel; nothing here is exported via modules — globals on window.

const StatusBar = ({ time = "9:41" }) => (
  <div className="wf-status">
    <span>{time}</span>
    <span>● ● ● ● ●  100%</span>
  </div>
);

const HeaderGuest = () => (
  <div className="wf-header">
    <div className="logo">MyCPT</div>
    <div className="right">
      <div style={{
        border: '1.2px solid var(--ink)',
        padding: '5px 12px',
        borderRadius: 999,
        fontSize: 11,
        fontWeight: 700,
        background: '#FEE500',
        color: '#191919',
      }}>💬 카카오로 시작</div>
    </div>
  </div>
);

const CoinPill = ({ count = 2, refillIn = '23:14' }) => (
  <div style={{
    display: 'inline-flex', alignItems: 'center',
    border: '1.2px solid var(--ink)', borderRadius: 999,
    background: 'white', fontSize: 10.5, fontWeight: 600,
    overflow: 'hidden', lineHeight: 1,
  }}>
    <span style={{
      padding: '4px 8px',
      fontFamily: "'JetBrains Mono', monospace",
      color: count >= 3 ? 'var(--ink-faint)' : 'var(--ink-soft)',
      borderRight: '1px solid var(--line)',
      background: 'var(--paper-2)',
    }}>⏱ {count >= 3 ? '00:00' : refillIn}</span>
    <span style={{ padding: '4px 8px', display: 'flex', alignItems: 'center', gap: 3 }}>
      <span style={{ color: 'var(--accent)' }}>⊙</span>
      <span>{count}</span>
    </span>
  </div>
);

const HeaderMember = ({ coins = 2, refillIn = '23:14' }) => (
  <div className="wf-header">
    <div className="logo">MyCPT</div>
    <div className="right" style={{ gap: 6 }}>
      <CoinPill count={coins} refillIn={refillIn} />
      <div className="wf-bell">🔔<span className="dot"></span></div>
      <div className="wf-chip">
        <span>닉네임</span>
        <span className="avatar">사진</span>
      </div>
    </div>
  </div>
);

const TabBar = ({ active = 0, lockedExcept = null }) => {
  const tabs = [
    { i: '⌂', label: '홈' },
    { i: '☰', label: '검사 결과' },
    { i: '⇄', label: '동료 & 케미' },
    { i: '◉', label: '내 정보' },
  ];
  return (
    <div className="wf-tabs">
      {tabs.map((t, idx) => {
        const locked = lockedExcept !== null && lockedExcept !== idx && idx !== 0;
        return (
          <div
            key={idx}
            className={"tab" + (idx === active ? " active" : "")}
            style={locked ? { opacity: 0.5 } : null}
          >
            <span className="icon">{locked ? '🔒' : t.i}</span>
            <span style={{ whiteSpace: 'nowrap' }}>{t.label}</span>
          </div>
        );
      })}
    </div>
  );
};

// Wireframe shell — supplies status + header + tabs around body.
const WfFrame = ({ member = true, activeTab = 0, lockedExcept = null, hideTabs = false, children }) => (
  <div className="wf">
    <StatusBar />
    {member ? <HeaderMember /> : <HeaderGuest />}
    <div className="wf-body">{children}</div>
    {!hideTabs && <TabBar active={activeTab} lockedExcept={lockedExcept} />}
  </div>
);

// Fullscreen sheet shell — sits on top of a faded main, with handle + close.
const SheetFrame = ({ title, step, totalSteps, children, closeable = true, kind = 'test' }) => (
  <div className="wf" style={{ background: 'var(--paper-2)' }}>
    <StatusBar />
    {/* dimmed peek of main */}
    <div style={{
      height: 22,
      background: 'oklch(0.55 0.01 250 / 0.4)',
      borderBottom: '1px solid var(--line)',
      fontSize: 9,
      fontFamily: "'JetBrains Mono', monospace",
      color: 'white',
      display: 'flex',
      alignItems: 'center',
      paddingLeft: 14,
    }}>← 메인 (dimmed)</div>

    <div style={{
      flex: 1,
      background: 'white',
      borderTopLeftRadius: 18,
      borderTopRightRadius: 18,
      marginTop: -10,
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
    }}>
      <div className="sheet-handle"></div>
      <div className="sheet-head">
        {closeable ? (
          <div className="close">✕</div>
        ) : (
          <div className="close" style={{ background: 'var(--paper-2)', color: 'var(--ink-faint)' }}>‹</div>
        )}
        <div className="title">{title}</div>
        <div className="stepdot">{step && `Step ${step}/${totalSteps || 3}`}</div>
      </div>
      <div style={{ flex: 1, overflow: 'hidden', padding: '14px 16px 16px', display: 'flex', flexDirection: 'column' }}>
        {children}
      </div>
    </div>
  </div>
);

const VariantCard = ({ variantId, title, name, notes, children, height = 720 }) => (
  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'stretch', width: 360 }}>
    <div className="variant-cap">
      <span className="id">{variantId}</span>
      <span className="name">{name}</span> — {title}
    </div>
    {children}
    <div className="variant-note">{notes}</div>
  </div>
);

Object.assign(window, {
  StatusBar, HeaderGuest, HeaderMember, CoinPill, TabBar,
  WfFrame, SheetFrame, VariantCard,
});
