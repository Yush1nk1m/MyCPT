// P3 — 케미 보고서 (3화면: 이력 / 상세 / 동료 초대 수락)

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


const HeaderGuest = () =>
<div className="wf-header">
    <div className="logo">MyCPT</div>
    <div className="right">
      <div style={{
      border: '1.5px solid var(--ink)', padding: '5px 12px', borderRadius: 999,
      fontSize: 11, fontWeight: 700, background: '#FEE500',
      fontFamily: "'JetBrains Mono', monospace"
    }}>💬 카카오로 시작</div>
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

const WfFrame = ({ children, screenLabel, activeTab = 2, header = 'member' }) =>
<div className="wf" data-screen-label={screenLabel}>
    <StatusBar />
    {header === 'guest' ? <HeaderGuest /> : <HeaderMember />}
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
}}>{name.slice(0, 1)}</div>;


// ─────────────────────────────────────────────────────────
// Mock data
// ─────────────────────────────────────────────────────────

const CHEM_REPORTS = [
{ id: 1, peer: '민지', peerType: 'I', myType: 'D', date: '2026.05.18 14:22', status: 'ready', unread: true, summary: '도전적 추진 ↔ 사교 균형' },
{ id: 2, peer: '도윤', peerType: 'C', myType: 'D', date: '2026.05.16 09:11', status: 'generating', unread: false, summary: null },
{ id: 3, peer: '하준', peerType: 'S', myType: 'D', date: '2026.04.30 21:45', status: 'ready', unread: false, summary: '안정적 신뢰 기반의 협업' },
{ id: 4, peer: '민지', peerType: 'I', myType: 'D', date: '2026.04.22 16:08', status: 'ready', unread: false, summary: '협력 중심 의사소통' },
{ id: 5, peer: '서연', peerType: 'D', myType: 'D', date: '2026.03.10 11:30', status: 'ready', unread: false, summary: '주도형끼리의 경쟁과 시너지' }];


// ─────────────────────────────────────────────────────────
// 1. 케미 보고서 이력 (/chemistry)
// ─────────────────────────────────────────────────────────

const ChemistryList = () =>
<WfFrame screenLabel="P3 케미 이력 (보고서 목록 탭)">
    <div className="page-tabs">
      <div className="pt">동료 목록</div>
      <div className="pt active">보고서 목록</div>
    </div>

    {/* List */}
    <div style={{ padding: '12px 14px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
      {CHEM_REPORTS.map((r) => {
      if (r.status === 'generating') {
        return (
          <div key={r.id} style={{
            background: 'white',
            border: '1.5px dashed var(--accent)',
            borderRadius: 12,
            padding: '14px 16px',
            display: 'flex', flexDirection: 'column', gap: 8,
            position: 'relative'
          }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Avatar name="나" size={32} type="D" />
                <span style={{ color: 'var(--ink-faint)', fontSize: 11 }}>↔</span>
                <Avatar name={r.peer} size={32} type={r.peerType} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 13, fontWeight: 700 }}>나 ↔ {r.peer}</div>
                  <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>{r.date}</div>
                </div>
                <span style={{
                fontSize: 10, color: 'var(--accent)', fontWeight: 700,
                border: '1px solid var(--accent)', borderRadius: 999,
                padding: '3px 8px', whiteSpace: 'nowrap'
              }}>발행 중…</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 5, marginTop: 4 }}>
                <div className="skel" style={{ height: 8, width: '90%' }}></div>
                <div className="skel" style={{ height: 8, width: '70%' }}></div>
              </div>
              <div style={{ fontSize: 10.5, color: 'var(--ink-soft)', lineHeight: 1.5, marginTop: 2 }}>
                완료되면 알림으로 알려드릴게요.
              </div>
            </div>);

      }
      return (
        <div key={r.id} style={{
          background: 'white',
          border: '1.5px solid var(--ink)',
          borderRadius: 12,
          padding: '12px 14px',
          display: 'grid',
          gridTemplateColumns: 'auto 1fr auto',
          gap: 12,
          alignItems: 'center',
          position: 'relative'
        }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              <Avatar name="나" size={32} type={r.myType} />
              <Avatar name={r.peer} size={32} type={r.peerType} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <span style={{ fontSize: 13, fontWeight: 700 }}>나 ↔ {r.peer}</span>
                {r.unread && <span className="badge-new">NEW</span>}
              </div>
              <div style={{ fontSize: 11, color: 'var(--ink-soft)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.summary}</div>
              <div style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>{r.date}</div>
            </div>
            <span style={{ color: 'var(--ink-faint)', fontSize: 16 }}>›</span>
          </div>);

    })}
      <div style={{
      textAlign: 'center', padding: '6px 0 0',
      fontSize: 11, color: 'var(--ink-faint)',
      fontFamily: "'JetBrains Mono', monospace"
    }}>· 더 이상의 기록이 없어요 ·</div>
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 2. 케미 보고서 상세 — 발행 완료 (/chemistry/[id])
// ─────────────────────────────────────────────────────────

const ChemistryDetail = ({ generating = false }) =>
<WfFrame screenLabel={`P3 케미 상세 · ${generating ? '발행 중' : '완료'}`}>
    <div className="back-bar">
      <div className="back">‹</div>
      <div className="meta">
        <div className="title">케미 보고서</div>
        <div className="sub">2026.05.18 14:22</div>
      </div>
      <div className="share">↗</div>
    </div>

    {/* Hero — 두 사람 */}
    <div style={{
    background: 'white',
    padding: '22px 18px 24px',
    borderBottom: '1px solid var(--line)',
    display: 'flex', alignItems: 'center', justifyContent: 'space-around',
    gap: 8
  }}>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
        <Avatar name="나" size={72} type="D" />
        <div style={{ fontSize: 13, fontWeight: 700 }}>나</div>
        <TypePill type="D" mini />
      </div>

      <div style={{
      fontFamily: "'Caveat', cursive",
      fontSize: 28, fontWeight: 700,
      color: 'var(--accent)',
      transform: 'translateY(-10px)'
    }}>↔</div>

      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
        <Avatar name="민지" size={72} type="I" />
        <div style={{ fontSize: 13, fontWeight: 700 }}>민지</div>
        <TypePill type="I" mini />
      </div>
    </div>

    {/* Body */}
    {generating ?
  <div style={{ padding: '24px 18px', background: 'var(--paper-2)', display: 'flex', flexDirection: 'column', gap: 14 }}>
        <div style={{
      background: 'white', border: '1.5px dashed var(--accent)',
      borderRadius: 12, padding: '14px 16px',
      display: 'flex', alignItems: 'center', gap: 10
    }}>
          <div style={{
        width: 28, height: 28, borderRadius: '50%',
        border: '2.5px solid var(--accent-2)',
        borderTopColor: 'var(--accent)',
        animation: 'spin 1s linear infinite'
      }}></div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--ink)' }}>케미 보고서를 만들고 있어요</div>
            <div style={{ fontSize: 11, color: 'var(--ink-soft)', marginTop: 2 }}>약 30초~1분 정도 걸려요. 완료되면 알림으로 알려드릴게요.</div>
          </div>
        </div>
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        {/* skeleton blocks */}
        {[80, 60, 90, 70, 85].map((w, i) =>
    <div key={i} className="skel" style={{ height: i % 2 === 0 ? 18 : 12, width: w + '%' }}></div>
    )}
        <div style={{ height: 10 }} />
        {[75, 88, 65].map((w, i) =>
    <div key={i} className="skel" style={{ height: 12, width: w + '%' }}></div>
    )}
      </div> :

  <div className="md">
        <h2>한눈에 보는 케미</h2>
        <p>
          <b>주도형(D)</b>의 추진력과 <b>사교형(I)</b>의 관계 지향이 만나면,
          속도와 분위기가 함께 가는 팀이 됩니다. 다만 결정의 속도와 합의의 깊이 사이에서
          서로의 페이스 차이가 마찰점이 될 수 있어요.
        </p>
        <blockquote>"빠르게 가되, 같이 가자."</blockquote>

        <h2>잘 통하는 지점</h2>
        <ul>
          <li><b>결정과 추진</b> — 나의 결단력 + 민지님의 분위기 메이킹</li>
          <li><b>새로운 시도에 대한 개방성</b> — 두 분 모두 변화에 능동적</li>
          <li><b>외향적 표현력</b> — 회의에서 의견이 적극적으로 오감</li>
        </ul>

        <h2>마찰이 생기기 쉬운 지점</h2>
        <ul>
          <li>나는 <b>결과 중심</b>, 민지님은 <b>관계 중심</b> — 같은 사안을 다르게 우선순위 매김</li>
          <li>나는 신속한 결론, 민지님은 충분한 대화 — 회의 길이 인식 차</li>
        </ul>

        <h2>이렇게 협업해 보세요</h2>
        <ul>
          <li>의사결정 전에 <b>5분 정도 짧은 분위기 토크</b>를 의식적으로 두기</li>
          <li>결론을 내릴 때는 "왜 그렇게 정했는지" 한 줄로 함께 공유</li>
          <li>긴 회의가 필요할 땐 시간을 정해두고 시작하기</li>
        </ul>

        <h2>대화 팁</h2>
        <p>
          민지님께는 <b>"먼저 어떻게 느꼈는지"</b>를 물어보세요. 본인의 결론을 곧장 던지기보다
          상대의 감정을 먼저 확인하면, 신뢰의 회복 탄력성이 큽니다.
        </p>

        <h2>한 줄 요약</h2>
        <p>
          <b>다른 강점이 곱해질 때 시너지가 가장 커지는 조합.</b> 단,
          속도와 분위기 둘 다 챙기는 의식적 노력이 필요합니다.
        </p>

        <hr />
        <p style={{ fontSize: 11, color: 'var(--ink-faint)' }}>
          이 보고서는 참고용 성향 분석이며, 심리·의학적 진단이 아닙니다.
        </p>
      </div>
  }
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 3. 동료 초대 수락 (/invite/[code])
// ─────────────────────────────────────────────────────────

const InviteAccept = () =>
<div className="wf" data-screen-label="P3 동료 초대 수락">
    <StatusBar />
    <HeaderGuest />
    <div className="wf-body" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      {/* Hero */}
      <div style={{
      background: 'white',
      padding: '32px 22px 28px',
      borderBottom: '1px solid var(--line)',
      textAlign: 'center'
    }}>
        <div style={{
        fontFamily: "'Caveat', cursive", fontSize: 22, fontWeight: 700,
        color: 'var(--accent)', marginBottom: 4
      }}>동료 초대장이 도착했어요!</div>
        <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'center', gap: 10, marginTop: 16 }}>
          <Avatar name="민지" size={96} type="I" />
          <div style={{ fontSize: 17, fontWeight: 800 }}>민지</div>
          <TypePill type="I" />
        </div>
        <div style={{
        marginTop: 14,
        fontSize: 13, color: 'var(--ink-soft)', lineHeight: 1.55
      }}>
          <b style={{ color: 'var(--ink)' }}>민지님</b>이 MyCPT에서 동료로 함께하자고 보냈어요.
        </div>
      </div>

      {/* Benefits */}
      <div style={{ padding: '20px 18px', flex: 1, background: 'var(--paper-2)', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.06em', textTransform: 'uppercase' }}>동료가 되면</div>
        {[
      { i: '🤝', t: '서로 케미 보고서를 발행할 수 있어요', s: '두 사람의 성향이 어떻게 어울리는지' },
      { i: '👀', t: '서로의 검사 결과 일부를 공유해요', s: '대표 유형 · 4축 점수' },
      { i: '🔔', t: '검사 갱신 시 알림을 받아요', s: '서로의 변화를 함께 관찰' }].
      map((b, i) =>
      <div key={i} style={{
        background: 'white', border: '1px solid var(--line)',
        borderRadius: 10, padding: '12px 14px',
        display: 'flex', gap: 12, alignItems: 'flex-start'
      }}>
            <span style={{ fontSize: 20 }}>{b.i}</span>
            <div>
              <div style={{ fontSize: 12.5, fontWeight: 700 }}>{b.t}</div>
              <div style={{ fontSize: 10.5, color: 'var(--ink-soft)', marginTop: 2 }}>{b.s}</div>
            </div>
          </div>
      )}
      </div>

      {/* CTA */}
      <div style={{
      background: 'white',
      borderTop: '1px solid var(--line)',
      padding: '14px 18px 18px',
      display: 'flex', flexDirection: 'column', gap: 8
    }}>
        <div className="btn-kakao">💬 카카오로 시작하고 동료 등록</div>
        <div style={{
        textAlign: 'center', fontSize: 10.5, color: 'var(--ink-faint)',
        fontFamily: "'JetBrains Mono', monospace"
      }}>코드 · MYCPT-7K3F-29Q · 24시간 유효</div>
      </div>
    </div>
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
    <DCSection id="chemistry" title="케미 보고서 흐름" subtitle="P3 · 이력 → 상세(발행 중/완료) → 동료 초대 수락">
      <DCArtboard id="list" label="① 케미 이력" width={ABW} height={ABH}>
        <Wrap id="·" name="보고서 목록" sub="/colleagues 의 두 번째 탭"
      note={<><span className="label">탭 구조 변경</span>"동료 & 케미" 메뉴의 두 탭 = <b>동료 목록 / 보고서 목록</b>. <b>발행 중 별도 탭 제거</b> — 같은 목록 안에 <b>점선 테두리 + 스켈레톤 + "발행 중…" 칩</b>의 inline 카드로 표시. SSE 완료 이벤트 도착 시 같은 자리에서 본 카드(실선 + NEW 뱃지)로 자동 교체. NEW 뱃지는 미열람.</>}>
          <ChemistryList />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="detail-generating" label="② 케미 상세 · 발행 중" width={ABW} height={ABH}>
        <Wrap id="·" name="케미 상세 — 발행 중" sub="SSE 도착 전 스켈레톤"
      note={<><span className="label">상태</span>비동기 발행 직후 상세에 바로 진입해도 의미 있게 보여야 함. <b>상단 두 아바타는 즉시 표시</b>(즉시 알 수 있는 정보), 본문은 <b>스피너 + 안내 + 스켈레톤</b>. SSE 완료 이벤트가 도착하면 같은 자리에서 마크다운으로 자동 전환.</>}>
          <ChemistryDetail generating={true} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="detail-ready" label="③ 케미 상세 · 완료" width={ABW} height={ABH}>
        <Wrap id="·" name="케미 상세 — 완료" sub="/chemistry/[id]"
      note={<><span className="label">구조</span>상단 hero는 <b>두 사람 ↔ 두 사람</b>의 대칭 구도(아바타·이름·유형 칩). 본문은 결과 상세와 동일한 마크다운 컴포넌트 재사용 — 6 섹션(한눈/통하는 지점/마찰 지점/협업 팁/대화 팁/한 줄 요약). 우상단 ↗로 카톡 공유.</>}>
          <ChemistryDetail generating={false} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="invite" label="④ 동료 초대 수락" width={ABW} height={ABH}>
        <Wrap id="·" name="동료 초대 수락" sub="/invite/[code] · 비회원 진입"
      note={<><span className="label">외부 진입</span>카톡 링크를 비회원이 탭했을 때의 첫 화면. <b>초대자 프로필(아바타·유형)</b>을 큼지막하게 → "동료가 되면" 3가지 혜택 → CTA는 <b>카카오 로그인 + 자동 동료 등록</b>. 코드는 하단에 작게 표시하고 24시간 유효 안내.</>}>
          <InviteAccept />
        </Wrap>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>;


ReactDOM.createRoot(document.getElementById('root')).render(<App />);