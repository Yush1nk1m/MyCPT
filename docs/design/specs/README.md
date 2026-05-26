# MyCPT — Frontend Specs

> 프론트엔드 코드 생성·구현을 위한 머신 리더블 명세 모음.
> 와이어프레임은 시각, 이 폴더는 정형 명세. 둘은 짝.

**version**: 0.1
**last updated**: 2026-05-27

---

## 파일 구성

| 파일                | 용도                                                  | 형식            |
| ------------------- | ----------------------------------------------------- | --------------- |
| `screens.yaml`      | 화면 명세 (라우트·인증·API·컴포넌트·상태)             | YAML            |
| `components.yaml`   | 재사용 컴포넌트 인터페이스 (props·variant·사용처)     | YAML / Markdown |
| `state-machines.md` | 비동기·시간 기반 상태 전이도 (Mermaid)                | Markdown        |
| `empty-states.md`   | 빈/에러/특수 상태별 UI 가이드                         | Markdown        |
| `access-matrix.md`  | 권한·접근 매트릭스 (anonymous/authenticated/assessor) | Markdown        |
| `tokens.css`        | 디자인 토큰 통합 (색·간격·반경·폰트)                  | CSS             |

---

## 함께 보면 좋은 문서

- `/와이어프레임 P*.html` — 시각 명세
- `/IA - 페이지 구조 도출 v3.html` — 정보 구조 (탭·라우트·모달 6종 매핑)
- `/docs/api-design.md` — API 요청·응답 스키마 (요청 바디·응답 필드 정의)
- `/docs/database-design.md` — DB 스키마·ERD·DBML
- `/docs/service-design.md` — 비즈니스 로직·정책
- `/docs/requirements-design.md` — 요구사항

---

## 사용 시나리오

### "이 화면 한 개를 코드로 구현해 줘" → Claude에게

1. 해당 와이어프레임 캡처(또는 P\*.html#artboard-id 링크)
2. `specs/screens.yaml`에서 해당 screen 블록
3. 그 블록의 `components`에 나온 컴포넌트 정의(`components.yaml`)
4. 관련 상태 머신(`state-machines.md`)
5. 사용할 API(`docs/api-design.md`)

→ 이 5개를 컨텍스트로 주면 1발에 일관된 코드.

### "전체 디자인 시스템을 셋업해 줘" → Claude에게

1. `tokens.css`
2. `components.yaml`의 Layout / Atoms / Patterns 섹션

### "권한 미들웨어 짜줘"

1. `access-matrix.md`
2. `docs/api-design.md`의 인증 섹션

---

## 다음 보강 후보 (현재 명세에서 빠진 것)

| 항목                                       | 우선순위 | 비고                                      |
| ------------------------------------------ | -------- | ----------------------------------------- |
| 폼 검증 규칙 통합 (`validation.yaml`)      | 🟡       | 닉네임·이미지·코드 등 흩어진 규칙 한 곳에 |
| 에러 코드 ↔ UI 메시지 매핑 (`errors.yaml`) | 🟡       | API 에러 코드별 토스트/다이얼로그 카피    |
| 데이터 바인딩 (`data-binding.md`)          | 🟢       | 와이어프레임 텍스트 ↔ API 응답 필드       |
| 분석 이벤트 정의 (`analytics.yaml`)        | 🟢       | 트래킹 이벤트                             |
| 접근성 가이드 (`a11y.md`)                  | 🟢       | 키보드 흐름, ARIA                         |
| 반응형 정책 (`responsive.md`)              | 🟢       | 데스크톱 진입 시                          |

→ 위 6개는 **개발 중 즉석 처리해도 OK**한 항목들. 사전 명세보다 코드에 직접 반영하는 게 효율적.

---

## 컨벤션

- ID: `kebab-case` (예: `peer-register-modal`)
- 컴포넌트 이름: `PascalCase` (예: `PeerRegisterModal`)
- 라우트: 절대 경로 (`/colleagues/[id]`)
- API: `METHOD /path` (예: `GET /results/{id}`)
- 상태: `snake_case` (예: `generating`, `ready`, `deleted_by_peer`)
