// P4 — 내 정보 / 마이페이지 (7화면 단일안)
// 허브 / 내 정보 수정 / 알림 센터 / 코인 / 인사이트 / 서비스 소개·약관 / 고객 문의·FAQ

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


const TabBar = ({ active = 3 }) => {
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

const WfFrame = ({ children, screenLabel, activeTab = 3, scrollable = true }) =>
<div className="wf" data-screen-label={screenLabel}>
    <StatusBar />
    <HeaderMember />
    <div className="wf-body" style={{ overflowY: scrollable ? 'auto' : 'hidden' }}>{children}</div>
    <TabBar active={activeTab} />
  </div>;


const BackBar = ({ title, sub, right }) =>
<div className="back-bar">
    <div className="back">‹</div>
    <div className="meta">
      <div className="title">{title}</div>
      {sub && <div className="sub">{sub}</div>}
    </div>
    {right || <div style={{ width: 28 }} />}
  </div>;


// ─────────────────────────────────────────────────────────
// 1. 마이페이지 허브 (/me)
// ─────────────────────────────────────────────────────────

const MeHub = () =>
<WfFrame screenLabel="P4 · 마이페이지 허브">
    {/* Big profile */}
    <div style={{
    background: 'white',
    padding: '24px 18px 22px',
    borderBottom: '1px solid var(--line)',
    display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12
  }}>
      <div style={{ position: 'relative' }}>
        <div style={{
        width: 110, height: 110, borderRadius: '50%',
        background: 'var(--paper-2)', border: '1.5px solid var(--ink)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: 'var(--ink-soft)', fontSize: 12, textAlign: 'center', lineHeight: 1.4
      }}>
          프로필<br />이미지
        </div>
        <div style={{
        position: 'absolute', right: -2, bottom: 4,
        width: 32, height: 32, borderRadius: '50%',
        background: 'var(--ink)', color: 'var(--paper)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: 13, border: '2px solid white'
      }}>✎</div>
      </div>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: 17, fontWeight: 800 }}>닉네임</div>
        <div style={{ fontSize: 11, color: 'var(--ink-faint)', marginTop: 2, fontFamily: "'JetBrains Mono', monospace" }}>kakao · 1992년생 · 여성</div>
      </div>
      <div style={{ display: 'flex', gap: 6 }}>
        <span className="type-pill type-d"><span className="dot"></span>D · 주도형 (내가 본 나)</span>
      </div>
    </div>

    {/* Menu rows */}
    <div className="section-label">내 정보</div>
    <div>
      <div className="row">
        <div className="icon">👤</div>
        <div className="text">
          <div className="label">내 정보 수정</div>
          <div className="meta">닉네임 · 생년 · 성별 · 프로필 이미지</div>
        </div>
        <div className="chevron">›</div>
      </div>
      <div className="row">
        <div className="icon">🔔</div>
        <div className="text">
          <div className="label">알림 센터</div>
          <div className="meta">케미 완료 · 동료 등록 등</div>
        </div>
        <span className="badge">3</span>
        <div className="chevron" style={{ marginLeft: 4 }}>›</div>
      </div>
      <div className="row">
        <div className="icon">⊙</div>
        <div className="text">
          <div className="label">코인 / 사용 이력</div>
          <div className="meta">잔량 2 · 다음 충전 23:14 후</div>
        </div>
        <div className="chevron">›</div>
      </div>
      <div className="row">
        <div className="icon">📊</div>
        <div className="text">
          <div className="label">통계 비교 · 변화 추이</div>
          <div className="meta">나이대·성별 평균 · 시간순 변화</div>
        </div>
        <div className="chevron">›</div>
      </div>
    </div>

    <div className="section-label">기타</div>
    <div>
      <div className="row">
        <div className="icon">ⓘ</div>
        <div className="text">
          <div className="label">서비스 소개 & 약관</div>
        </div>
        <div className="chevron">›</div>
      </div>
      <div className="row">
        <div className="icon">✉</div>
        <div className="text">
          <div className="label">고객 문의 / FAQ</div>
        </div>
        <div className="chevron">›</div>
      </div>
    </div>

    <div className="section-label">계정</div>
    <div>
      <div className="row">
        <div className="icon">↩</div>
        <div className="text">
          <div className="label" style={{ color: 'var(--ink-soft)' }}>로그아웃</div>
          <div className="meta">즉시 처리</div>
        </div>
        <div className="chevron">›</div>
      </div>
      <div className="row">
        <div className="icon" style={{ color: 'var(--danger)' }}>✕</div>
        <div className="text">
          <div className="label" style={{ color: 'var(--danger)' }}>회원탈퇴</div>
          <div className="meta">사유 입력 + 2단 확인</div>
        </div>
        <div className="chevron">›</div>
      </div>
    </div>
    <div style={{ height: 20 }} />
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 2. 내 정보 수정 (/me/profile)
// ─────────────────────────────────────────────────────────

const MeProfile = () =>
<WfFrame screenLabel="P4 · 내 정보 수정">
    <BackBar title="내 정보 수정" sub="필수 정보만" />

    <div style={{ padding: '16px 16px 0', display: 'flex', flexDirection: 'column', gap: 14 }}>
      {/* avatar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 14, background: 'white', border: '1px solid var(--line)', borderRadius: 12, padding: 14 }}>
        <div style={{
        width: 64, height: 64, borderRadius: '50%',
        background: 'var(--paper-2)', border: '1.2px solid var(--ink)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: 'var(--ink-soft)', fontSize: 10, textAlign: 'center'
      }}>프로필<br />이미지</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 12.5, fontWeight: 700, marginBottom: 4 }}>프로필 이미지</div>
          <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', lineHeight: 1.5 }}>jpg, png, webp · 최대 10MB</div>
        </div>
        <div className="btn-secondary" style={{ padding: '8px 14px', fontSize: 11 }}>변경</div>
      </div>

      {/* nickname */}
      <div>
        <div className="field-label">닉네임 <span className="req">*</span></div>
        <div className="field-input">
          <span>닉네임</span>
          <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 10, color: 'var(--ink-faint)' }}>3 / 20</span>
        </div>
        <div className="field-help">동료와 케미 보고서에 표시되는 이름이에요.</div>
      </div>

      {/* birth year */}
      <div>
        <div className="field-label">출생 연도 <span className="req">*</span></div>
        <div className="field-input">
          <span>1992</span>
          <span style={{ color: 'var(--ink-faint)', fontSize: 14 }}>▾</span>
        </div>
        <div className="field-help">통계 비교(20대/30대 평균)에 사용돼요.</div>
      </div>

      {/* gender */}
      <div>
        <div className="field-label">성별 <span className="req">*</span></div>
        <div className="seg" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
          <div className="opt">남성</div>
          <div className="opt active">여성</div>
          <div className="opt">선택 안 함</div>
        </div>
        <div className="field-help">통계에만 사용되고 다른 사용자에게 보이지 않아요.</div>
      </div>

      {/* notice */}
      <div style={{
      background: 'oklch(0.97 0.03 95)', border: '1px dashed var(--accent)',
      borderRadius: 10, padding: '10px 12px', fontSize: 11, color: 'var(--ink-soft)', lineHeight: 1.55
    }}>
        <b style={{ color: 'var(--ink)' }}>안내</b> · 생년·성별을 비워두면 <b style={{ color: 'var(--ink)' }}>통계 비교</b> 기능이 제한돼요.
      </div>

      <div style={{ height: 4 }} />
      <div className="btn-primary">저장</div>
      <div style={{ height: 24 }} />
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 3. 알림 센터 (/me/notifications) + 헤더 드롭다운 동일 데이터
// ─────────────────────────────────────────────────────────

const NOTIFS = [
{ id: 1, kind: 'chem-done', t: '민지님과의 케미 보고서가 도착했어요', d: '방금 전', unread: true, icon: '🤝' },
{ id: 2, kind: 'invite', t: '도윤님이 동료로 등록을 요청했어요', d: '2시간 전', unread: true, icon: '👥' },
{ id: 3, kind: 'other-result', t: '여자친구님이 보낸 평정 결과가 도착했어요', d: '5시간 전', unread: true, icon: '🪞' },
{ id: 4, kind: 'coin', t: '코인 1개가 충전됐어요', d: '어제', unread: false, icon: '⊙' },
{ id: 5, kind: 'chem-done', t: '하준님과의 케미 보고서가 도착했어요', d: '2일 전', unread: false, icon: '🤝' },
{ id: 6, kind: 'system', t: '새로운 검사 유형(MBTI)이 곧 추가돼요', d: '1주 전', unread: false, icon: 'ⓘ' }];


const MeNotifications = () =>
<WfFrame screenLabel="P4 · 알림 센터">
    <BackBar title="알림" sub={`읽지 않음 ${NOTIFS.filter((n) => n.unread).length}건`}
  right={<div style={{ fontSize: 10.5, color: 'var(--ink-soft)', textDecoration: 'underline' }}>모두 읽음</div>} />
  

    <div style={{ background: 'white' }}>
      {NOTIFS.map((n) =>
    <div key={n.id} className="row" style={{
      background: n.unread ? 'oklch(0.98 0.02 95)' : 'white'
    }}>
          <div className="icon">{n.icon}</div>
          <div className="text">
            <div className="label" style={{ fontWeight: n.unread ? 700 : 500, fontSize: 12.5, lineHeight: 1.4 }}>{n.t}</div>
            <div className="meta">{n.d}</div>
          </div>
          {n.unread && <span style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--accent)', flexShrink: 0 }}></span>}
        </div>
    )}
    </div>

    <div style={{
    textAlign: 'center', padding: '20px 0',
    fontSize: 11, color: 'var(--ink-faint)',
    fontFamily: "'JetBrains Mono', monospace"
  }}>· 30일 이전 알림은 자동 삭제 ·</div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 4. 코인 / 사용 이력 (/me/coins)
// ─────────────────────────────────────────────────────────

const COIN_HISTORY = [
{ id: 1, kind: 'use', label: '민지님과의 케미 보고서', date: '2026.05.18 14:22', amount: -1 },
{ id: 2, kind: 'refill', label: '자동 충전', date: '2026.05.17 23:14', amount: +1 },
{ id: 3, kind: 'use', label: '도윤님과의 케미 보고서', date: '2026.05.16 09:11', amount: -1 },
{ id: 4, kind: 'refill', label: '자동 충전', date: '2026.05.15 23:14', amount: +1 },
{ id: 5, kind: 'refill', label: '신규 가입 보너스', date: '2026.05.01 00:00', amount: +3 }];


const MeCoins = () =>
<WfFrame screenLabel="P4 · 코인">
    <BackBar title="코인" sub="케미 1회 = 1 코인" />

    {/* Hero — current balance */}
    <div style={{
    background: 'white',
    padding: '22px 18px 20px',
    borderBottom: '1px solid var(--line)',
    textAlign: 'center'
  }}>
      <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: 8 }}>현재 잔량</div>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'center', gap: 6 }}>
        <span style={{ fontSize: 56, fontWeight: 800, color: 'var(--accent)', lineHeight: 1, fontFamily: "'JetBrains Mono', monospace" }}>2</span>
        <span style={{ fontSize: 18, color: 'var(--ink-soft)', fontWeight: 600 }}>/ 3</span>
      </div>
      {/* slots viz */}
      <div style={{ display: 'flex', gap: 8, justifyContent: 'center', marginTop: 14 }}>
        {[true, true, false].map((filled, i) =>
      <div key={i} style={{
        width: 38, height: 38, borderRadius: '50%',
        border: '1.5px solid var(--ink)',
        background: filled ? 'var(--accent)' : 'var(--paper-2)',
        color: filled ? 'white' : 'var(--ink-faint)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: 16, fontWeight: 700
      }}>⊙</div>
      )}
      </div>
      <div style={{
      marginTop: 14, fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.5
    }}>
        다음 충전까지 <b style={{ color: 'var(--ink)', fontFamily: "'JetBrains Mono', monospace" }}>23 : 14 : 02</b>
      </div>
    </div>

    {/* info */}
    <div style={{
    background: 'oklch(0.97 0.03 95)', borderBottom: '1px dashed var(--accent)',
    padding: '12px 16px', fontSize: 11, color: 'var(--ink-soft)', lineHeight: 1.55
  }}>
      <b style={{ color: 'var(--ink)' }}>안내</b> · 코인은 매일 자정에 1개씩 자동 충전되며, 최대 3개까지 보관됩니다. 케미 보고서 1회 발행에 1 코인이 사용돼요.
    </div>

    {/* history */}
    <div className="section-label">사용 이력</div>
    <div style={{ background: 'white' }}>
      {COIN_HISTORY.map((h) =>
    <div key={h.id} className="row">
          <div className="icon" style={{
        background: h.amount > 0 ? 'var(--member-bg)' : 'var(--accent-2)',
        color: h.amount > 0 ? 'var(--member)' : 'var(--accent)',
        fontWeight: 700, fontFamily: "'JetBrains Mono', monospace"
      }}>{h.amount > 0 ? '+' : '−'}</div>
          <div className="text">
            <div className="label" style={{ fontSize: 12.5 }}>{h.label}</div>
            <div className="meta">{h.date}</div>
          </div>
          <div style={{
        fontFamily: "'JetBrains Mono', monospace", fontWeight: 700,
        fontSize: 14, color: h.amount > 0 ? 'var(--member)' : 'var(--accent)'
      }}>{h.amount > 0 ? `+${h.amount}` : h.amount}</div>
        </div>
    )}
    </div>
    <div style={{ height: 20 }} />
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 5. 통계 비교 · 변화 추이 (/me/insights)
// ─────────────────────────────────────────────────────────

const COMPARE_DATA = {
  D: { me: 85, avg: 62 },
  I: { me: 60, avg: 68 },
  S: { me: 40, avg: 55 },
  C: { me: 70, avg: 60 }
};

const DISC = {
  D: { name: '주도', color: 'var(--disc-d)' },
  I: { name: '사교', color: 'var(--disc-i)' },
  S: { name: '안정', color: 'var(--disc-s)' },
  C: { name: '신중', color: 'var(--disc-c)' }
};

const MeInsights = () =>
<WfFrame screenLabel="P4 · 인사이트">
    <BackBar title="통계 비교 · 변화 추이" />

    {/* Test type selector — extensible to MBTI/Big5 in future */}
    <div style={{
    background: 'white', borderBottom: '1px solid var(--line)',
    padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 10
  }}>
      <span style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", whiteSpace: 'nowrap', letterSpacing: '0.04em', textTransform: 'uppercase' }}>⎵ 검사 종류</span>
      <div style={{
      display: 'inline-flex', gap: 4, padding: 3,
      background: 'var(--paper-2)', border: '1px solid var(--line)',
      borderRadius: 999
    }}>
        <div style={{
        padding: '5px 14px', borderRadius: 999,
        background: 'var(--ink)', color: 'var(--paper)',
        fontSize: 11.5, fontWeight: 700
      }}>DISC</div>
      </div>
      <span style={{ fontSize: 10, color: 'var(--ink-faint)', fontStyle: 'italic' }}></span>
    </div>

    <div className="page-tabs">
      <div className="pt active">비교</div>
      <div className="pt">추이</div>
    </div>

    {/* Filter chips */}
    <div style={{
    background: 'white', borderBottom: '1px solid var(--line)',
    padding: '12px 16px', display: 'flex', gap: 8, overflowX: 'auto', alignItems: 'center'
  }}>
      <span style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", whiteSpace: 'nowrap', marginRight: 2 }}>비교 대상</span>
      {[
    { t: '전체 평균', active: false },
    { t: '30대 여성', active: true },
    { t: '30대', active: false },
    { t: '여성', active: false }].
    map((c, i) =>
    <div key={i} style={{
      padding: '5px 12px', borderRadius: 999,
      border: '1.2px solid ' + (c.active ? 'var(--ink)' : 'var(--line)'),
      background: c.active ? 'var(--ink)' : 'white',
      color: c.active ? 'var(--paper)' : 'var(--ink)',
      fontSize: 11.5, fontWeight: 600, whiteSpace: 'nowrap'
    }}>{c.t}</div>
    )}
    </div>

    {/* Chart card */}
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
      <div style={{
      background: 'white', border: '1.5px solid var(--ink)',
      borderRadius: 14, padding: '16px 18px'
    }}>
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 14 }}>
          <div style={{ fontSize: 13, fontWeight: 700 }}>나 vs <span style={{ color: 'var(--accent)' }}>30대 여성</span></div>
          <div style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>n=12,847</div>
        </div>

        {['D', 'I', 'S', 'C'].map((k) => {
        const { me, avg } = COMPARE_DATA[k];
        const diff = me - avg;
        return (
          <div key={k} style={{ marginBottom: 14 }}>
              <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 6 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{
                  fontFamily: "'JetBrains Mono', monospace", fontSize: 13, fontWeight: 700,
                  color: DISC[k].color
                }}>{k}</span>
                  <span style={{ fontSize: 11, color: 'var(--ink-soft)' }}>{DISC[k].name}</span>
                </div>
                <div style={{ fontSize: 11, color: 'var(--ink-soft)', fontFamily: "'JetBrains Mono', monospace" }}>
                  <b style={{ color: 'var(--ink)' }}>{me}</b> · 평균 {avg}
                  <span style={{
                  marginLeft: 6,
                  color: diff > 0 ? 'var(--accent)' : diff < 0 ? 'var(--member)' : 'var(--ink-faint)',
                  fontWeight: 700
                }}>{diff > 0 ? `▲${diff}` : diff < 0 ? `▼${-diff}` : '—'}</span>
                </div>
              </div>
              <div style={{ position: 'relative', height: 8, background: 'var(--paper-3)', borderRadius: 4 }}>
                {/* avg marker line */}
                <div style={{
                position: 'absolute', left: `${avg}%`, top: -3, bottom: -3,
                width: 2, background: 'var(--ink-faint)',
                borderRadius: 1
              }}></div>
                <div style={{
                height: '100%', width: `${me}%`,
                background: DISC[k].color, borderRadius: 4, opacity: 0.85
              }}></div>
              </div>
            </div>);

      })}

        <div style={{
        marginTop: 6, paddingTop: 12, borderTop: '1px dashed var(--line)',
        fontSize: 11, color: 'var(--ink-soft)', lineHeight: 1.55
      }}>
          <b style={{ color: 'var(--ink)' }}>인사이트</b> · 30대 여성 평균 대비 <b style={{ color: 'var(--disc-d)' }}>D(주도)</b>가 23점 높고, <b style={{ color: 'var(--disc-s)' }}>S(안정)</b>이 15점 낮아요. 빠른 의사결정 성향이 두드러집니다.
        </div>
      </div>
    </div>
    <div style={{ height: 8 }} />
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 6. 서비스 소개 & 약관 (/me/about)
// ─────────────────────────────────────────────────────────

const MeAbout = () =>
<WfFrame screenLabel="P4 · 서비스 소개 & 약관">
    <BackBar title="서비스 소개 & 약관" />

    {/* Hero */}
    <div style={{
    background: 'white', padding: '24px 20px', borderBottom: '1px solid var(--line)'
  }}>
      <div style={{ fontFamily: "'Caveat', cursive", fontSize: 30, fontWeight: 700 }}>MyCPT</div>
      <div style={{ fontSize: 12, color: 'var(--ink-soft)', marginTop: 4, lineHeight: 1.6 }}>
        나의 성향을 알아보고, 친구와 동료의 시선과 비교해 보세요.
        DISC 24문항을 기반으로 한 친숙한 성향 검사 서비스예요.
      </div>
    </div>

    {/* Features */}
    <div className="section-label">이런 걸 할 수 있어요</div>
    <div style={{ background: 'white' }}>
      {[
    { i: '①', t: '나는 누구일까?', s: '24문항 DISC 검사 · 누구나 무료' },
    { i: '②', t: '남이 보는 내 모습', s: '친구에게 일회용 링크로 평정 요청' },
    { i: '③', t: '우리 잘 맞을까?', s: '동료와의 케미 보고서 발행 (코인 1)' },
    { i: '④', t: '통계 비교 · 변화 추이', s: '연령대·성별 평균과 시간순 변화 비교' }].
    map((f, i) =>
    <div key={i} className="row">
          <div className="icon" style={{
        background: 'var(--accent-2)', color: 'var(--accent)',
        fontFamily: "'Caveat', cursive", fontSize: 18, fontWeight: 700
      }}>{f.i}</div>
          <div className="text">
            <div className="label" style={{ fontSize: 13 }}>{f.t}</div>
            <div className="meta">{f.s}</div>
          </div>
        </div>
    )}
    </div>

    <div className="section-label">약관 / 정책</div>
    <div style={{ background: 'white' }}>
      {[
    '서비스 이용약관',
    '개인정보 처리방침',
    '면책 조항 — 참고용 성향 분석, 심리 진단 아님',
    '오픈소스 라이선스'].
    map((t, i) =>
    <div key={i} className="row">
          <div className="text">
            <div className="label" style={{ fontSize: 13, fontWeight: 500 }}>{t}</div>
          </div>
          <div className="chevron">›</div>
        </div>
    )}
    </div>

    <div style={{
    padding: '20px 16px 24px', textAlign: 'center',
    fontSize: 10.5, color: 'var(--ink-faint)',
    fontFamily: "'JetBrains Mono', monospace", lineHeight: 1.6
  }}>
      MyCPT v0.6 · 2026.05<br />
      © 2026 MyCPT. All rights reserved.
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 7. 고객 문의 / FAQ (/me/help)
// ─────────────────────────────────────────────────────────

const FAQS = [
{ q: '코인은 어떻게 충전되나요?', a: '매일 자정에 1개씩 자동 충전되며, 최대 3개까지 보관할 수 있어요. 별도의 결제는 없습니다.', open: true },
{ q: '검사 결과를 친구에게 공유해도 안전한가요?', a: '"남이 보는 내 모습"은 일회용 링크로 발급되며, 응시가 완료되면 자동 만료됩니다.', open: false },
{ q: '동료를 삭제하면 케미 보고서도 사라지나요?', a: '동료 관계는 끊어지지만, 이미 발행된 케미 보고서는 본인의 이력에 남아 있어요.', open: false },
{ q: '왜 출생연도와 성별을 입력해야 하나요?', a: '연령대·성별 평균과 비교하는 통계 기능에만 사용되며, 다른 사용자에게는 보이지 않아요.', open: false },
{ q: '검사 결과가 매번 조금씩 다른데 정상인가요?', a: '성향은 시기와 상황에 따라 자연스럽게 변동돼요. 변화 추이 화면에서 흐름을 확인해 보세요.', open: false }];


const MeHelp = () =>
<WfFrame screenLabel="P4 · 고객 문의 / FAQ">
    <BackBar title="고객 문의 / FAQ" />

    {/* Contact card */}
    <div style={{ padding: '16px 16px 0' }}>
      <div style={{
      background: 'white', border: '1.5px solid var(--ink)',
      borderRadius: 14, padding: 16
    }}>
        <div style={{ fontSize: 13, fontWeight: 700, marginBottom: 4 }}>도움이 필요하신가요?</div>
        <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.5, marginBottom: 12 }}>
          평일 10:00 ~ 18:00 응대 · 1~2 영업일 내 회신
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          <div className="btn-secondary" style={{ fontSize: 12, padding: '10px 0' }}>💬 카카오 채널</div>
          <div className="btn-secondary" style={{ fontSize: 12, padding: '10px 0' }}>✉ 이메일 문의</div>
        </div>
        <div style={{ marginTop: 10, fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", textAlign: 'center' }}>
          help@mycpt.kr
        </div>
      </div>
    </div>

    <div className="section-label">자주 묻는 질문</div>
    <div>
      {FAQS.map((f, i) =>
    <div key={i} className="faq">
          <div className="q">
            <span>Q. {f.q}</span>
            <span className="ico">{f.open ? '▴' : '▾'}</span>
          </div>
          {f.open && <div className="a">{f.a}</div>}
        </div>
    )}
    </div>
    <div style={{ height: 24 }} />
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 8. 인사이트 — 추이 탭 (P6 통합)
// ─────────────────────────────────────────────────────────

const DISC_TREND_COLORS = {
  D: 'var(--disc-d)', I: 'var(--disc-i)', S: 'var(--disc-s)', C: 'var(--disc-c)',
};
const DISC_TREND_NAMES = { D: '주도', I: '사교', S: '안정', C: '신중' };

const TrendChart = () => {
  const w = 280, h = 140;
  const data = [
    { D: 60, I: 55, S: 70, C: 65, date: '12.10' },
    { D: 65, I: 60, S: 65, C: 65, date: '02.14' },
    { D: 70, I: 65, S: 55, C: 70, date: '03.20' },
    { D: 78, I: 65, S: 45, C: 68, date: '04.02' },
    { D: 85, I: 60, S: 40, C: 70, date: '05.20' },
  ];
  const n = data.length;
  const xStep = w / (n - 1);
  const yFor = (v) => h - v / 100 * h;
  const buildPath = (k) =>
    data.map((p, i) => `${i === 0 ? 'M' : 'L'} ${(i * xStep).toFixed(1)} ${yFor(p[k]).toFixed(1)}`).join(' ');
  return (
    <svg width="100%" viewBox={`0 0 ${w} ${h + 18}`} preserveAspectRatio="none" style={{ display: 'block' }}>
      {[0, 25, 50, 75, 100].map((v) =>
        <line key={v} x1="0" x2={w} y1={yFor(v)} y2={yFor(v)} stroke="var(--line-soft)" strokeWidth="0.5" strokeDasharray="2 3" />
      )}
      {['D', 'I', 'S', 'C'].map((k) =>
        <g key={k}>
          <path d={buildPath(k)} fill="none" stroke={DISC_TREND_COLORS[k]} strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />
          {data.map((p, i) =>
            <circle key={i} cx={i * xStep} cy={yFor(p[k])} r={i === n - 1 ? 3.5 : 2.2} fill={DISC_TREND_COLORS[k]} stroke="white" strokeWidth="1" />
          )}
        </g>
      )}
      {data.map((p, i) =>
        <text key={i} x={i * xStep} y={h + 14} textAnchor="middle" fontSize="9" fill="var(--ink-faint)" fontFamily="JetBrains Mono">{p.date}</text>
      )}
    </svg>
  );
};

const TypePillMini = ({ type }) => (
  <span className={"type-pill type-" + type.toLowerCase()} style={{ fontSize: 10, padding: '2px 7px' }}>
    <span className="dot"></span>
    <span>{type} · {DISC_TREND_NAMES[type]}형</span>
  </span>
);

const MeInsightsTrend = () =>
<WfFrame screenLabel="P4 · 인사이트 · 추이 탭">
    <BackBar title="통계 비교 · 변화 추이" />

    {/* test type */}
    <div style={{ background: 'white', borderBottom: '1px solid var(--line)', padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 10 }}>
      <span style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.04em', textTransform: 'uppercase' }}>⎵ 검사 종류</span>
      <div style={{ display: 'inline-flex', padding: 3, background: 'var(--paper-2)', border: '1px solid var(--line)', borderRadius: 999 }}>
        <div style={{ padding: '5px 14px', borderRadius: 999, background: 'var(--ink)', color: 'var(--paper)', fontSize: 11.5, fontWeight: 700 }}>DISC</div>
      </div>
    </div>

    <div className="page-tabs">
      <div className="pt">비교</div>
      <div className="pt active">추이</div>
    </div>

    {/* range filter */}
    <div style={{ background: 'white', borderBottom: '1px solid var(--line)', padding: '12px 16px', display: 'flex', gap: 8, overflowX: 'auto' }}>
      <span style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", whiteSpace: 'nowrap' }}>기간</span>
      {[
        { t: '최근 3개월', active: false },
        { t: '최근 6개월', active: true },
        { t: '최근 1년', active: false },
        { t: '전체', active: false },
      ].map((c, i) =>
        <div key={i} style={{
          padding: '5px 12px', borderRadius: 999,
          border: '1.2px solid ' + (c.active ? 'var(--ink)' : 'var(--line)'),
          background: c.active ? 'var(--ink)' : 'white',
          color: c.active ? 'var(--paper)' : 'var(--ink)',
          fontSize: 11.5, fontWeight: 600, whiteSpace: 'nowrap',
        }}>{c.t}</div>
      )}
    </div>

    {/* chart */}
    <div style={{ padding: 16 }}>
      <div className="trend-card">
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 10 }}>
          <div style={{ fontSize: 13, fontWeight: 700 }}>4축 변화 추이</div>
          <div style={{ fontSize: 10, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>5회 응시</div>
        </div>
        <TrendChart />
        <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap', marginTop: 10, paddingTop: 10, borderTop: '1px dashed var(--line)' }}>
          {['D', 'I', 'S', 'C'].map((k) =>
            <div key={k} style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--ink-soft)' }}>
              <span style={{ width: 12, height: 3, background: DISC_TREND_COLORS[k], borderRadius: 2 }}></span>
              <span style={{ fontFamily: "'JetBrains Mono', monospace", fontWeight: 700, color: 'var(--ink)' }}>{k}</span>
              <span>{DISC_TREND_NAMES[k]}</span>
            </div>
          )}
        </div>
      </div>

      <div style={{
        marginTop: 14, background: 'white', border: '1px dashed var(--accent)',
        borderRadius: 10, padding: '12px 14px',
        fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.6,
      }}>
        <b style={{ color: 'var(--ink)' }}>인사이트</b> · 최근 6개월간 <b style={{ color: 'var(--disc-d)' }}>D</b>가 25점 상승, <b style={{ color: 'var(--disc-s)' }}>S</b>가 30점 하락. <b style={{ color: 'var(--ink)' }}>더 빠르고 결단력 있는 방향</b>으로 성향이 이동했어요.
      </div>

      <div style={{ marginTop: 14 }}>
        <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: 8 }}>응시 기록</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {[
            { date: '2026.05.20', top: 'D', note: '최근' },
            { date: '2026.04.02', top: 'D', note: null },
            { date: '2026.03.20', top: 'D', note: null },
            { date: '2026.02.14', top: 'I', note: '유형 전환' },
            { date: '2025.12.10', top: 'S', note: '첫 검사' },
          ].map((m, i) =>
            <div key={i} style={{
              background: 'white', border: '1px solid var(--line-soft)',
              borderRadius: 8, padding: '8px 12px',
              display: 'flex', alignItems: 'center', gap: 10,
            }}>
              <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 10.5, color: 'var(--ink-faint)', width: 70 }}>{m.date}</span>
              <TypePillMini type={m.top} />
              {m.note && <span style={{ fontSize: 10, color: 'var(--accent)', marginLeft: 'auto', fontStyle: 'italic' }}>{m.note}</span>}
            </div>
          )}
        </div>
      </div>
    </div>
    <div style={{ height: 14 }} />
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 9. 회원탈퇴 Step 1 — 사유 선택
// ─────────────────────────────────────────────────────────

const LeaveStep1 = () =>
<WfFrame screenLabel="P4 · 회원탈퇴 Step 1 사유">
    <BackBar title="회원탈퇴" sub="Step 1 / 2 · 사유 선택" />

    <div style={{ padding: '18px 16px 0' }}>
      <div style={{ fontFamily: 'Inter', fontSize: 17, fontWeight: 800, marginBottom: 8, letterSpacing: '-0.2px' }}>떠나기 전에, 알려주세요</div>
      <div style={{ fontSize: 12, color: 'var(--ink-soft)', lineHeight: 1.6, marginBottom: 18 }}>
        더 나은 서비스를 만드는 데 큰 도움이 돼요. 익명으로 처리됩니다.
      </div>

      <div style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: 8 }}>탈퇴 사유 (복수 선택)</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 16 }}>
        {[
          { t: '필요한 기능이 없어요', c: false },
          { t: '검사 결과가 정확하지 않은 것 같아요', c: true },
          { t: '자주 쓰지 않게 됐어요', c: true },
          { t: '개인 정보가 걱정돼요', c: false },
          { t: '새로운 계정으로 다시 시작하고 싶어요', c: false },
          { t: '기타', c: false },
        ].map((r, i) =>
          <div key={i} className={"check-row" + (r.c ? ' checked' : '')}>
            <div className="box">{r.c ? '✓' : ''}</div>
            <span>{r.t}</span>
          </div>
        )}
      </div>

      <div style={{ fontSize: 11, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: 8 }}>자세히 알려주기 (선택)</div>
      <div className="field-input text-area">예: 검사 결과가 실제 나와 다르게 나오는 것 같아요…</div>

      <div style={{
        marginTop: 18, background: 'oklch(0.97 0.04 28)', border: '1px solid var(--danger)',
        borderRadius: 10, padding: '12px 14px',
        fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.6,
      }}>
        <div style={{ fontWeight: 700, color: 'var(--danger)', marginBottom: 4 }}>⚠ 탈퇴 시 함께 삭제돼요</div>
        <ul style={{ margin: 0, paddingLeft: 16 }}>
          <li>검사 결과 5회 · 케미 보고서 4건</li>
          <li>동료 관계 5명 (상대방 목록에서도 사라져요)</li>
          <li>코인 잔량 · 사용 이력</li>
        </ul>
        <div style={{ marginTop: 6, color: 'var(--danger)', fontWeight: 600 }}>한 번 삭제된 데이터는 복구할 수 없습니다.</div>
      </div>

      <div style={{ height: 16 }} />
      <div className="btn-secondary">취소하고 돌아가기</div>
      <div style={{ height: 8 }} />
      <div className="btn-danger">다음 — 최종 확인 ›</div>
      <div style={{ height: 20 }} />
    </div>
  </WfFrame>;


// ─────────────────────────────────────────────────────────
// 10. 회원탈퇴 Step 2 — 최종 확인 다이얼로그
// ─────────────────────────────────────────────────────────

const LeaveStep2 = () =>
<div className="wf" data-screen-label="P4 · 회원탈퇴 Step 2 확인">
    <StatusBar />
    <HeaderMember />
    <div className="wf-body" style={{ position: 'relative' }}>
      <div style={{ opacity: 0.35, padding: 16, fontSize: 11 }}>
        <div className="back-bar" style={{ padding: 0, border: 'none', background: 'transparent', marginBottom: 14 }}>
          <div className="back">‹</div>
          <div className="meta"><div className="title">회원탈퇴</div></div>
        </div>
        <div>사유 선택…</div>
      </div>

      <div className="scrim">
        <div className="dialog-card">
          <div className="icon">🚪</div>
          <h3 style={{ color: 'var(--danger)' }}>정말로 탈퇴할까요?</h3>
          <p>
            <b style={{ color: 'var(--ink)' }}>닉네임</b>님의 모든 데이터(결과 5회·케미 4건·동료 5명)가 <b style={{ color: 'var(--danger)' }}>영구 삭제</b>됩니다.<br />
            진행하려면 아래에 <b style={{ color: 'var(--ink)' }}>"탈퇴할게요"</b>를 입력해 주세요.
          </p>

          <div className="field-input" style={{
            background: 'var(--paper-2)', borderColor: 'var(--line)',
            color: 'var(--ink-faint)', fontSize: 12.5, padding: '10px 12px', marginBottom: 14,
          }}>탈퇴할게요</div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div className="btn-danger">탈퇴 진행</div>
            <div className="btn-secondary">계정 유지하기</div>
          </div>
        </div>
      </div>
    </div>
    <TabBar active={3} />
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
    <DCSection id="me-hub" title="내 정보 — 허브" subtitle="P4 · /me · 헤더 칩 / 하단 탭 첫 항목 진입">
      <DCArtboard id="hub" label="① 마이페이지 허브" width={ABW} height={ABH}>
        <Wrap id="·" name="마이페이지 허브" sub="/me"
      note={<><span className="label">구조</span>상단 <b>큰 프로필 이미지</b>(✎ 펜으로 즉시 변경) + 닉네임 + 대표 유형. 본문은 <b>3개 섹션</b>(내 정보 / 기타 / 계정)의 리스트 계층. <b>로그아웃은 즉시</b>, <b>회원탈퇴는 2단 확인</b>으로 분리해 위계를 시각화 (탈퇴는 빨강).</>}>
          <MeHub />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="profile" label="② 내 정보 수정" width={ABW} height={ABH}>
        <Wrap id="·" name="내 정보 수정" sub="/me/profile"
      note={<><span className="label">폼</span>프로필 이미지 / 닉네임 / 출생 연도 / 성별 — 네 항목만. 각 필드의 <b>help text</b>로 "이게 어디에 쓰이는지"를 명시해 입력 동기를 부여(예: 통계 비교). 성별은 세그먼트로 빠른 선택.</>}>
          <MeProfile />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="notifs" label="③ 알림 센터" width={ABW} height={ABH}>
        <Wrap id="·" name="알림 센터" sub="/me/notifications"
      note={<><span className="label">패턴</span>읽지 않은 알림은 <b>옅은 노란 배경</b> + 우측 빨간 점. 클릭 시 이동 + 즉시 삭제(=읽음 처리 후 사라짐). 우상단 "모두 읽음"으로 일괄 처리. 30일 이전은 자동 삭제 안내. 헤더 드롭다운은 동일 데이터를 짧게 보여줌.</>}>
          <MeNotifications />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="coins" label="④ 코인 / 사용 이력" width={ABW} height={ABH}>
        <Wrap id="·" name="코인 / 사용 이력" sub="/me/coins"
      note={<><span className="label">시각화</span>잔량을 <b>큰 숫자 + 3슬롯 시각화</b>로 즉시 파악. 다음 충전까지 카운트다운. 사용 이력은 <b>+/−</b> 아이콘 + 색상으로 충전/사용을 구분. "케미 1회 = 1 코인" 안내 박스.</>}>
          <MeCoins />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="insights" label="⑤ 통계 비교" width={ABW} height={ABH}>
        <Wrap id="·" name="통계 비교 · 변화 추이" sub="/me/insights · 비교 탭"
      note={<><span className="label">검사 종류 선택</span>상단에 <b>검사 종류 토글</b>(현재 DISC만, MVP 단계에서는 준비 중 항목 미노출). 추후 MBTI / Big5가 추가되면 같은 자리에 알약 형태로 자연스럽게 확장. <b>차트</b>는 <b>비교 대상 필터 칩</b>(전체 / 30대 여성 / 30대 / 여성)으로 모집단 선택. 각 축마다 막대 + <b>평균선 마커</b> + ▲▼ 차이 표시. 하단 인사이트 문장은 가장 큰 편차를 자연어로 설명.</>}>
          <MeInsights />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="about" label="⑥ 서비스 소개 & 약관" width={ABW} height={ABH}>
        <Wrap id="·" name="서비스 소개 & 약관" sub="/me/about · 공용"
      note={<><span className="label">콘텐츠</span>메인 ④ CTA의 시트와 동일 콘텐츠를 풀페이지로. 4개 기능 소개 + 약관/정책 리스트 + 면책 조항 + 버전 정보. <b>공용 접근</b>이라 비회원 헤더 상태로도 접근 가능.</>}>
          <MeAbout />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="help" label="⑦ 고객 문의 / FAQ" width={ABW} height={ABH}>
        <Wrap id="·" name="고객 문의 / FAQ" sub="/me/help"
      note={<><span className="label">구조</span>상단 <b>응대 시간 + 카카오·이메일</b> 카드 → 아래 FAQ 아코디언. 첫 번째 항목은 펼친 상태로 시작해 "탭하면 펼쳐진다"는 시그널을 자동 노출.</>}>
          <MeHelp />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="insights-trend" label="⑤-b 인사이트 · 추이 탭" width={ABW} height={ABH}>
        <Wrap id="·" name="추이 탭" sub="/me/insights · 시간순 4축 라인 차트"
      note={<><span className="label">시계열</span>4축 라인 차트(D/I/S/C)로 변화 시각화. <b>각 점은 응시 지점</b>(JetBrains Mono 라벨로 날짜). 기간 필터 칩(최근 3/6/12개월/전체)로 줌. 차트 하단에 자연어 인사이트, 그 아래 <b>응시 기록 리스트</b> — 유형 전환 등 마일스톤은 캡션으로 표시.</>}>
          <MeInsightsTrend />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="leave-1" label="⑧ 회원탈퇴 Step 1 · 사유" width={ABW} height={ABH}>
        <Wrap id="·" name="회원탈퇴 Step 1" sub="사유 + 삭제 안내"
      note={<><span className="label">사유 수집</span>익명 처리됨을 명시한 <b>복수선택 체크 리스트</b> + 자유 입력(선택). <b>삭제 항목을 정량으로 미리 노출</b>(검사 5회·케미 4건·동료 5명·코인 등) — "사라지는 것"을 구체적으로 인지시켜 신중한 결정을 유도. 빨강 영역으로 위험 신호.</>}>
          <LeaveStep1 />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="leave-2" label="⑨ 회원탈퇴 Step 2 · 최종 확인" width={ABW} height={ABH}>
        <Wrap id="·" name="회원탈퇴 Step 2" sub="입력형 최종 확인"
      note={<><span className="label">치명적 액션</span>중앙 다이얼로그로 <b>"탈퇴할게요"를 직접 입력</b>해야 진행 — 단순 버튼 탭으로 인한 실수 방지(GitHub 스타일). 닉네임으로 1인칭 호명 + 삭제 항목 재확인. <b>주 버튼 = 빨강</b>, 보조는 "계정 유지하기".</>}>
          <LeaveStep2 />
        </Wrap>
      </DCArtboard>
    </DCSection>
  </DesignCanvas>;


ReactDOM.createRoot(document.getElementById('root')).render(<App />);