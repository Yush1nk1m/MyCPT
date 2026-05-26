// Wireframes — 메인 / 검사 시트 / 공유 시트 (3안씩)

const { useState } = React;

// ─────────────────────────────────────────────────────────
// 메인 화면 — 3안
// ─────────────────────────────────────────────────────────

const MainA = () =>
<WfFrame member={true} activeTab={0}>
    <div style={{ padding: '22px 12px 16px', display: 'flex', flexDirection: 'column', gap: 14, height: '100%' }}>
      <div className="pill hero">
        <div className="title">나는 누구일까?</div>
        <div className="sub">(심리검사)</div>
      </div>
      <div className="pill">
        <div className="title">남이 보는 내 모습</div>
        <div className="sub">(친구에게 공유하기)</div>
      </div>
      <div className="pill">
        <div className="title">우리 잘 맞을까?</div>
        <div className="sub">(동료와의 케미 검사)</div>
      </div>
      <div className="pill" style={{ background: 'var(--paper-2)' }}>
        <div className="title" style={{ fontSize: 15 }}>이 서비스는 무엇인가요?</div>
        <div className="sub">(서비스 소개 &amp; 약관)</div>
      </div>
    </div>
  </WfFrame>;


const MainAGuest = () =>
<WfFrame member={false} activeTab={0} lockedExcept={0}>
    <div style={{ padding: '22px 12px 16px', display: 'flex', flexDirection: 'column', gap: 14, height: '100%' }}>
      <div className="pill hero">
        <div className="title">나는 누구일까?</div>
        <div className="sub">(심리검사)</div>
      </div>
      <div className="pill locked">
        <div className="title">남이 보는 내 모습</div>
        <div className="sub">(친구에게 공유하기)</div>
        <span className="lock-badge">🔒 회원</span>
      </div>
      <div className="pill locked">
        <div className="title">우리 잘 맞을까?</div>
        <div className="sub">(동료와의 케미 검사)</div>
        <span className="lock-badge">🔒 회원</span>
      </div>
      <div className="pill" style={{ background: 'var(--paper-2)' }}>
        <div className="title" style={{ fontSize: 15 }}>이 서비스는 무엇인가요?</div>
        <div className="sub">(서비스 소개 &amp; 약관)</div>
      </div>
      {/* toast */}
      <div style={{
      position: 'absolute', left: 16, right: 16, bottom: 80,
      background: 'var(--ink)', color: 'white', borderRadius: 999,
      padding: '10px 14px', fontSize: 11.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between'
    }}>
        <span>🔒 로그인이 필요해요</span>
        <span style={{ background: '#FEE500', color: '#191919', padding: '3px 10px', borderRadius: 999, fontWeight: 700, fontSize: 10.5 }}>카카오로 시작</span>
      </div>
    </div>
  </WfFrame>;


const MainB = () =>
<WfFrame member={true} activeTab={0}>
    <div style={{ padding: '16px 14px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, height: '100%', gridAutoRows: '1fr' }}>
      {[
    { t: '나는 누구일까?', s: '심리검사', bg: 'oklch(0.96 0.04 95)', locked: false },
    { t: '남이 보는 내 모습', s: '친구에게 공유', bg: 'white', locked: false },
    { t: '우리 잘 맞을까?', s: '동료와의 케미', bg: 'white', locked: false },
    { t: '이 서비스는?', s: '소개 & 약관', bg: 'var(--paper-2)', locked: false }].
    map((c, i) =>
    <div key={i} style={{
      border: '1.5px solid var(--ink)',
      borderRadius: 14,
      background: c.bg,
      padding: 12,
      display: 'flex',
      flexDirection: 'column',
      gap: 8,
      position: 'relative'
    }}>
          <div className="placeholder-img" style={{ flex: 1, minHeight: 0 }}>illust {i + 1}</div>
          <div>
            <div style={{ fontSize: 13, fontWeight: 700 }}>{c.t}</div>
            <div style={{ fontSize: 10.5, color: 'var(--ink-soft)', marginTop: 1 }}>{c.s}</div>
          </div>
        </div>
    )}
    </div>
  </WfFrame>;


const MainC = () =>
<WfFrame member={true} activeTab={0}>
    <div style={{ padding: '14px 14px 14px', display: 'flex', flexDirection: 'column', gap: 10, height: '100%' }}>
      {/* hero card */}
      <div style={{
      flex: 1,
      border: '1.5px solid var(--ink)',
      borderRadius: 18,
      background: 'oklch(0.96 0.04 95)',
      padding: 16,
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      position: 'relative'
    }}>
        <div className="placeholder-img" style={{ flex: 1, marginBottom: 10 }}>main illust</div>
        <div>
          <div style={{ fontSize: 18, fontWeight: 700 }}>나는 누구일까?</div>
          <div style={{ fontSize: 12, color: 'var(--ink-soft)', marginTop: 2 }}>(심리검사) · 누구나 시작</div>
          <div style={{
          marginTop: 10,
          background: 'var(--ink)', color: 'var(--paper)',
          textAlign: 'center', padding: '10px 0',
          borderRadius: 10, fontSize: 13, fontWeight: 700
        }}>지금 검사 시작 ›</div>
        </div>
      </div>
      {/* 3 secondary */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
        {['남이 보는\n내 모습', '우리 잘\n맞을까?', '서비스\n소개'].map((t, i) =>
      <div key={i} style={{
        border: '1.5px solid var(--ink)',
        borderRadius: 12,
        background: 'white',
        padding: '10px 4px',
        textAlign: 'center',
        fontSize: 11.5,
        fontWeight: 600,
        lineHeight: 1.3,
        position: 'relative'
      }}>{t}</div>
      )}
      </div>
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 검사 풀스크린 시트 — 3안
// ─────────────────────────────────────────────────────────

const Q_TEXT = '아래 4개 중에서, 나와 가장 가까운 것과 가장 먼 것을 골라주세요.';
const OPTIONS = ['결과를 빠르게 만들고 싶다', '사람들과 어울리는 게 좋다', '꼼꼼하게 계획하는 편이다', '주변을 챙기는 편이다'];

// A. 상단 진행바 + 4문항 카드 (Most/Least 칩)
const TestA = () =>
<SheetFrame title="DISC 검사" step={2} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1 }}>
      <div className="progress-bar"><div className="fill" style={{ width: '42%' }}></div></div>
      <div style={{
      fontFamily: "'JetBrains Mono', monospace", fontSize: 11,
      color: 'var(--ink-soft)', textAlign: 'right'
    }}>10 / 24</div>
      <div style={{ fontSize: 13.5, fontWeight: 600, lineHeight: 1.45 }}>
        {Q_TEXT}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
        {OPTIONS.map((opt, i) =>
      <div key={i} className="opt-card">
            <div className="opt-text">{opt}</div>
            <div className="opt-controls">
              <div className={"opt-pick" + (i === 0 ? " most" : "")}>가까움</div>
              <div className={"opt-pick" + (i === 2 ? " least" : "")}>먼</div>
            </div>
          </div>
      )}
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 8 }}>
        <div className="btn-secondary">‹ 이전</div>
        <div className="btn-primary">다음 ›</div>
      </div>
    </div>
  </SheetFrame>;


// B. 스와이프 카드 + dot indicator
const TestB = () =>
<SheetFrame title="DISC 검사" step={2} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1 }}>
      {/* dot row */}
      <div style={{ display: 'flex', gap: 3, justifyContent: 'center', flexWrap: 'wrap', padding: '4px 0' }}>
        {Array.from({ length: 24 }).map((_, i) =>
      <div key={i} style={{
        width: 9, height: 9, borderRadius: '50%',
        background: i < 10 ? 'var(--accent)' : i === 10 ? 'var(--ink)' : 'var(--paper-3)',
        border: '1px solid var(--line)'
      }} />
      )}
      </div>
      <div style={{
      fontFamily: "'JetBrains Mono', monospace", fontSize: 11,
      color: 'var(--ink-soft)', textAlign: 'center'
    }}>11번 문항</div>

      <div style={{
      flex: 1,
      border: '1.5px solid var(--ink)',
      borderRadius: 14,
      background: 'white',
      padding: 14,
      boxShadow: '4px 4px 0 var(--paper-3)',
      display: 'flex', flexDirection: 'column', gap: 10,
      position: 'relative'
    }}>
        <div style={{ fontSize: 12.5, fontWeight: 600, lineHeight: 1.45 }}>{Q_TEXT}</div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
          <div style={{ fontSize: 10.5, color: 'var(--member)', fontWeight: 700, letterSpacing: '0.04em' }}>● 가장 가까운</div>
          {OPTIONS.map((opt, i) =>
        <div key={i} style={{
          border: '1.2px solid ' + (i === 0 ? 'var(--member)' : 'var(--line)'),
          background: i === 0 ? 'oklch(0.96 0.04 150)' : 'white',
          borderRadius: 8, padding: '7px 10px', fontSize: 11.5
        }}>{opt}</div>
        )}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
          <div style={{ fontSize: 10.5, color: 'var(--accent)', fontWeight: 700, letterSpacing: '0.04em' }}>● 가장 먼</div>
          {OPTIONS.map((opt, i) => {
          const disabled = i === 0; // 가장 가까운에서 이미 선택됨
          return (
            <div key={i} style={{
              border: '1.2px solid ' + (i === 2 ? 'var(--accent)' : 'var(--line)'),
              background: i === 2 ? 'oklch(0.96 0.04 30)' : disabled ? 'var(--paper-3)' : 'white',
              borderRadius: 8, padding: '7px 10px', fontSize: 11.5,
              opacity: disabled ? 0.45 : 1,
              textDecoration: disabled ? 'line-through' : 'none',
              color: disabled ? 'var(--ink-faint)' : 'var(--ink)',
              display: 'flex', alignItems: 'center', justifyContent: 'space-between'
            }}>
              <span>{opt}</span>
              {disabled && <span style={{ fontSize: 9.5, color: 'var(--member)', fontFamily: "'JetBrains Mono', monospace" }}>가까운으로 선택됨</span>}
            </div>);
        })}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 8 }}>
        <div className="btn-secondary">‹ 이전</div>
        <div className="btn-primary">다음 ›</div>
      </div>
    </div>
  </SheetFrame>;


// C. 한 화면 / Most·Least 영역 분리 (드래그 메타포)
const TestC = () =>
<SheetFrame title="DISC 검사" step={2} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1 }}>
      {/* segmented progress */}
      <div style={{ display: 'flex', gap: 2 }}>
        {Array.from({ length: 24 }).map((_, i) =>
      <div key={i} style={{
        flex: 1, height: 5,
        background: i < 10 ? 'var(--accent)' : 'var(--paper-3)',
        borderRadius: 2
      }} />
      )}
      </div>
      <div style={{ fontSize: 11, color: 'var(--ink-soft)', fontFamily: "'JetBrains Mono', monospace" }}>
        Q.11 — 가장 가까운 / 먼 문항을 한 가지씩 골라주세요
      </div>

      {/* pool of options */}
      <div style={{
      border: '1px dashed var(--line)',
      borderRadius: 10, padding: '10px 10px',
      background: 'var(--paper-2)'
    }}>
        <div style={{ fontSize: 10, color: 'var(--ink-faint)', marginBottom: 6, fontFamily: "'JetBrains Mono', monospace" }}>OPTIONS</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {OPTIONS.map((opt, i) =>
        <div key={i} style={{
          border: '1.2px solid var(--ink)',
          borderRadius: 999, padding: '8px 14px',
          background: 'white', fontSize: 11.5,
          cursor: 'grab',
          display: 'flex', alignItems: 'center', gap: 8
        }}>
              <span style={{ color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", fontSize: 10 }}>⋮⋮</span>
              {opt}
            </div>
        )}
        </div>
      </div>

      {/* Most slot */}
      <div style={{
      border: '1.5px dashed var(--member)',
      background: 'oklch(0.96 0.04 150 / 0.5)',
      borderRadius: 10, padding: 10, minHeight: 60
    }}>
        <div style={{ fontSize: 10.5, color: 'var(--member)', fontWeight: 700, marginBottom: 4 }}>● 가장 가까운 (Most)</div>
        <div style={{
        border: '1.2px solid var(--member)', background: 'white',
        borderRadius: 999, padding: '8px 14px', fontSize: 11.5
      }}>결과를 빠르게 만들고 싶다</div>
      </div>

      {/* Least slot */}
      <div style={{
      border: '1.5px dashed var(--accent)',
      background: 'oklch(0.96 0.04 30 / 0.5)',
      borderRadius: 10, padding: 10, minHeight: 60
    }}>
        <div style={{ fontSize: 10.5, color: 'var(--accent)', fontWeight: 700, marginBottom: 4 }}>● 가장 먼 (Least)</div>
        <div style={{
        border: '1.2px dashed var(--line)', background: 'transparent',
        borderRadius: 999, padding: '8px 14px', fontSize: 11.5,
        color: 'var(--ink-faint)', textAlign: 'center'
      }}>여기에 끌어다 놓으세요</div>
      </div>
    </div>
  </SheetFrame>;


// ─────────────────────────────────────────────────────────
// 공유 풀스크린 시트 — 3안 (검사 모달과 동일 컴포넌트, Step 분기)
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// 검사 시트 Step 1 / Step 3 / 응시 중단 다이얼로그 (P6 통합)
// ─────────────────────────────────────────────────────────

const DISC_COLORS = {
  D: 'oklch(0.60 0.17 30)',
  I: 'oklch(0.70 0.14 80)',
  S: 'oklch(0.55 0.13 150)',
  C: 'oklch(0.55 0.13 240)',
};
const DISC_NAMES = { D: '주도', I: '사교', S: '안정', C: '신중' };

const DiscBarsLarge = ({ scores }) => (
  <div className="disc-bars-lg">
    {['D', 'I', 'S', 'C'].map((k) => (
      <div key={k} className="col">
        <div className="barwrap">
          <div className="bar" style={{
            height: scores[k] + '%',
            background: DISC_COLORS[k], opacity: 0.85,
          }} />
        </div>
        <div className="footer">
          <span className="axis" style={{ color: DISC_COLORS[k] }}>{k}</span>
          <span className="axis-name">{DISC_NAMES[k]}</span>
          <span className="pct">{scores[k]}</span>
        </div>
      </div>
    ))}
  </div>
);

const TypePillD = () => (
  <span className="type-pill type-d">
    <span className="dot"></span>
    <span>D · 주도형</span>
  </span>
);

// Step 1 — 유형 선택
const TestStep1 = () => (
  <SheetFrame title="나는 누구일까?" step={1} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14, flex: 1 }}>
      <div style={{ fontSize: 13.5, color: 'var(--ink-soft)', lineHeight: 1.55 }}>
        어떤 검사로 알아볼까요?
      </div>

      <div style={{
        border: '1.5px solid var(--ink)', borderRadius: 14,
        background: 'oklch(0.96 0.04 95)', padding: '16px 18px',
        display: 'flex', flexDirection: 'column', gap: 8,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: 18, fontWeight: 800 }}>DISC</span>
          <div style={{
            width: 22, height: 22, borderRadius: '50%',
            background: 'var(--ink)', color: 'white',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 13, fontWeight: 800,
          }}>✓</div>
        </div>
        <div style={{ fontSize: 12, color: 'var(--ink-soft)', lineHeight: 1.5 }}>
          행동 성향을 4가지 축(주도·사교·안정·신중)으로 나눠 분석해요.
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
          <span style={{ fontSize: 10.5, fontFamily: "'JetBrains Mono', monospace", color: 'var(--ink-soft)', background: 'white', padding: '3px 8px', borderRadius: 999, border: '1px solid var(--line)' }}>24문항</span>
          <span style={{ fontSize: 10.5, fontFamily: "'JetBrains Mono', monospace", color: 'var(--ink-soft)', background: 'white', padding: '3px 8px', borderRadius: 999, border: '1px solid var(--line)' }}>약 3분</span>
          <span style={{ fontSize: 10.5, fontFamily: "'JetBrains Mono', monospace", color: 'var(--member)', background: 'var(--member-bg)', padding: '3px 8px', borderRadius: 999 }}>● 누구나 무료</span>
        </div>
      </div>

      <div style={{
        background: 'var(--paper-2)', border: '1px dashed var(--line)',
        borderRadius: 10, padding: '12px 14px',
        fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.6,
      }}>
        <b style={{ color: 'var(--ink)' }}>안내</b> · MVP 단계엔 DISC만 제공. 추후 MBTI · Big5 추가 예정.
      </div>

      <div style={{ flex: 1 }} />

      <div style={{
        background: 'var(--paper-2)', border: '1px solid var(--line)',
        borderRadius: 10, padding: '10px 14px',
        fontSize: 11, color: 'var(--ink-soft)', lineHeight: 1.5,
      }}>
        <b style={{ color: 'var(--ink)' }}>참고용</b> 성향 분석이며, 심리·의학적 진단이 아닙니다.
      </div>

      <div className="btn-primary">시작하기 (24문항)</div>
    </div>
  </SheetFrame>
);

// Step 3 — 결과 (비회원)
const TestStep3Guest = () => (
  <SheetFrame title="DISC 검사 결과" step={3} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1, overflow: 'auto' }}>
      <div style={{ textAlign: 'center' }}>
        <div style={{
          fontFamily: "'Caveat', cursive", fontSize: 22, fontWeight: 700,
          color: 'var(--accent)', marginBottom: 4,
        }}>검사가 끝났어요!</div>
        <TypePillD />
      </div>

      <h1 style={{
        fontFamily: 'Inter', fontSize: 20, fontWeight: 800,
        margin: '0', textAlign: 'center', letterSpacing: '-0.3px',
      }}>당신은 D — 주도형</h1>

      <DiscBarsLarge scores={{ D: 85, I: 60, S: 40, C: 70 }} />

      <div style={{
        background: 'var(--paper-2)', border: '1px solid var(--line)',
        borderRadius: 10, padding: '12px 14px',
        fontSize: 12, color: 'var(--ink-soft)', lineHeight: 1.6,
      }}>
        <b style={{ color: 'var(--ink)' }}>한눈 요약</b> · 빠른 결단력과 추진력이 강점이고, 주변의 신호를 살피는 균형 감각도 갖추고 있어요. 결과 상세는 회원으로 저장 후 확인할 수 있어요.
      </div>

      <div style={{
        background: 'oklch(0.97 0.03 95)', border: '1.5px dashed var(--accent)',
        borderRadius: 12, padding: '14px 16px',
        display: 'flex', flexDirection: 'column', gap: 10,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 20 }}>📥</span>
          <div>
            <div style={{ fontSize: 13, fontWeight: 700 }}>결과를 저장하고 더 자세히 보기</div>
            <div style={{ fontSize: 11, color: 'var(--ink-soft)', marginTop: 1 }}>친구 평정 · 동료 케미도 가능해요</div>
          </div>
        </div>
        <div className="btn-kakao">💬 카카오로 시작하고 저장</div>
        <div style={{
          textAlign: 'center', fontSize: 10.5, color: 'var(--ink-faint)',
          textDecoration: 'underline',
        }}>저장 없이 닫기</div>
      </div>
    </div>
  </SheetFrame>
);

// Step 3 — 결과 (회원, 자동 저장)
const TestStep3Member = () => (
  <SheetFrame title="DISC 검사 결과" step={3} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1, overflow: 'auto' }}>
      <div style={{ textAlign: 'center' }}>
        <div style={{
          display: 'inline-flex', alignItems: 'center', gap: 5,
          background: 'var(--member-bg)', color: 'var(--member)',
          fontSize: 11, fontWeight: 700, padding: '3px 10px', borderRadius: 999,
          marginBottom: 6,
        }}>
          <span>✓</span><span>자동 저장됨</span>
        </div>
        <div style={{ fontFamily: "'Caveat', cursive", fontSize: 20, fontWeight: 700 }}>이번 검사의 당신은</div>
        <div style={{ marginTop: 6 }}><TypePillD /></div>
      </div>

      <h1 style={{
        fontFamily: 'Inter', fontSize: 20, fontWeight: 800,
        margin: 0, textAlign: 'center', letterSpacing: '-0.3px',
      }}>D — 주도형</h1>

      <DiscBarsLarge scores={{ D: 85, I: 60, S: 40, C: 70 }} />

      <div style={{
        background: 'var(--paper-2)', border: '1px solid var(--line)',
        borderRadius: 10, padding: '12px 14px',
        fontSize: 12, color: 'var(--ink-soft)', lineHeight: 1.6,
      }}>
        <b style={{ color: 'var(--ink)' }}>변화 알림</b> · 지난 검사 대비 <b style={{ color: 'var(--ink)' }}>I(사교) 지표가 12점</b> 올라갔어요.
      </div>

      <div style={{ flex: 1 }} />

      <div className="btn-primary">결과 상세로 가기 ›</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
        <div className="btn-secondary">친구에게 평정 요청</div>
        <div className="btn-secondary">동료와의 케미</div>
      </div>
    </div>
  </SheetFrame>
);

// 응시 중단 확인 다이얼로그 (Step 2 위에 떠 있는 상태)
const TestCloseDialog = () => (
  <div className="wf" data-screen-label="P1 · 검사 시트 응시 중단 다이얼로그">
    <StatusBar />
    <div style={{
      height: 22, background: 'oklch(0.55 0.01 250 / 0.4)',
      borderBottom: '1px solid var(--line)', fontSize: 9,
      fontFamily: "'JetBrains Mono', monospace", color: 'white',
      display: 'flex', alignItems: 'center', paddingLeft: 14,
    }}>← 메인 (dimmed)</div>

    <div style={{ flex: 1, background: 'white', position: 'relative', overflow: 'hidden' }}>
      <div style={{ opacity: 0.4, padding: 16 }}>
        <div className="sheet-handle"></div>
        <div className="sheet-head" style={{ padding: '6px 0 12px' }}>
          <div className="close">✕</div>
          <div className="title">DISC 검사</div>
          <div className="stepdot">Step 2/3</div>
        </div>
        <div style={{ marginTop: 16 }}>
          <div className="progress-bar"><div className="fill" style={{ width: '42%' }}></div></div>
          <div style={{ fontSize: 13, marginTop: 14, color: 'var(--ink-soft)' }}>
            아래 4개 중에서 가장 가까운 것과 먼 것을 골라주세요.
          </div>
        </div>
      </div>

      <div className="scrim">
        <div className="dialog-card">
          <div className="icon">⚠️</div>
          <h3>응시를 중단할까요?</h3>
          <p>지금까지 답한 <b style={{ color: 'var(--ink)' }}>10 / 24</b> 문항은 저장되지 않아요. 처음부터 다시 시작해야 합니다.</p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div className="btn-danger">중단하고 닫기</div>
            <div className="btn-secondary">계속 응시하기</div>
          </div>
        </div>
      </div>
    </div>
  </div>
);


// ─────────────────────────────────────────────────────────
// 공유 풀스크린 시트 — 3안 (검사 모달과 동일 컴포넌트, Step 분기)
// ─────────────────────────────────────────────────────────

// A. 검사 모달과 동일 UI, Step만 분기 (Step 1 유형 선택 → Step 3 링크 생성)
const ShareA = () =>
<SheetFrame title="남이 보는 내 모습" step={3} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14, flex: 1 }}>
      <div style={{ fontSize: 12.5, color: 'var(--ink-soft)' }}>
        <b style={{ color: 'var(--ink)' }}>1.</b> 선택한 검사 — <b style={{ color: 'var(--ink)' }}>DISC</b>
      </div>

      <div>
        <div style={{ fontSize: 12, color: 'var(--ink-soft)', marginBottom: 6, lineHeight: 1.4 }}>
          2. <b style={{ color: 'var(--ink)' }}>상대방의 호칭</b>
          <span style={{ color: 'var(--ink-faint)', fontSize: 11 }}>  · 초대 메시지에 포함되어요</span>
        </div>
        <div style={{
        border: '1.2px solid var(--ink)', borderRadius: 8,
        padding: '10px 12px', fontSize: 13, background: 'white'
      }}>여자친구</div>
        <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', marginTop: 4, fontStyle: 'italic' }}>
          예시 · 『여자친구님이 보기에 OOO님은 어떤 사람인가요?』
        </div>
      </div>

      <div>
        <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', marginBottom: 6 }}>3. 발급된 일회용 링크</div>
        <div style={{
        border: '1.2px dashed var(--ink)', borderRadius: 8,
        padding: '10px 12px', fontSize: 11, background: 'var(--paper-2)',
        fontFamily: "'JetBrains Mono', monospace",
        color: 'var(--ink)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between'
      }}>
          <span>mycpt.kr/a/9f7…</span>
          <span style={{ color: 'var(--accent)', fontFamily: 'Inter', fontWeight: 600 }}>복사</span>
        </div>
      </div>

      <div style={{ flex: 1 }} />

      <div className="btn-kakao">💬 카카오톡으로 공유</div>
      <div className="btn-secondary">URL 복사하기</div>
    </div>
  </SheetFrame>;


// B. Step 3 강조 — QR 큰 + 카카오 공유
const ShareB = () =>
<SheetFrame title="남이 보는 내 모습" step={3} totalSteps={3}>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1 }}>
      <div style={{
      fontFamily: "'Caveat', cursive", fontSize: 22, fontWeight: 700, color: 'var(--ink)',
      textAlign: 'center'
    }}>
        링크가 만들어졌어요!
      </div>
      <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', textAlign: 'center' }}>
        상대방의 호칭 · <b style={{ color: 'var(--ink)' }}>여자친구</b> · DISC 검사
      </div>

      {/* QR placeholder */}
      <div style={{
      margin: '0 auto', width: 180, height: 180,
      border: '1.5px solid var(--ink)', borderRadius: 14,
      background:
      'conic-gradient(from 0deg, var(--ink) 0deg 90deg, white 90deg 180deg, var(--ink) 180deg 270deg, white 270deg 360deg)',
      backgroundSize: '40px 40px',
      position: 'relative',
      display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}>
        <div style={{
        width: 50, height: 50, background: 'white',
        border: '2px solid var(--ink)', borderRadius: 8,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontFamily: "'JetBrains Mono', monospace", fontSize: 10
      }}>QR</div>
      </div>

      <div style={{
      textAlign: 'center', fontFamily: "'JetBrains Mono', monospace",
      fontSize: 11, color: 'var(--ink)',
      border: '1px dashed var(--line)', padding: '6px 10px',
      borderRadius: 6, background: 'var(--paper-2)'
    }}>mycpt.kr/a/9f7d2a</div>

      <div style={{ flex: 1 }} />

      <div className="btn-kakao">💬 카카오톡 공유 (추천)</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
        <div className="btn-secondary">URL 복사</div>
        <div className="btn-secondary">QR 저장</div>
      </div>
    </div>
  </SheetFrame>;


// C. 모달 헤더 모드 토글 — 검사/공유 모드를 시각적으로 분기 (동일 컴포넌트임을 보여줌)
const ShareC = ({ step = 1 }) => {
  const toggleDisabled = step >= 2;
  return (
  <div className="wf" style={{ background: 'var(--paper-2)' }}>
    <StatusBar />
    <div style={{
      height: 22, background: 'oklch(0.55 0.01 250 / 0.4)',
      borderBottom: '1px solid var(--line)', fontSize: 9,
      fontFamily: "'JetBrains Mono', monospace", color: 'white',
      display: 'flex', alignItems: 'center', paddingLeft: 14,
    }}>← 메인 (dimmed)</div>

    <div style={{
      flex: 1, background: 'white',
      borderTopLeftRadius: 18, borderTopRightRadius: 18,
      marginTop: -10,
      display: 'flex', flexDirection: 'column', overflow: 'hidden',
    }}>
      <div className="sheet-handle"></div>

      {/* Mode toggle as header */}
      <div style={{ padding: '6px 14px 8px', position: 'relative', opacity: toggleDisabled ? 0.5 : 1 }}>
        <div style={{
          display: 'grid', gridTemplateColumns: '1fr 1fr',
          border: '1.2px solid var(--ink)', borderRadius: 999,
          padding: 2, background: 'var(--paper-2)',
          fontSize: 11.5, fontWeight: 700,
          position: 'relative',
        }}>
          <div style={{ textAlign: 'center', padding: '7px 0', borderRadius: 999, color: 'var(--ink-soft)' }}>나는 누구일까?</div>
          <div style={{ textAlign: 'center', padding: '7px 0', borderRadius: 999, background: 'var(--ink)', color: 'var(--paper)' }}>남이 보는 내 모습</div>
          {toggleDisabled && (
            <div style={{
              position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)',
              fontSize: 11, color: 'var(--paper)',
            }}>🔒</div>
          )}
        </div>
        {!toggleDisabled && (
          <div style={{
            position: 'absolute', right: 14, top: -2,
            fontFamily: "'Caveat', cursive", fontSize: 13,
            color: 'var(--accent)', background: 'white',
            padding: '0 6px',
          }}>Step 1 전용 · Step 2부터 잠김</div>
        )}
      </div>

      <div style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '4px 14px 12px', borderBottom: '1px solid var(--line)',
      }}>
        <div className="close" style={{ width: 28, height: 28, border: '1.2px solid var(--ink)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14 }}>{step === 1 ? '✕' : '‹'}</div>
        <div className="stepdot">{`Step ${step}/3 · ${step === 1 ? '유형 선택' : step === 2 ? '상대방의 호칭' : '링크 공유'}`}</div>
        <div style={{ width: 28 }} />
      </div>

      {step === 1 && <ShareCStep1 />}
      {step === 2 && <ShareCStep2 />}
      {step === 3 && <ShareCStep3 />}
    </div>
  </div>);
};

const ShareCStep1 = () => (
  <div style={{ flex: 1, padding: 16, display: 'flex', flexDirection: 'column', gap: 10 }}>
    <div style={{ fontSize: 13, color: 'var(--ink-soft)' }}>
      어떤 검사 유형으로 친구에게 물어볼까요?
    </div>
    {[
      { t: 'DISC', s: '24문항 · 약 3분', ready: true, selected: true },
      { t: 'MBTI', s: '준비 중', ready: false, selected: false },
      { t: 'Big5', s: '준비 중', ready: false, selected: false },
    ].map((c, i) => (
      <div key={i} style={{
        border: '1.5px solid ' + (c.selected ? 'var(--ink)' : 'var(--line)'),
        borderRadius: 12, padding: '12px 14px',
        background: c.selected ? oklchHighlight() : 'white',
        opacity: c.ready ? 1 : 0.5,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <div>
          <div style={{ fontSize: 14, fontWeight: 700 }}>{c.t}</div>
          <div style={{ fontSize: 11, color: 'var(--ink-soft)' }}>{c.s}</div>
        </div>
        <div style={{
          width: 22, height: 22, borderRadius: '50%',
          border: '1.5px solid ' + (c.selected ? 'var(--ink)' : 'var(--line)'),
          background: c.selected ? 'var(--ink)' : 'white',
        }} />
      </div>
    ))}
    <div style={{ flex: 1 }} />
    <div className="btn-primary">다음 — 상대방의 호칭 입력 ›</div>
  </div>
);

const ShareCStep2 = () => (
  <div style={{ flex: 1, padding: 16, display: 'flex', flexDirection: 'column', gap: 14 }}>
    <div style={{ fontSize: 12.5, color: 'var(--ink-soft)' }}>
      선택한 검사 — <b style={{ color: 'var(--ink)' }}>DISC</b>
    </div>

    <div>
      <div style={{ fontSize: 13.5, color: 'var(--ink)', fontWeight: 700, marginBottom: 4, lineHeight: 1.4 }}>
        상대방을 어떻게 부르고 싶으세요?
      </div>
      <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', marginBottom: 8, lineHeight: 1.5 }}>
        입력한 호칭은 <b style={{ color: 'var(--ink)' }}>초대 메시지에 그대로 포함</b>되어요.
      </div>
      <div style={{
        border: '1.5px solid var(--ink)', borderRadius: 10,
        padding: '12px 14px', fontSize: 14, background: 'white',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <span>여자친구</span>
        <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 10, color: 'var(--ink-faint)' }}>4 / 20</span>
      </div>
      <div style={{
        marginTop: 10, padding: '10px 12px',
        background: 'var(--paper-2)', borderRadius: 8,
        border: '1px dashed var(--line)',
        fontSize: 11, color: 'var(--ink-soft)', lineHeight: 1.55,
        fontStyle: 'italic',
      }}>
        예시 · 『<b style={{ color: 'var(--ink)', fontStyle: 'normal' }}>여자친구</b>님이 보기에 OOO님은 어떤 사람인가요?』
      </div>
    </div>

    <div style={{ flex: 1 }} />
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 8 }}>
      <div className="btn-secondary">‹ 이전</div>
      <div className="btn-primary">링크 만들기 ›</div>
    </div>
  </div>
);

const ShareCStep3 = () => (
  <div style={{ flex: 1, padding: 16, display: 'flex', flexDirection: 'column', gap: 12 }}>
    <div style={{
      fontFamily: "'Caveat', cursive", fontSize: 22, fontWeight: 700, color: 'var(--ink)',
      textAlign: 'center', marginTop: 4,
    }}>
      링크가 만들어졌어요!
    </div>
    <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', textAlign: 'center' }}>
      <b style={{ color: 'var(--ink)' }}>여자친구</b>님에게 보낼 DISC 검사
    </div>

    {/* QR (B안 요소 통합) */}
    <div style={{
      margin: '4px auto 0', width: 140, height: 140,
      border: '1.5px solid var(--ink)', borderRadius: 12,
      background:
        'conic-gradient(from 0deg, var(--ink) 0deg 90deg, white 90deg 180deg, var(--ink) 180deg 270deg, white 270deg 360deg)',
      backgroundSize: '32px 32px',
      position: 'relative',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <div style={{
        width: 40, height: 40, background: 'white',
        border: '2px solid var(--ink)', borderRadius: 6,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontFamily: "'JetBrains Mono', monospace", fontSize: 9,
      }}>QR</div>
    </div>

    <div style={{
      border: '1.2px dashed var(--ink)', borderRadius: 10,
      padding: '10px 14px', background: 'var(--paper-2)',
      fontFamily: "'JetBrains Mono', monospace", fontSize: 11,
      color: 'var(--ink)',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    }}>
      <span>mycpt.kr/a/9f7d2a</span>
      <span style={{ color: 'var(--accent)', fontFamily: 'Inter', fontWeight: 700, fontSize: 12 }}>복사</span>
    </div>

    <div style={{
      padding: '8px 12px', borderRadius: 8,
      background: 'oklch(0.97 0.03 95)', border: '1px dashed var(--accent)',
      fontSize: 10.5, color: 'var(--ink-soft)', lineHeight: 1.5,
    }}>
      링크는 <b style={{ color: 'var(--ink)' }}>일회용</b>이에요. 응시가 완료되면 자동 만료됩니다.
    </div>

    <div style={{ flex: 1 }} />

    <div className="btn-kakao">💬 카카오톡으로 공유</div>
    <div className="btn-secondary">QR 이미지 저장</div>
  </div>
);

function oklchHighlight() {return 'oklch(0.96 0.04 95)';}

// ─────────────────────────────────────────────────────────
// Canvas root
// ─────────────────────────────────────────────────────────

const ABH = 970; // artboard height (frame 720 + caption + note)
const ABW = 380;

const Caption = ({ id, name, sub }) =>
<div className="variant-cap" style={{ marginTop: 0, marginBottom: 8 }}>
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
    <div style={{ display: 'flex', justifyContent: 'center' }}>
      {children}
    </div>
    <div className="variant-note">{note}</div>
  </div>;


const App = () =>
<DesignCanvas>
    <DCSection id="main" title="① 메인 화면" subtitle="P1 · 친숙어 4-CTA를 다양한 비율과 위계로 발산">
      <DCArtboard id="main-a" label="A · 알약 스택  ✓ 확정" width={ABW} height={ABH}>
        <Wrap id="A ✓" name="알약 스택" sub="첨부 모형 충실 재현 · 확정안"
      note={<><span className="label">✓ 확정안</span>4개를 동일 위계의 알약으로 세로 스택. <b>장점</b> 모형과 가장 가까워 구현 일관성 ↑. <b>단점</b> 시각적 위계가 약함 — 가장 자주 쓸 ① CTA와 보조 ④ CTA의 비중이 같음.</>}>
          <MainA />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="main-a-guest" label="A · 비회원 상태" width={ABW} height={ABH}>
        <Wrap id="A'" name="A · 비회원" sub="잠금 + 토스트 상태 시연"
      note={<><span className="label">잠금 패턴</span>② ③ pill은 <b>opacity 0.55 + 자물쇠 배지</b>, 탭바 ②③④도 자물쇠. 탭 시 하단 토스트로 카카오 로그인 유도. 우상단은 카카오 시작 버튼.</>}>
          <MainAGuest />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="main-b" label="B · 2×2 카드 그리드" width={ABW} height={ABH}>
        <Wrap id="B" name="2×2 카드 그리드" sub="일러스트 자리 명시"
      note={<><span className="label">레이아웃</span>2×2 그리드. 친숙어 + 작은 일러스트(placeholder). <b>장점</b> 시각적 풍부함, 추후 일러스트 자산을 받아 풍부한 톤 표현 가능. <b>단점</b> 텍스트 압축 필요(④는 줄여서 표기).</>}>
          <MainB />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="main-c" label="C · 영웅 + 보조 3" width={ABW} height={ABH}>
        <Wrap id="C" name="영웅 + 보조 3" sub="① 강조 + 나머지 작은 행"
      note={<><span className="label">레이아웃</span>가장 자주 쓸 ① "나는 누구일까?"를 큰 영웅 카드(일러스트 + CTA)로, 나머지 3개는 하단 작은 행. <b>장점</b> ① 진입 전환률 ↑, 명확한 위계. <b>단점</b> ② ③의 시인성이 낮아짐 — 회원 핵심 동선이 가려질 위험.</>}>
          <MainC />
        </Wrap>
      </DCArtboard>
    </DCSection>

    <DCSection id="test" title="② 검사 풀스크린 시트 — Step 2 응시 중" subtitle="P1 · DISC 24문항 / Most·Least 강제선택 UX 3안">
      <DCArtboard id="test-a" label="A · 진행바 + 4 카드" width={ABW} height={ABH}>
        <Wrap id="A" name="진행바 + 4 카드" sub="문항 카드에 Most/Least 칩"
      note={<><span className="label">UX</span>상단 선형 진행바 + 4개 옵션을 카드로 한 화면에 표시. 각 카드 우측에 [가까움]/[먼] 칩 토글. <b>장점</b> 가장 익숙한 패턴, 정보 밀도 높음. <b>단점</b> 정보가 빽빽함, 모바일에서 칩 터치 영역이 작아질 수 있음.</>}>
          <TestA />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="test-b" label="B · 카드 + dot indicator  ✓ 확정" width={ABW} height={ABH}>
        <Wrap id="B ✓" name="카드 + dot indicator" sub="두 영역으로 시각 분리 · 확정안"
      note={<><span className="label">✓ 확정안</span>상단에 24개 dot(현재 위치 강조). 가운데 큰 카드에 "가장 가까운" / "가장 먼" 두 영역을 라디오 형태로 분리. <b>예외 처리</b> "가장 가까운"에서 이미 선택한 옵션은 "가장 먼"에서 비활성(워터마크)으로 표시.</>}>
          <TestB />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="test-c" label="C · 드래그 슬롯" width={ABW} height={ABH}>
        <Wrap id="C" name="드래그 슬롯" sub="옵션 풀 → Most/Least 슬롯"
      note={<><span className="label">UX</span>옵션 풀에서 알약을 끌어 "가장 가까운" / "가장 먼" 슬롯으로 드롭. <b>장점</b> 강제선택의 의미(2개 중 1개씩만 가능)가 물리적으로 명확함, 게임적 재미. <b>단점</b> 24문항 × 드래그 부담, 접근성/터치 정확도 이슈 — 보조로 탭 지정도 제공 필요.</>}>
          <TestC />
        </Wrap>
      </DCArtboard>
    </DCSection>

    <DCSection id="test-extra" title="②-b 검사 시트 — Step 1 / Step 3 / 응시 중단" subtitle="P1 · Step 2(앞 섹션) 흐름의 앞뒤 + 닫기 다이얼로그">
      <DCArtboard id="test-step1" label="Step 1 · 유형 선택" width={ABW} height={ABH}>
        <Wrap id="Step 1" name="유형 선택" sub="overlay on /"
          note={<><span className="label">유형 선택</span>MVP에선 <b>DISC 카드 1개</b>만 선택된 상태로 노출. 카드에는 문항 수·소요 시간·접근 권한 태그. 하단에 추후 확장(MBTI·Big5) 안내. <b>자유롭게 닫기 가능</b> — Step 2부터는 닫기 시 다이얼로그.</>}>
          <TestStep1 />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="test-step3-guest" label="Step 3 · 결과 (비회원)" width={ABW} height={ABH}>
        <Wrap id="Step 3·G" name="결과 (비회원)" sub="저장 CTA로 회원 전환 유도"
          note={<><span className="label">회원 전환 깔때기</span>응시는 누구나, <b>상세·이력 저장은 회원 전용</b>. "한눈 요약"만 노출하고 <b>카카오 저장 CTA</b>가 화면 하단을 차지. "저장 없이 닫기" 보조 링크. 이 동선이 회원 전환의 1차 깔때기.</>}>
          <TestStep3Guest />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="test-step3-member" label="Step 3 · 결과 (회원)" width={ABW} height={ABH}>
        <Wrap id="Step 3·M" name="결과 (회원)" sub="자동 저장 + 다음 동선 3개"
          note={<><span className="label">완료 후 동선</span>회원은 자동 저장됨을 즉시 알리고(✓ 칩), <b>변화 알림</b>(지난 검사 대비 차이)을 한 줄 인사이트로 미리 노출. CTA 위계: <b>결과 상세</b>가 1순위, 그 아래 <b>친구 평정 / 동료 케미</b> 두 보조 행동.</>}>
          <TestStep3Member />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="test-close" label="응시 중단 다이얼로그" width={ABW} height={ABH}>
        <Wrap id="Dialog" name="응시 중단 확인" sub="Step 2 이후 ✕ 탭 시"
          note={<><span className="label">진행 손실 방지</span>응시 중 ✕ 탭하면 <b>중앙 다이얼로그</b>로 확인. 답한 문항 수를 명시해 손실의 크기를 정량화. <b>주 버튼 = "중단"(빨강)</b>, 보조 = "계속". Step 1에선 안 뜸.</>}>
          <TestCloseDialog />
        </Wrap>
      </DCArtboard>
    </DCSection>

    <DCSection id="share" title={'③ 공유 풀스크린 시트 — "남이 보는 내 모습"'} subtitle="P1 · 검사 모달 동일 컴포넌트 / Step만 분기">
      <DCArtboard id="share-a" label="A · 단일 화면 폼" width={ABW} height={ABH}>
        <Wrap id="A" name="단일 화면 폼" sub="유형 + 상대방 호칭 + 링크를 한 화면에"
      note={<><span className="label">패턴</span>Step 3에 1~3 단계를 압축. <b>상대방의 호칭</b>은 친숙어로, 그 호칭이 <b>초대 메시지에 포함</b>됨을 예시 문구로 명시. <b>장점</b> 한눈 정보 밀도. <b>단점</b> 검사 모달과 시각이 비슷해 "응시"와 혼동될 여지 — 제목으로 구분.</>}>
          <ShareA />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="share-b" label="B · QR + 카카오 강조" width={ABW} height={ABH}>
        <Wrap id="B" name="QR + 카카오 강조" sub="발급 완료 화면 단독"
      note={<><span className="label">패턴</span>Step 3은 "발급 완료"만 큼지막하게. QR + URL + 카카오톡 공유 버튼이 주인공. <b>장점</b> 공유 행위에 집중, QR로 오프라인 공유까지 자연스럽게 확장. <b>단점</b> Step 1~2는 검사 모달의 컴포넌트를 그대로 쓰는 형태가 되어 분기 시점이 명확해야 함.</>}>
          <ShareB />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="share-c" label="C · Step 1 유형 선택  ✓ 확정" width={ABW} height={ABH}>
        <Wrap id="C·1 ✓" name="헤더 모드 토글 — Step 1" sub="검사 ↔ 공유를 토글로 명시 · 확정안"
      note={<><span className="label">✓ 확정안</span>모달 헤더 자리에 <b>"나는 누구일까?" ↔ "남이 보는 내 모습"</b> 토글. 동일 컴포넌트임을 가장 명확히 시각화. <b>UX 보완</b> Step 1에서만 활성, <b>Step 2 이후는 잠금</b> — 응시 중 무심코 모드 전환으로 인한 진행 손실 방지.</>}>
          <ShareC step={1} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="share-c-2" label="C · Step 2 상대방의 호칭" width={ABW} height={ABH}>
        <Wrap id="C·2" name="헤더 모드 토글 — Step 2" sub="상대방의 호칭 입력 (초대 메시지에 포함)"
      note={<><span className="label">UX</span>호칭은 친숙어로 표기 — <b>"상대방을 어떻게 부르고 싶으세요?"</b>. 입력한 값이 <b>초대 메시지에 그대로 포함</b>됨을 예시 문구로 명시. 헤더 토글은 잠금(🔒).</>}>
          <ShareC step={2} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="share-c-3" label="C · Step 3 링크 공유" width={ABW} height={ABH}>
        <Wrap id="C·3" name="헤더 모드 토글 — Step 3" sub="링크 + 복사 + 카카오 공유"
      note={<><span className="label">완료</span>발급된 일회용 링크 + <b>복사</b> + <b>카카오톡으로 공유</b>. 토글은 계속 잠금 상태. 일회용/자동 만료 안내로 신뢰감 부여.</>}>
          <ShareC step={3} />
        </Wrap>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>;


ReactDOM.createRoot(document.getElementById('root')).render(<App />);