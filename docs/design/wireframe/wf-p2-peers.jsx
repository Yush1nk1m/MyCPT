// P2 — 동료 & 케미 (3화면: 동료 목록 / 동료 상세 / 등록 모달)

// ─────────────────────────────────────────────────────────
// Shared primitives
// ─────────────────────────────────────────────────────────

const StatusBar = () =>
<div className="wf-status">
    <span>9:41</span>
    <span>● ● ● ● ●  100%</span>
  </div>;


const CoinPill = ({ count = 2, refillIn = '23:14' }) =>
<div className="coin-pill">
    <span className="left">⏱ {count >= 3 ? '00:00' : refillIn}</span>
    <span className="right"><span className="icon">⊙</span><span>{count}</span></span>
  </div>;


const HeaderMember = () =>
<div className="wf-header">
    <div className="logo">MyCPT</div>
    <div className="right" style={{ gap: 6 }}>
      <CoinPill />
      <div className="wf-bell">🔔<span className="dot"></span></div>
      <div className="wf-chip">
        <span>닉네임</span>
        <span className="avatar">사진</span>
      </div>
    </div>
  </div>;


const TabBar = ({ active = 2 }) => {
  const tabs = [
  { i: '⌂', label: '홈' },
  { i: '☰', label: '검사 결과' },
  { i: '⇄', label: '동료 & 케미' },
  { i: '◉', label: '내 정보' }];

  return (
    <div className="wf-tabs">
      {tabs.map((t, idx) =>
      <div key={idx} className={"tab" + (idx === active ? " active" : "")}>
          <span className="icon">{t.i}</span>
          <span>{t.label}</span>
        </div>
      )}
    </div>);

};

const WfFrame = ({ children, screenLabel, activeTab = 2 }) =>
<div className="wf" data-screen-label={screenLabel}>
    <StatusBar />
    <HeaderMember />
    <div className="wf-body">{children}</div>
    <TabBar active={activeTab} />
  </div>;


const DISC = {
  D: { name: '주도', color: 'var(--disc-d)' },
  I: { name: '사교', color: 'var(--disc-i)' },
  S: { name: '안정', color: 'var(--disc-s)' },
  C: { name: '신중', color: 'var(--disc-c)' }
};

const TypePill = ({ type, mini = false }) =>
<span className={"type-pill type-" + type.toLowerCase()} style={mini ? { fontSize: 10, padding: '2px 7px' } : null}>
    <span className="dot"></span>
    <span>{type} · {DISC[type].name}형</span>
  </span>;


const Avatar = ({ name, size = 40, type }) =>
<div className="avatar-circle" style={{
  width: size, height: size, fontSize: size * 0.4,
  background: type ? 'oklch(0.97 0.03 80)' : 'var(--paper-3)'
}}>
    {name.slice(0, 1)}
  </div>;


// ─────────────────────────────────────────────────────────
// Mock data
// ─────────────────────────────────────────────────────────

const COLLEAGUES = [
{ id: 1, name: '민지', type: 'I', last: '2026.05.18', reports: 2 },
{ id: 2, name: '도윤', type: 'C', last: '2026.04.30', reports: 1 },
{ id: 3, name: '서연', type: 'D', last: null, reports: 0 },
{ id: 4, name: '하준', type: 'S', last: '2026.03.12', reports: 3 },
{ id: 5, name: '지우', type: null, last: null, reports: 0 } // 아직 검사 안 한 동료
];

// ─────────────────────────────────────────────────────────
// 1. 동료 목록 (/colleagues)
// ─────────────────────────────────────────────────────────

const PeerList = () =>
<WfFrame screenLabel="P2 동료 목록 (동료 & 케미 첫 탭)">
    <div className="page-tabs">
      <div className="pt active">동료 목록</div>
      <div className="pt">보고서 목록</div>
    </div>

    {/* My peer code card */}
    <div style={{ padding: '14px 14px 0' }}>
      <div style={{
      background: 'white',
      border: '1.5px solid var(--ink)',
      borderRadius: 14,
      padding: '14px 16px',
      display: 'flex',
      flexDirection: 'column',
      gap: 10
    }}>
        <div style={{
        display: 'flex', alignItems: 'baseline', justifyContent: 'space-between'
      }}>
          <span style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.06em', textTransform: 'uppercase' }}>내 동료 코드</span>
          <span style={{ fontSize: 10.5, color: 'var(--accent)', fontWeight: 600 }}>↻ 새 코드</span>
        </div>
        <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        background: 'var(--paper-2)',
        border: '1px dashed var(--line)',
        borderRadius: 10,
        padding: '10px 14px',
        fontFamily: "'JetBrains Mono', monospace",
        fontSize: 15, fontWeight: 700,
        letterSpacing: '0.08em'
      }}>
          <span>MYCPT-7K3F-29Q</span>
          <span style={{ fontFamily: 'Inter', fontSize: 12, fontWeight: 600, color: 'var(--accent)' }}>복사</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          <div className="btn-kakao" style={{ fontSize: 12, padding: '10px 0' }}>💬 카카오톡 공유</div>
          <div className="btn-secondary" style={{ fontSize: 12, padding: '9px 0' }}>+ 친구 코드 입력</div>
        </div>
      </div>
    </div>

    {/* Peers section */}
    <div className="section-label">
      내 동료 · {COLLEAGUES.length}명
    </div>
    <div style={{ padding: '0 14px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
      {COLLEAGUES.map((p) =>
    <div key={p.id} style={{
      background: 'white',
      border: '1.5px solid var(--ink)',
      borderRadius: 12,
      padding: '12px 14px',
      display: 'grid',
      gridTemplateColumns: 'auto 1fr auto',
      gap: 12,
      alignItems: 'center'
    }}>
          <Avatar name={p.name} type={p.type} />
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4, minWidth: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <span style={{ fontSize: 13.5, fontWeight: 700 }}>{p.name}</span>
              {p.type ? <TypePill type={p.type} mini /> :
          <span style={{
            fontSize: 10, color: 'var(--ink-faint)',
            border: '1px dashed var(--line)',
            borderRadius: 999, padding: '2px 7px'
          }}>검사 전</span>
          }
            </div>
            <div style={{ fontSize: 10.5, color: 'var(--ink-soft)' }}>
              {p.reports > 0 ?
          <>케미 보고서 {p.reports}회 · 최근 {p.last}</> :

          <span style={{ color: 'var(--ink-faint)', fontStyle: 'italic' }}>아직 케미 보고서가 없어요</span>
          }
            </div>
          </div>
          <span style={{ color: 'var(--ink-faint)', fontSize: 16 }}>›</span>
        </div>
    )}
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 2. 동료 상세 (/colleagues/[id])
// ─────────────────────────────────────────────────────────

const PEER_REPORTS = [
{ id: 1, date: '2026.05.18', summary: '도전적 추진 ↔ 사교 균형' },
{ id: 2, date: '2026.04.22', summary: '협력 중심 의사소통' }];


const PeerDetail = () =>
<WfFrame screenLabel="P2 동료 상세">
    {/* back bar */}
    <div className="back-bar">
      <div className="back">‹</div>
      <div className="meta">
        <div className="title">민지</div>
        <div className="sub">동료 · 등록 2026.02.10</div>
      </div>
      <div className="icon-btn">⋯</div>
    </div>

    {/* Profile hero */}
    <div style={{
    background: 'white',
    padding: '18px 18px 20px',
    borderBottom: '1px solid var(--line)',
    display: 'flex', alignItems: 'center', gap: 16
  }}>
      <Avatar name="민지" size={72} type="I" />
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 18, fontWeight: 800 }}>민지</div>
        <div>
          <TypePill type="I" />
        </div>
        <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>
          최근 검사 2026.05.18
        </div>
      </div>
    </div>

    {/* CTA — 케미 발행 */}
    <div style={{
    padding: '14px',
    background: 'var(--paper-2)',
    borderBottom: '1px solid var(--line)'
  }}>
      <div style={{
      background: 'white',
      border: '1.5px solid var(--ink)',
      borderRadius: 14,
      padding: '14px 16px',
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 10 }}>
          <span style={{ fontSize: 22 }}>🤝</span>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 700 }}>우리 잘 맞을까?</div>
            <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', marginTop: 2, lineHeight: 1.5 }}>민지님과의 케미 보고서를 받아볼 수 있어요.
코인 1개를 사용해요.
          </div>
          </div>
        </div>
        <div className="btn-primary">케미 보고서 발행 (코인 1)</div>
      </div>
    </div>

    {/* Past reports */}
    <div className="section-label">
      이전 케미 보고서 · {PEER_REPORTS.length}회
    </div>
    <div style={{ padding: '0 14px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
      {PEER_REPORTS.map((r) =>
    <div key={r.id} style={{
      background: 'white',
      border: '1.2px solid var(--line)',
      borderRadius: 10,
      padding: '12px 14px',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between'
    }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            <span style={{ fontSize: 13, fontWeight: 700 }}>{r.summary}</span>
            <span style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>{r.date}</span>
          </div>
          <span style={{ color: 'var(--ink-faint)', fontSize: 16 }}>›</span>
        </div>
    )}
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 3. 동료 등록 모달 (bottom sheet)
// ─────────────────────────────────────────────────────────

const PeerRegisterModal = () =>
<div className="wf" style={{ background: 'var(--paper-2)' }}>
    <StatusBar />
    {/* dimmed peek of peer list */}
    <div style={{
    height: 22, background: 'oklch(0.55 0.01 250 / 0.4)',
    borderBottom: '1px solid var(--line)', fontSize: 9,
    fontFamily: "'JetBrains Mono', monospace", color: 'white',
    display: 'flex', alignItems: 'center', paddingLeft: 14
  }}>← 동료 목록 (dimmed)</div>

    {/* sheet — bottom-anchored */}
    <div style={{ flex: 1, position: 'relative' }}>
      <div style={{
      position: 'absolute',
      left: 0, right: 0, bottom: 0,
      background: 'white',
      borderTopLeftRadius: 22, borderTopRightRadius: 22,
      display: 'flex', flexDirection: 'column',
      boxShadow: '0 -4px 20px oklch(0.2 0.01 250 / 0.15)',
      maxHeight: '92%'
    }}>
        <div className="sheet-handle"></div>
        <div className="sheet-head">
          <div className="close">✕</div>
          <div className="title">동료 등록</div>
          <div style={{ width: 28 }} />
        </div>

        <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Section 1: 친구 코드 입력 */}
          <div>
            <div style={{ fontSize: 12, color: 'var(--ink-soft)', marginBottom: 6, fontWeight: 600 }}>
              ① 친구가 알려준 코드 입력
            </div>
            <div style={{
            border: '1.5px solid var(--ink)',
            borderRadius: 10,
            padding: '12px 14px',
            fontSize: 16, fontWeight: 700,
            fontFamily: "'JetBrains Mono', monospace",
            letterSpacing: '0.1em',
            background: 'white',
            color: 'var(--ink)',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between'
          }}>
              <span style={{ color: 'var(--ink-faint)' }}>MYCPT-□□□□-□□□</span>
              <span style={{ fontFamily: 'Inter', fontSize: 10, color: 'var(--ink-faint)', fontWeight: 500, letterSpacing: 0 }}>붙여넣기</span>
            </div>
            <div style={{
            marginTop: 6, fontSize: 11, color: 'var(--ink-soft)', lineHeight: 1.5
          }}>
              친구의 동료 코드를 입력하면 양방향으로 등록돼요.
            </div>
          </div>

          {/* OR separator */}
          <div style={{
          display: 'flex', alignItems: 'center', gap: 10,
          color: 'var(--ink-faint)', fontSize: 11,
          fontFamily: "'JetBrains Mono', monospace"
        }}>
            <div style={{ flex: 1, borderTop: '1px dashed var(--line)' }}></div>
            <span>또는</span>
            <div style={{ flex: 1, borderTop: '1px dashed var(--line)' }}></div>
          </div>

          {/* Section 2: 내 코드 공유 */}
          <div>
            <div style={{ fontSize: 12, color: 'var(--ink-soft)', marginBottom: 6, fontWeight: 600 }}>
              ② 내 코드로 친구를 초대하기
            </div>
            <div style={{
            background: 'var(--paper-2)',
            border: '1px dashed var(--line)',
            borderRadius: 10,
            padding: '10px 14px',
            fontFamily: "'JetBrains Mono', monospace",
            fontSize: 14, fontWeight: 700,
            letterSpacing: '0.08em',
            textAlign: 'center',
            marginBottom: 8
          }}>MYCPT-7K3F-29Q</div>
            <div className="btn-kakao">💬 카카오톡으로 코드 보내기</div>
          </div>

          <div style={{ height: 4 }} />
          <div className="btn-primary">코드로 등록</div>
        </div>
      </div>
    </div>

    <TabBar active={2} />
  </div>;


// ─────────────────────────────────────────────────────────
// 4. 케미 발행 확인 모달 (M4) — 동료 상세 → 발행 CTA
// ─────────────────────────────────────────────────────────

const ChemistryConfirmModal = () =>
<div className="wf" data-screen-label="P2 케미 발행 확인 모달">
    <StatusBar />
    <HeaderMember />

    <div className="wf-body" style={{ position: 'relative', overflow: 'hidden' }}>
      {/* dimmed peer detail behind */}
      <div style={{ opacity: 0.35, padding: 14 }}>
        <div style={{ background: 'white', borderRadius: 12, padding: 14, marginBottom: 10, display: 'flex', alignItems: 'center', gap: 12 }}>
          <Avatar name="민지" size={56} type="I" />
          <div>
            <div style={{ fontSize: 16, fontWeight: 800 }}>민지</div>
            <TypePill type="I" />
          </div>
        </div>
        <div style={{ background: 'white', borderRadius: 12, padding: 14, fontSize: 12 }}>케미 발행</div>
      </div>

      <div className="scrim">
        <div className="dialog-card" style={{ width: 296 }}>
          <div className="icon">🤝</div>
          <h3>민지님과의 케미 보고서를 발행할까요?</h3>
          <p>두 사람의 최근 DISC 결과로 보고서가 만들어져요.<br />발행에 <b style={{ color: 'var(--ink)' }}>약 30초~1분</b> 정도 걸려요.</p>

          {/* coin breakdown */}
          <div style={{
          background: 'var(--paper-2)', border: '1px dashed var(--line)',
          borderRadius: 10, padding: '10px 12px', marginBottom: 14,
          display: 'grid', gridTemplateColumns: '1fr auto', gap: 4,
          fontSize: 11.5
        }}>
            <span style={{ color: 'var(--ink-soft)' }}>사용</span>
            <span style={{ color: 'var(--accent)', fontWeight: 700, fontFamily: "'JetBrains Mono', monospace" }}>− 1 ⊙</span>
            <span style={{ color: 'var(--ink-soft)' }}>발행 후 잔량</span>
            <span style={{ color: 'var(--ink)', fontWeight: 700, fontFamily: "'JetBrains Mono', monospace" }}>1 / 3</span>
            <span style={{ color: 'var(--ink-soft)' }}>다음 충전까지</span>
            <span style={{ color: 'var(--ink)', fontFamily: "'JetBrains Mono', monospace" }}>23 : 14</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div className="btn-primary">발행하기 (코인 1)</div>
            <div className="btn-secondary">취소</div>
          </div>
        </div>
      </div>
    </div>

    <TabBar active={2} />
  </div>;


// ─────────────────────────────────────────────────────────
// Canvas
// ─────────────────────────────────────────────────────────

const ABW = 380,ABH = 990;

const Caption = ({ id, name, sub }) =>
<div className="variant-cap">
    <span className="id">{id}</span>
    <span className="name">{name}</span> — {sub}
  </div>;


const Wrap = ({ id, name, sub, note, children }) =>
<div style={{
  padding: 16, width: ABW, height: ABH,
  display: 'flex', flexDirection: 'column', alignItems: 'stretch',
  background: 'white'
}}>
    <Caption id={id} name={name} sub={sub} />
    <div style={{ display: 'flex', justifyContent: 'center' }}>{children}</div>
    <div className="variant-note">{note}</div>
  </div>;


const App = () =>
<DesignCanvas>
    <DCSection id="peers" title="동료 & 케미 — 3화면 흐름" subtitle="P2 · 동료 목록 → 동료 상세 → 등록 모달">
      <DCArtboard id="list" label="① 동료 목록" width={ABW} height={ABH}>
        <Wrap id="·" name="동료 목록" sub="/colleagues — 동료 & 케미 첫 탭"
      note={<><span className="label">탭 구조</span>"동료 & 케미" 메뉴 = 두 탭(<b>동료 목록 / 보고서 목록</b>)이 같은 페이지를 공유. 이 화면은 <b>동료 목록 active</b>. 상단 <b>내 동료 코드 카드</b>(복사 / 카톡 공유 / 친구 코드 입력)와 <b>동료 카드 리스트</b>. 각 카드에 DISC 유형 칩 + 케미 보고서 횟수 + 최근 발행 일자. 검사 전 동료는 "검사 전" 회색 태그.</>}>
          <PeerList />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="detail" label="② 동료 상세" width={ABW} height={ABH}>
        <Wrap id="·" name="동료 상세" sub="/colleagues/[id]"
      note={<><span className="label">구조</span>상단 프로필 hero(아바타 + 이름 + DISC 유형). 그 아래 <b>"우리 잘 맞을까?" 케미 발행 카드</b>(코인 1 사용 안내). 하단에 이전 케미 보고서 리스트. 우상단 ⋯은 동료 삭제 / 신고.</>}>
          <PeerDetail />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="register" label="③ 동료 등록 모달" width={ABW} height={ABH}>
        <Wrap id="·" name="동료 등록 모달" sub="bottom sheet"
      note={<><span className="label">패턴</span>바닥에서 올라오는 <b>bottom sheet</b>. 두 경로를 한 화면에 — <b>① 친구가 알려준 코드 입력</b>(붙여넣기 지원) + <b>② 내 코드로 친구 초대</b>(카카오톡 공유). "또는" 구분선으로 두 흐름의 동등함을 시각화.</>}>
          <PeerRegisterModal />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="chem-confirm" label="④ 케미 발행 확인 모달" width={ABW} height={ABH}>
        <Wrap id="·" name="케미 발행 확인 (M4)" sub="동료 상세 → 발행 CTA 후속"
      note={<><span className="label">코인 차감 안내</span>발행 전 <b>중앙 다이얼로그</b>로 확인. 코인 안내를 <b>사용·잔량·다음 충전까지</b>의 3행 표로 시각화 — 잔량 부족 시 충전 시간을 함께 보여줘 사용자가 발행 시기를 판단하도록. 발행은 비동기라 약 30초~1분 안내도 포함. 확인 → 케미 보고서 이력에 "발행 중…" 카드로 즉시 표시.</>}>
          <ChemistryConfirmModal />
        </Wrap>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>;


ReactDOM.createRoot(document.getElementById('root')).render(<App />);