// P2 — 검사 결과 이력 (2안: A 카드 리스트 / B 컴팩트 + 추이 그래프)
// "내 검사 결과" / "친구가 본 내 모습" 친숙어 탭

// ─────────────────────────────────────────────────────────
// Shared primitives (inlined; P2 is standalone)
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
// Data
// ─────────────────────────────────────────────────────────

const DISC = {
  D: { name: '주도형', cls: 'type-d', color: 'var(--disc-d)' },
  I: { name: '사교형', cls: 'type-i', color: 'oklch(0.45 0.14 80)' },
  S: { name: '안정형', cls: 'type-s', color: 'var(--disc-s)' },
  C: { name: '신중형', cls: 'type-c', color: 'var(--disc-c)' },
};

const SELF_RESULTS = [
  { id: 1, date: '2026.05.20', top: 'D', scores: { D: 85, I: 60, S: 40, C: 70 } },
  { id: 2, date: '2026.04.02', top: 'D', scores: { D: 78, I: 65, S: 45, C: 68 } },
  { id: 3, date: '2026.02.14', top: 'I', scores: { D: 60, I: 80, S: 55, C: 50 } },
  { id: 4, date: '2025.12.10', top: 'S', scores: { D: 50, I: 55, S: 75, C: 60 } },
];

const OTHER_RESULTS = [
  { id: 1, date: '2026.05.18', rater: '여자친구', top: 'D', scores: { D: 80, I: 75, S: 35, C: 60 } },
  { id: 2, date: '2026.05.10', rater: '팀장님',   top: 'C', scores: { D: 55, I: 40, S: 60, C: 85 } },
  { id: 3, date: '2026.04.20', rater: '친한친구', top: 'I', scores: { D: 65, I: 88, S: 50, C: 45 } },
  { id: 4, date: '2026.03.05', rater: '어머니',   top: 'S', scores: { D: 45, I: 60, S: 80, C: 50 } },
];

// ─────────────────────────────────────────────────────────
// Visualizations
// ─────────────────────────────────────────────────────────

const MiniDiscBars = ({ scores, height = 50 }) => (
  <div className="disc-bars" style={{ height }}>
    {['D', 'I', 'S', 'C'].map((k) => {
      const pct = scores[k];
      return (
        <div key={k} className="col">
          <div className="bar" style={{
            height: pct + '%',
            background: DISC[k].color,
            opacity: 0.85,
          }}></div>
          <div className="label">{k}</div>
        </div>
      );
    })}
  </div>
);

// Tiny trend mini-chart — 4 lines (D/I/S/C) over time
const TrendMini = ({ data, height = 70 }) => {
  // data = array of { D, I, S, C }; we plot polyline per axis
  const w = 280, h = height;
  const n = data.length;
  const xStep = w / (n - 1);
  const yFor = (v) => h - (v / 100) * h;
  const buildPath = (k) =>
    data.map((p, i) => `${i === 0 ? 'M' : 'L'} ${(i * xStep).toFixed(1)} ${yFor(p[k]).toFixed(1)}`).join(' ');
  return (
    <svg width="100%" viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none" style={{ display: 'block' }}>
      {/* grid */}
      {[0, 25, 50, 75, 100].map((v) => (
        <line key={v} x1="0" x2={w} y1={yFor(v)} y2={yFor(v)} stroke="var(--line-soft)" strokeWidth="0.5" strokeDasharray="2 3" />
      ))}
      {['D', 'I', 'S', 'C'].map((k) => (
        <path key={k} d={buildPath(k)} fill="none" stroke={DISC[k].color} strokeWidth="1.8" strokeLinejoin="round" strokeLinecap="round" />
      ))}
      {/* end dots */}
      {['D', 'I', 'S', 'C'].map((k) => {
        const last = data[data.length - 1];
        return <circle key={k} cx={w} cy={yFor(last[k])} r="2.5" fill={DISC[k].color} />;
      })}
    </svg>
  );
};

const TypePill = ({ type }) => (
  <span className={"type-pill " + DISC[type].cls}>
    <span className="dot"></span>
    <span>{type} · {DISC[type].name}</span>
  </span>
);

// Test type filter chips — between rater tabs and list
const TypeFilter = ({ active = 'DISC' }) => (
  <div style={{
    background: 'white',
    borderBottom: '1px solid var(--line)',
    padding: '10px 14px',
    display: 'flex',
    gap: 8,
    overflowX: 'auto',
    alignItems: 'center',
  }}>
    <span style={{
      fontSize: 10, color: 'var(--ink-faint)',
      fontFamily: "'JetBrains Mono', monospace",
      letterSpacing: '0.04em', textTransform: 'uppercase',
      whiteSpace: 'nowrap', marginRight: 2,
    }}>⎵ 검사 종류</span>
    {[
      { key: 'DISC' },
    ].map((c) => {
      const isActive = c.key === active;
      return (
        <div key={c.key} style={{
          padding: '5px 12px',
          borderRadius: 999,
          border: '1.2px solid ' + (isActive ? 'var(--ink)' : 'var(--line)'),
          background: isActive ? 'var(--ink)' : 'white',
          color: isActive ? 'var(--paper)' : 'var(--ink)',
          fontSize: 11.5,
          fontWeight: 700,
          whiteSpace: 'nowrap',
          display: 'flex', alignItems: 'center', gap: 4,
        }}>
          <span>{c.key}</span>
        </div>
      );
    })}
  </div>
);

// ─────────────────────────────────────────────────────────
// A · 카드 리스트 (수직 스택)
// ─────────────────────────────────────────────────────────

const ResultsACard = ({ r, showRater }) => (
  <div style={{
    border: '1.5px solid var(--ink)',
    borderRadius: 14,
    background: 'white',
    padding: '14px 16px',
    display: 'grid',
    gridTemplateColumns: '1fr auto',
    gap: 12,
    alignItems: 'center',
  }}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      {showRater && (
        <div style={{
          display: 'inline-flex', alignSelf: 'flex-start',
          padding: '2px 8px', borderRadius: 999,
          background: 'var(--member-bg)', color: 'var(--member)',
          fontSize: 10.5, fontWeight: 700,
        }}>● {r.rater}</div>
      )}
      <TypePill type={r.top} />
      <div style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>{r.date}</div>
    </div>
    <div style={{ width: 100 }}>
      <MiniDiscBars scores={r.scores} height={44} />
    </div>
  </div>
);

const ResultsA = ({ tab = 'self' }) => (
  <WfFrame screenLabel={`P2 결과이력 · A · ${tab === 'self' ? '내 검사 결과' : '친구가 본 내 모습'}`}>
    <div className="page-tabs">
      <div className={"pt" + (tab === 'self' ? ' active' : '')}>내 검사 결과</div>
      <div className={"pt" + (tab === 'other' ? ' active' : '')}>친구가 본 내 모습</div>
    </div>
    <TypeFilter />
    <div style={{ padding: '14px 14px 20px', display: 'flex', flexDirection: 'column', gap: 10 }}>
      {(tab === 'self' ? SELF_RESULTS : OTHER_RESULTS).map((r) => (
        <ResultsACard key={r.id} r={r} showRater={tab === 'other'} />
      ))}
      <div style={{ height: 6 }} />
      <div style={{
        textAlign: 'center', fontSize: 11, color: 'var(--ink-faint)',
        fontFamily: "'JetBrains Mono', monospace",
      }}>· 더 이상의 기록이 없어요 ·</div>
    </div>
  </WfFrame>
);

// ─────────────────────────────────────────────────────────
// B · 컴팩트 리스트 + 상단 추이 그래프
// ─────────────────────────────────────────────────────────

const ResultsBRow = ({ r, showRater }) => (
  <div style={{
    background: 'white',
    borderTop: '1px solid var(--line-soft)',
    padding: '12px 16px',
    display: 'grid',
    gridTemplateColumns: '46px 1fr auto',
    gap: 12,
    alignItems: 'center',
  }}>
    {/* Date stamp */}
    <div style={{
      textAlign: 'center',
      fontFamily: "'JetBrains Mono', monospace",
      lineHeight: 1.15,
    }}>
      <div style={{ fontSize: 9, color: 'var(--ink-faint)' }}>{r.date.slice(0, 4)}</div>
      <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--ink)' }}>{r.date.slice(5)}</div>
    </div>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <TypePill type={r.top} />
        {showRater && (
          <span style={{
            fontSize: 10.5, color: 'var(--member)', fontWeight: 700,
          }}>· {r.rater}</span>
        )}
      </div>
      {/* inline 4-axis micro */}
      <div style={{ display: 'flex', gap: 3, alignItems: 'center' }}>
        {['D', 'I', 'S', 'C'].map((k) => (
          <div key={k} style={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <span style={{ fontSize: 8.5, fontFamily: "'JetBrains Mono', monospace", color: 'var(--ink-faint)' }}>{k}</span>
            <div style={{
              width: 24, height: 4, background: 'var(--paper-3)',
              borderRadius: 2, overflow: 'hidden',
            }}>
              <div style={{
                width: r.scores[k] + '%',
                height: '100%', background: DISC[k].color,
              }}></div>
            </div>
          </div>
        ))}
      </div>
    </div>
    <div style={{ color: 'var(--ink-faint)', fontSize: 16 }}>›</div>
  </div>
);

const ResultsB = ({ tab = 'self' }) => {
  const list = tab === 'self' ? SELF_RESULTS : OTHER_RESULTS;
  // For trend chart we want oldest → newest
  const trendData = [...list].reverse().map((r) => r.scores);

  return (
    <WfFrame screenLabel={`P2 결과이력 · B · ${tab === 'self' ? '내 검사 결과' : '친구가 본 내 모습'}`}>
      <div className="page-tabs">
        <div className={"pt" + (tab === 'self' ? ' active' : '')}>내 검사 결과</div>
        <div className={"pt" + (tab === 'other' ? ' active' : '')}>친구가 본 내 모습</div>
      </div>
      <TypeFilter />

      {/* Trend mini-chart card */}
      <div style={{
        padding: '14px 16px',
        background: 'white',
        borderBottom: '1px solid var(--line)',
      }}>
        <div style={{
          display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
          marginBottom: 6,
        }}>
          <div style={{ fontSize: 12.5, fontWeight: 700 }}>
            {tab === 'self' ? '나의 변화 추이' : '평정자별 분포'}
          </div>
          <div style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>최근 {list.length}회</div>
        </div>
        <TrendMini data={trendData} height={70} />
        {/* legend */}
        <div style={{ display: 'flex', gap: 10, marginTop: 6, flexWrap: 'wrap' }}>
          {['D', 'I', 'S', 'C'].map((k) => (
            <div key={k} style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 10, color: 'var(--ink-soft)' }}>
              <span style={{ width: 10, height: 3, background: DISC[k].color, borderRadius: 2 }}></span>
              <span style={{ fontFamily: "'JetBrains Mono', monospace", fontWeight: 600, color: 'var(--ink)' }}>{k}</span>
              <span>{DISC[k].name}</span>
            </div>
          ))}
        </div>
      </div>

      {/* List */}
      <div style={{ background: 'var(--paper-2)' }}>
        <div style={{
          padding: '10px 16px 4px',
          fontSize: 10.5, color: 'var(--ink-faint)',
          fontFamily: "'JetBrains Mono', monospace",
          letterSpacing: '0.06em', textTransform: 'uppercase',
        }}>이력 · 최근부터</div>
        {list.map((r) => (
          <ResultsBRow key={r.id} r={r} showRater={tab === 'other'} />
        ))}
        <div style={{
          textAlign: 'center', padding: '14px 0 20px',
          fontSize: 11, color: 'var(--ink-faint)',
          fontFamily: "'JetBrains Mono', monospace",
        }}>· 더 이상의 기록이 없어요 ·</div>
      </div>
    </WfFrame>
  );
};

// ─────────────────────────────────────────────────────────
// Canvas
// ─────────────────────────────────────────────────────────

const ABW = 380, ABH = 970;

const Caption = ({ id, name, sub }) => (
  <div className="variant-cap" style={{ marginTop: 0, marginBottom: 8 }}>
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
    <DCSection id="results-a" title="A · 카드 리스트  ✓ 확정" subtitle="P2 · 각 결과를 독립 카드로 — DISC 4축 미니 바 차트 + 유형 + 날짜 (·라벨)">
      <DCArtboard id="a-self" label="A · 내 검사 결과  ✓ 확정" width={ABW} height={ABH}>
        <Wrap id="A·1 ✓" name="카드 리스트" sub="내 검사 결과 탭 · 확정안"
          note={<><span className="label">레이아웃</span>각 카드 = <b>유형 칩</b> + <b>날짜</b> + 우측에 <b>4축 미니 바 차트</b>. <b>장점</b> 한 카드 안에서 결과의 인상(유형 + 분포)을 한눈에 파악. P1 알약 스택의 단정한 톤을 이어받음. <b>단점</b> 변화 추이는 별도 화면에서 확인.</>}>
          <ResultsA tab="self" />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="a-other" label="A · 친구가 본 내 모습  ✓ 확정" width={ABW} height={ABH}>
        <Wrap id="A·2 ✓" name="카드 리스트" sub="친구가 본 내 모습 탭 · 확정안"
          note={<><span className="label">친숙어</span>카드 상단에 <b>평정자 라벨</b>(예: "여자친구", "팀장님") 칩으로 누가 본 결과인지 즉시 파악. 자기 결과 탭과 동일 카드 골격을 유지해 학습 비용 ↓.</>}>
          <ResultsA tab="other" />
        </Wrap>
      </DCArtboard>
    </DCSection>

    <DCSection id="results-b" title="B · 컴팩트 리스트 + 추이 그래프" subtitle="P2 · 상단에 4축 변화 추이 그래프 + 작은 행 리스트">
      <DCArtboard id="b-self" label="B · 내 검사 결과" width={ABW} height={ABH}>
        <Wrap id="B·1" name="추이 + 컴팩트" sub="내 검사 결과 탭"
          note={<><span className="label">분석 톤</span>상단 <b>변화 추이 그래프</b>(D/I/S/C 4축 시계열)로 "나는 어떻게 변했나"를 즉시. 하단은 가벼운 행 리스트. <b>장점</b> 인사이트 진입 없이도 추이 파악. <b>단점</b> 결과가 1~2개뿐일 때 추이 영역이 비어 보일 수 있음 — 빈 상태 처리 필요.</>}>
          <ResultsB tab="self" />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="b-other" label="B · 친구가 본 내 모습" width={ABW} height={ABH}>
        <Wrap id="B·2" name="추이 + 컴팩트" sub="친구가 본 내 모습 탭"
          note={<><span className="label">변형</span>"친구가 본 내 모습" 탭에서는 추이 영역의 제목을 <b>"평정자별 분포"</b>로 바꿔, 시간 순보다 "누가 어떻게 봤는지"를 강조. 행에는 평정자 라벨이 유형 옆에 따라붙음.</>}>
          <ResultsB tab="other" />
        </Wrap>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>
);

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
