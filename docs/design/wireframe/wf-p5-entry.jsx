// P5 — 진입 화면 (3화면)
// 타인 평정 응시 외부 진입 / 서비스 소개 시트(메인 ④) / 카카오 콜백 인터스티셜

const StatusBar = () => (
  <div className="wf-status">
    <span>9:41</span>
    <span>● ● ● ● ●  100%</span>
  </div>
);

const Avatar = ({ name, size = 40 }) => (
  <div className="avatar-circle" style={{
    width: size, height: size, fontSize: size * 0.4,
    background: 'oklch(0.97 0.03 80)',
  }}>{name.slice(0, 1)}</div>
);

// ─────────────────────────────────────────────────────────
// 1. 타인 평정 응시 외부 진입 (/assessments/[token])
//    카톡 링크를 받아 비회원이 들어옴 → 인트로 → 응시 → 종료
// ─────────────────────────────────────────────────────────

const AssessmentIntro = () => (
  <div className="wf" data-screen-label="P5 타인 평정 · 인트로">
    <StatusBar />
    {/* minimal header — no tab bar in assessment context */}
    <div style={{
      height: 44, flexShrink: 0,
      borderBottom: '1px solid var(--line)',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 14px', background: 'white',
    }}>
      <div style={{ fontFamily: "'Caveat', cursive", fontSize: 20, fontWeight: 700 }}>MyCPT</div>
      <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace" }}>외부 진입 · 로그인 불필요</div>
    </div>

    <div className="wf-body" style={{ background: 'var(--paper-2)', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
      {/* hero */}
      <div style={{
        background: 'white', padding: '28px 22px 24px',
        borderBottom: '1px solid var(--line)', textAlign: 'center',
      }}>
        <div style={{ fontFamily: "'Caveat', cursive", fontSize: 22, fontWeight: 700, color: 'var(--accent)', marginBottom: 14 }}>
          평정 요청이 도착했어요!
        </div>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
          <Avatar name="현우" size={56} />
          <div style={{ textAlign: 'left' }}>
            <div style={{ fontSize: 16, fontWeight: 800 }}>현우</div>
            <div style={{ fontSize: 11, color: 'var(--ink-faint)' }}>님이 보낸 요청</div>
          </div>
        </div>
        <div style={{
          fontSize: 14, color: 'var(--ink)', lineHeight: 1.55,
          padding: '14px 16px', background: 'oklch(0.97 0.03 95)',
          border: '1px dashed var(--accent)', borderRadius: 10,
        }}>
          『<b>여자친구</b>님이 보기에<br/>현우님은 어떤 사람인가요?』
        </div>
      </div>

      {/* what to do */}
      <div style={{ padding: '18px 18px 0', flex: 1, display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div style={{ fontSize: 10.5, color: 'var(--ink-faint)', fontFamily: "'JetBrains Mono', monospace", letterSpacing: '0.06em', textTransform: 'uppercase' }}>응시 방법</div>
        {[
          { i: '①', t: '24개의 짧은 문항을 풀어요', s: '약 3분 소요' },
          { i: '②', t: '각 문항마다 가장 가까운 것과 먼 것 하나씩', s: '현우님의 시선에서 답해주세요' },
          { i: '③', t: '결과는 현우님에게 자동 전송돼요', s: '평정자의 개인 정보는 저장되지 않아요' },
        ].map((b, i) => (
          <div key={i} style={{
            background: 'white', border: '1px solid var(--line)', borderRadius: 10,
            padding: '12px 14px', display: 'flex', gap: 12, alignItems: 'flex-start',
          }}>
            <span style={{
              fontFamily: "'Caveat', cursive", fontSize: 22, fontWeight: 700,
              color: 'var(--accent)', lineHeight: 1, marginTop: 2,
            }}>{b.i}</span>
            <div>
              <div style={{ fontSize: 12.5, fontWeight: 700 }}>{b.t}</div>
              <div style={{ fontSize: 10.5, color: 'var(--ink-soft)', marginTop: 2 }}>{b.s}</div>
            </div>
          </div>
        ))}
      </div>

      {/* footer CTA */}
      <div style={{
        background: 'white', borderTop: '1px solid var(--line)',
        padding: '14px 18px 18px',
      }}>
        <div className="btn-primary">시작하기 (24문항 · 약 3분)</div>
        <div style={{
          textAlign: 'center', fontSize: 10, color: 'var(--ink-faint)',
          marginTop: 8, lineHeight: 1.5,
        }}>
          이 링크는 <b style={{ color: 'var(--ink-soft)' }}>일회용</b>이에요. 응시 완료 시 자동 만료됩니다.<br/>
          참고용 성향 분석, 심리 진단 아닙니다.
        </div>
      </div>
    </div>
  </div>
);

// ─────────────────────────────────────────────────────────
// 2. 서비스 소개 시트 (메인 ④ "이 서비스는 무엇인가요?")
//    풀스크린 시트, 캐러셀
// ─────────────────────────────────────────────────────────

const SLIDES = [
  {
    idx: 1,
    ko: '나의 성향을 알아보고,\n남이 보는 내 모습과 비교해 보세요',
    cap: 'DISC 24문항 기반 친숙어 성향 검사',
    color: 'oklch(0.96 0.04 95)',
    illust: '소개 일러스트 #1',
  },
  {
    idx: 2,
    ko: '친구에게 일회용 링크로\n나를 평가받아 봐요',
    cap: '"남이 보는 내 모습" — 회원 전용',
    color: 'oklch(0.96 0.05 30)',
    illust: '소개 일러스트 #2',
  },
  {
    idx: 3,
    ko: '동료와의 케미 보고서로\n협업의 결을 살펴봐요',
    cap: '"우리 잘 맞을까?" — 코인 1회',
    color: 'oklch(0.95 0.05 150)',
    illust: '소개 일러스트 #3',
  },
  {
    idx: 4,
    ko: '연령대·성별 평균과 비교,\n변화 추이도 확인해요',
    cap: '데이터로 보는 나의 변화',
    color: 'oklch(0.95 0.05 240)',
    illust: '소개 일러스트 #4',
  },
];

const AboutSheet = ({ slideIdx = 2 }) => {
  const slide = SLIDES[slideIdx - 1];
  return (
    <div className="wf" style={{ background: 'var(--paper-2)' }} data-screen-label={`P5 서비스 소개 시트 · ${slideIdx}/4`}>
      <StatusBar />
      {/* dimmed peek of main */}
      <div style={{
        height: 22, background: 'oklch(0.55 0.01 250 / 0.4)',
        borderBottom: '1px solid var(--line)', fontSize: 9,
        fontFamily: "'JetBrains Mono', monospace", color: 'white',
        display: 'flex', alignItems: 'center', paddingLeft: 14,
      }}>← 메인 (dimmed)</div>

      <div style={{
        flex: 1, background: 'white',
        borderTopLeftRadius: 22, borderTopRightRadius: 22,
        marginTop: -10, display: 'flex', flexDirection: 'column', overflow: 'hidden',
      }}>
        <div className="sheet-handle"></div>
        <div className="sheet-head">
          <div className="close">✕</div>
          <div className="title">이 서비스는 무엇인가요?</div>
          <div className="stepdot">{slideIdx} / 4</div>
        </div>

        {/* slide body */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '18px 22px 16px', gap: 16, overflow: 'hidden' }}>
          <div style={{
            flex: 1,
            background: slide.color,
            border: '1.5px solid var(--ink)',
            borderRadius: 16,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: 'var(--ink-soft)', fontFamily: "'JetBrains Mono', monospace", fontSize: 11,
            position: 'relative',
            backgroundImage: `repeating-linear-gradient(45deg, transparent 0 14px, oklch(1 0 0 / 0.35) 14px 15px)`,
          }}>
            <div style={{
              padding: '6px 14px', background: 'white',
              border: '1px dashed var(--line)', borderRadius: 999,
            }}>{slide.illust}</div>
          </div>

          <div style={{ minHeight: 110 }}>
            <div style={{
              fontFamily: "'Inter'", fontSize: 20, fontWeight: 800,
              lineHeight: 1.35, letterSpacing: '-0.3px',
              whiteSpace: 'pre-line',
            }}>{slide.ko}</div>
            <div style={{ fontSize: 12.5, color: 'var(--ink-soft)', marginTop: 8, lineHeight: 1.5 }}>{slide.cap}</div>
          </div>

          {/* dots + nav */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div className="dots">
              {SLIDES.map((_, i) => (
                <div key={i} className={"d" + (i === slideIdx - 1 ? " active" : "")}></div>
              ))}
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <div style={{
                width: 36, height: 36, borderRadius: '50%',
                border: '1.2px solid var(--line)', background: 'white',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 16, color: 'var(--ink-soft)',
              }}>‹</div>
              <div style={{
                width: 36, height: 36, borderRadius: '50%',
                background: 'var(--ink)', color: 'var(--paper)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 16,
              }}>›</div>
            </div>
          </div>
        </div>

        {/* footer — legal links */}
        <div style={{
          borderTop: '1px dashed var(--line)',
          padding: '12px 22px 16px', background: 'var(--paper-2)',
          display: 'flex', justifyContent: 'space-between',
          fontSize: 10.5, color: 'var(--ink-soft)',
          fontFamily: "'JetBrains Mono', monospace",
        }}>
          <span>이용약관</span>
          <span>개인정보처리방침</span>
          <span>문의</span>
        </div>
      </div>
    </div>
  );
};

// ─────────────────────────────────────────────────────────
// 3. 카카오 콜백 인터스티셜 (/auth/kakao/callback)
//    토큰 교환 중 보여주는 짧은 로더 화면
// ─────────────────────────────────────────────────────────

const KakaoCallback = () => (
  <div className="wf" data-screen-label="P5 카카오 콜백 · 인터스티셜">
    <StatusBar />
    <div className="wf-body" style={{
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      gap: 24, padding: 24, background: 'white',
    }}>
      {/* logo lockup */}
      <div style={{
        fontFamily: "'Caveat', cursive", fontSize: 44, fontWeight: 700,
        color: 'var(--ink)', letterSpacing: '-0.5px',
      }}>MyCPT</div>

      {/* kakao + spinner */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
        <div style={{
          width: 48, height: 48, borderRadius: 12,
          background: '#FEE500', border: '1.2px solid var(--ink)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 22,
        }}>💬</div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          {[0, 1, 2].map((i) => (
            <span key={i} style={{
              width: 6, height: 6, borderRadius: '50%',
              background: 'var(--ink-faint)',
              animation: `dot 1.4s ${i * 0.2}s infinite ease-in-out`,
            }}></span>
          ))}
        </div>

        <div style={{
          width: 48, height: 48, borderRadius: '50%',
          border: '2.5px solid var(--accent-2)',
          borderTopColor: 'var(--accent)',
          animation: 'spin 1s linear infinite',
        }}></div>
      </div>

      {/* text */}
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 6 }}>카카오 로그인 처리 중…</div>
        <div style={{ fontSize: 11.5, color: 'var(--ink-soft)', lineHeight: 1.5 }}>
          잠시만 기다려주세요.<br/>
          자동으로 이전 화면으로 돌아갑니다.
        </div>
      </div>

      {/* fine print */}
      <div style={{
        position: 'absolute', bottom: 24, left: 24, right: 24,
        fontSize: 10, color: 'var(--ink-faint)',
        fontFamily: "'JetBrains Mono', monospace",
        textAlign: 'center', lineHeight: 1.6,
      }}>
        /auth/kakao/callback?code=…&state=…<br/>
        if redirect doesn't happen, tap below
      </div>

      <div style={{
        position: 'absolute', bottom: 56, left: 24, right: 24,
      }}>
        <div className="btn-secondary" style={{ fontSize: 12, padding: '10px 0', opacity: 0.6 }}>처음으로 돌아가기</div>
      </div>
    </div>
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
    <DCSection id="entry" title="진입 화면 — 외부에서 들어오는 입구" subtitle="P5 · 타인 평정 외부 진입 / 서비스 소개 시트 / 카카오 콜백">

      <DCArtboard id="assessment-intro" label="① 타인 평정 외부 진입" width={ABW} height={ABH}>
        <Wrap id="·" name="타인 평정 응시 · 인트로" sub="/assessments/[token]"
          note={<><span className="label">외부 진입</span>카톡으로 받은 일회용 링크의 첫 화면 — <b>탭바·로그인 없음</b>. 헤더는 로고와 "외부 진입 · 로그인 불필요" 라벨만. 초대자 카드 + 친숙어 메시지("『여자친구』님이 보기에 OOO님은 어떤 사람인가요?") + 응시 방법 3단계 + 시작 CTA. 일회용 안내와 면책 조항을 푸터에 명시.</>}>
          <AssessmentIntro />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="about-1" label="② 서비스 소개 시트 · 1/4" width={ABW} height={ABH}>
        <Wrap id="·" name="서비스 소개 시트" sub="메인 ④ CTA · slide 1"
          note={<><span className="label">캐러셀</span>메인 위에 풀스크린 시트로 오버레이(좌측 dimmed strip이 메인의 흔적). 4장 캐러셀 — 일러스트 + 큰 한국어 문장 + 캡션. dot 인디케이터는 현재 위치를 막대형으로 늘려 시각적 위치 단서를 강화. 첫 장은 검사 소개로 시작.</>}>
          <AboutSheet slideIdx={1} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="about-2" label="③ 서비스 소개 시트 · 2/4" width={ABW} height={ABH}>
        <Wrap id="·" name="서비스 소개 시트" sub="slide 2 · 친구에게 공유"
          note={<><span className="label">변형</span>같은 레이아웃, 다른 색상 — 슬라이드마다 배경 색조를 살짝 다르게 해서(노랑 → 주황 → 초록 → 파랑) 페이지 이동의 리듬감을 만듦. 일러스트는 placeholder.</>}>
          <AboutSheet slideIdx={2} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="about-4" label="④ 서비스 소개 시트 · 4/4 (마지막)" width={ABW} height={ABH}>
        <Wrap id="·" name="서비스 소개 시트" sub="slide 4 · 마지막 페이지"
          note={<><span className="label">마지막</span>4번째에서는 우측 ›가 '마침'으로 바뀌고 탭 시 시트 닫힘. 푸터의 이용약관·개인정보처리방침·문의 링크는 모든 슬라이드에 고정.</>}>
          <AboutSheet slideIdx={4} />
        </Wrap>
      </DCArtboard>

      <DCArtboard id="kakao-callback" label="⑤ 카카오 콜백 인터스티셜" width={ABW} height={ABH}>
        <Wrap id="·" name="카카오 콜백" sub="/auth/kakao/callback"
          note={<><span className="label">인터스티셜</span>카카오 인증 후 토큰 교환 중 1~2초간 보이는 짧은 화면. <b>탭바·헤더 없음</b>(앱이 아직 어디로 갈지 모르는 상태). 로고 + 카카오 아이콘 + 점/스피너로 진행감 표현. 자동 리디렉트가 실패할 경우를 대비해 흐릿한 "처음으로 돌아가기" 보조 버튼.</>}>
          <KakaoCallback />
        </Wrap>
      </DCArtboard>

    </DCSection>
  </DesignCanvas>
);

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
