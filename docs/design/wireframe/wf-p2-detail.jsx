// P2 — 결과 상세 (단일 안)
// 서버에서 받은 마크다운 보고서 위에, DISC 4축 시각화 컴포넌트를 확대하여 hero로 재사용.
// 검사 종류에 따라 본문(마크다운)은 서버에서 다르게 제공됨.

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

const HeaderMember = () => (
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
  </div>
);

const TabBar = ({ active = 1 }) => {
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

const WfFrame = ({ children, screenLabel }) => (
  <div className="wf" data-screen-label={screenLabel}>
    <StatusBar />
    <HeaderMember />
    <div className="wf-body">{children}</div>
    <TabBar active={1} />
  </div>
);

// ─────────────────────────────────────────────────────────
// DISC visualization — reused from results list, ENLARGED
// ─────────────────────────────────────────────────────────

const DISC = {
  D: { name: '주도', full: '주도형 — Dominance', color: 'var(--disc-d)' },
  I: { name: '사교', full: '사교형 — Influence', color: 'var(--disc-i)' },
  S: { name: '안정', full: '안정형 — Steadiness', color: 'var(--disc-s)' },
  C: { name: '신중', full: '신중형 — Conscientiousness', color: 'var(--disc-c)' },
};

const TypePill = ({ type }) => (
  <span className={"type-pill type-" + type.toLowerCase()}>
    <span className="dot"></span>
    <span>{type} · {DISC[type].name}형</span>
  </span>
);

// Same shape as P2 list MiniDiscBars, but height=150 + percentage labels.
// In production, this is one component with a `size` prop.
const DiscBarsLarge = ({ scores }) => (
  <div className="disc-bars-lg">
    {['D', 'I', 'S', 'C'].map((k) => (
      <div key={k} className="col">
        <div className="barwrap">
          <div className="bar" style={{
            height: scores[k] + '%',
            background: DISC[k].color,
            opacity: 0.85,
          }} />
        </div>
        <div className="footer">
          <span className="axis" style={{ color: DISC[k].color }}>{k}</span>
          <span className="axis-name">{DISC[k].name}</span>
          <span className="pct">{scores[k]}</span>
        </div>
      </div>
    ))}
  </div>
);

// ─────────────────────────────────────────────────────────
// Mock markdown body
// 본문은 서버에서 받아온 마크다운을 그대로 렌더 (검사 유형마다 다른 보고서)
// ─────────────────────────────────────────────────────────

const ReportMarkdown = ({ top, rater }) => (
  <div className="md">
    {/* server-provided markdown sample */}
    <div style={{
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      marginBottom: 14,
    }}>
      <span style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>
        ## 보고서 본문
      </span>
      <span className="src-tag">md from server</span>
    </div>

    <h2>한눈에 보는 {rater ? `${rater}님이 본 ` : ''}당신</h2>
    <p>
      당신은 <b>{top} · {DISC[top].name}형</b>에 가까운 사람이에요.
      목표를 향해 빠르게 움직이는 추진력이 강점이고, 동시에 주변 사람의 페이스를 살피는 균형 감각도 갖추고 있어요.
    </p>
    <blockquote>
      "결정을 미루기보다 일단 시도해 보는 편이에요."
    </blockquote>

    <h2>강점</h2>
    <ul>
      <li><b>결단력</b> — 흐름이 끊기지 않게 빠르게 의사결정합니다.</li>
      <li><b>주도성</b> — 누가 시키지 않아도 먼저 움직여요.</li>
      <li><b>도전 의식</b> — 익숙한 방식보다 새로운 시도에 끌립니다.</li>
    </ul>

    <h2>주의할 점</h2>
    <ul>
      <li>속도에 집중하다 보면 <b>주변의 신호를 놓치기 쉬워요</b>.</li>
      <li>의견 차이를 빠르게 정리하려다 상대가 위축될 수 있어요.</li>
    </ul>

    <h2>의사소통 스타일</h2>
    <p>
      간결하고 결과 중심으로 말하는 편이에요. 회의에서는 <b>결론부터 먼저</b> 듣는 것을 선호합니다.
      대화 상대가 신중형(C)에 가깝다면, 의사결정 전에 충분한 데이터를 함께 살펴주면 좋아요.
    </p>

    <h2>잘 맞는 환경</h2>
    <ul>
      <li>의사결정 권한이 명확한 자리</li>
      <li>측정 가능한 목표가 있는 프로젝트</li>
      <li>변화의 속도가 빠른 팀</li>
    </ul>

    <h2>이번 회차의 인사이트</h2>
    <p>
      지난 검사 대비 <b>I(사교) 지표가 12점 올라갔어요</b>.
      최근 다른 사람과의 협업이 늘어났거나, 의도적으로 듣는 시간을 늘렸을 가능성이 있어요.
    </p>

    <hr />

    <p style={{ fontSize: 11, color: 'var(--ink-faint)' }}>
      이 보고서는 참고용 성향 분석이며, 심리·의학적 진단이 아닙니다.
    </p>
  </div>
);

// ─────────────────────────────────────────────────────────
// 결과 상세 — single variant, two states (self / other)
// ─────────────────────────────────────────────────────────

const ResultDetail = ({ kind = 'self' }) => {
  const data = kind === 'self'
    ? { date: '2026.05.20', top: 'D', scores: { D: 85, I: 60, S: 40, C: 70 }, rater: null }
    : { date: '2026.05.18', top: 'D', scores: { D: 80, I: 75, S: 35, C: 60 }, rater: '여자친구' };

  return (
    <WfFrame screenLabel={`P2 결과 상세 · ${kind === 'self' ? '내 검사 결과' : '친구가 본 내 모습'}`}>
      <div className="back-bar">
        <div className="back">‹</div>
        <div className="meta">
          <div className="title">DISC 검사 결과</div>
          <div className="sub">{data.date}{data.rater && ` · ${data.rater}`}</div>
        </div>
        <div className="share">↗</div>
      </div>

      {/* HERO — enlarged DISC viz (reused from list MiniDiscBars) */}
      <div className="hero-block">
        <div className="pre">
          {data.rater && (
            <span className="rater-pill">● {data.rater}</span>
          )}
          <TypePill type={data.top} />
        </div>
        <h1>
          {data.rater
            ? `${data.rater}님이 본 당신은 ${data.top}형`
            : `당신은 ${data.top}형`}
        </h1>
        <p className="blurb">
          {data.rater
            ? '평정자의 관점에서 본 당신의 성향이에요. 자기 평정과 비교해 보세요.'
            : '24문항 응시 결과를 4축으로 정리했어요.'}
        </p>
        <DiscBarsLarge scores={data.scores} />
      </div>

      {/* Markdown body */}
      <ReportMarkdown top={data.top} rater={data.rater} />
    </WfFrame>
  );
};

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
    <DCSection id="detail" title="결과 상세" subtitle="P2 · 서버 마크다운 보고서 위에 DISC 4축 시각화를 hero로 확대 재사용">
      <DCArtboard id="self" label="내 검사 결과" width={ABW} height={ABH}>
        <Wrap id="·" name="결과 상세" sub="자기 평정"
          note={<><span className="label">구조</span>상단 hero = <b>결과 이력의 미니 바 차트</b>를 확대한 동일 컴포넌트(<code style={{background:'var(--paper-3)',padding:'1px 4px',borderRadius:3,fontFamily:'JetBrains Mono'}}>size=lg</code>). 그 아래 본문은 서버가 보내준 <b>마크다운을 그대로 렌더</b>. <b>확장성</b> 새 검사(MBTI/Big5)는 시각화 컴포넌트만 추가하면 마크다운 본문은 그대로 흐름이 유지됨.</>}>
          <ResultDetail kind="self" />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="other" label="친구가 본 내 모습" width={ABW} height={ABH}>
        <Wrap id="·" name="결과 상세" sub="타인 평정"
          note={<><span className="label">변형</span>같은 컴포넌트, 상단에 <b>평정자 라벨 칩</b>("여자친구") + 제목과 본문이 평정자 시점으로 자연스럽게 전환. 마크다운에 평정자 이름을 변수로 받아 1인칭/3인칭을 서버가 결정.</>}>
          <ResultDetail kind="other" />
        </Wrap>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>
);

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
