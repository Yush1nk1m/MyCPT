# MyCPT — 권한·접근 매트릭스 (Access Matrix)

> 화면·기능별 인증 상태에 따른 접근 권한 정리. 미들웨어 / RSC 가드 / 클라이언트 분기에 사용.

**version**: 0.1
**관련 화면**: `specs/screens.yaml`

---

## 인증 상태 정의

| 상태            | 식별                                 | 비고                                |
| --------------- | ------------------------------------ | ----------------------------------- |
| `anonymous`     | accessToken 쿠키 없음 또는 토큰 만료 | 비로그인 일반 사용자                |
| `authenticated` | 유효한 JWT + `/auth/me` 200          | 카카오 로그인 완료한 회원           |
| `assessor`      | URL 토큰 (`/assessments/[token]`)    | 외부 평정자. 인증 무관, 토큰만 검증 |

---

## 화면 접근 매트릭스

✅ 접근 가능 · ⛔ 차단 · 🔒 잠금 표시 후 토스트 · → 자동 리다이렉트

| 화면                                      | anonymous            | authenticated      | assessor |
| ----------------------------------------- | -------------------- | ------------------ | -------- |
| `main` (/)                                | ✅                   | ✅                 | ✅       |
| `test-sheet-step1`                        | ✅                   | ✅                 | ⛔       |
| `test-sheet-step2`                        | ✅                   | ✅                 | ⛔       |
| `test-sheet-step3-guest`                  | ✅                   | ⛔                 | ⛔       |
| `test-sheet-step3-member`                 | ⛔                   | ✅                 | ⛔       |
| `share-sheet-*`                           | 🔒                   | ✅                 | ⛔       |
| `about-sheet`                             | ✅                   | ✅                 | ✅       |
| `assessment-intro` (/assessments/[token]) | ✅                   | ✅                 | ✅       |
| `kakao-callback` (/auth/kakao/callback)   | ✅                   | ✅                 | ⛔       |
| `results-list` (/results)                 | 🔒 → main            | ✅                 | ⛔       |
| `result-detail` (/results/[id])           | → /auth/kakao        | ✅ (본인 결과만)   | ⛔       |
| `peers-list` (/colleagues)                | 🔒 → main            | ✅                 | ⛔       |
| `chemistry-list` (/chemistry)             | 🔒 → main            | ✅                 | ⛔       |
| `peer-detail` (/colleagues/[id])          | → /auth/kakao        | ✅ (본인 동료만)   | ⛔       |
| `peer-register-modal`                     | ⛔                   | ✅                 | ⛔       |
| `chemistry-confirm-modal`                 | ⛔                   | ✅                 | ⛔       |
| `chemistry-detail` (/chemistry/[id])      | → /auth/kakao        | ✅ (본인 보고서만) | ⛔       |
| `invite-accept` (/invite/[code])          | ✅ (로그인 유도)     | ✅                 | ⛔       |
| `me-hub` (/me)                            | 🔒 → main            | ✅                 | ⛔       |
| `me-profile` (/me/profile)                | → /auth/kakao        | ✅                 | ⛔       |
| `me-notifications` (/me/notifications)    | → /auth/kakao        | ✅                 | ⛔       |
| `notif-dropdown` (헤더 종)                | — (헤더 자체 미노출) | ✅                 | ⛔       |
| `me-coins` (/me/coins)                    | → /auth/kakao        | ✅                 | ⛔       |
| `me-insights-*` (/me/insights)            | → /auth/kakao        | ✅                 | ⛔       |
| `me-about` (/me/about)                    | ✅                   | ✅                 | ⛔       |
| `me-help` (/me/help)                      | ✅                   | ✅                 | ⛔       |
| `me-leave-*`                              | ⛔                   | ✅                 | ⛔       |

---

## 기능 단위 접근 권한

| 기능                                | 권한                                                          |
| ----------------------------------- | ------------------------------------------------------------- |
| DISC 검사 응시                      | 누구나 (anonymous + authenticated)                            |
| 검사 결과 저장                      | authenticated only                                            |
| 검사 결과 이력 조회                 | authenticated, 본인 것만                                      |
| 친구에게 평정 요청 (공유 링크 생성) | authenticated only                                            |
| 외부 평정 응시                      | 토큰 보유자 (assessor) — 인증 무관                            |
| 동료 등록 / 삭제                    | authenticated only                                            |
| 케미 보고서 발행                    | authenticated only + 코인 ≥ 1                                 |
| 케미 보고서 열람                    | authenticated only, 본인이 발행자 또는 대상자일 때만          |
| 통계 비교 / 추이                    | authenticated only + 생년·성별 입력됨                         |
| 알림 수신 (SSE)                     | authenticated only                                            |
| 코인 사용                           | authenticated only                                            |
| 카카오 로그인                       | anonymous only (이미 로그인된 상태로 진입 시 / 로 리다이렉트) |

---

## 비회원 잠금 동작 상세

비회원이 회원 전용에 접근하는 4가지 경로와 동작:

### 1. 회원 전용 CTA 탭 (예: 메인 ② "남이 보는 내 모습")

- 즉시 `LockedToast` 표시 (검정 배경, 카카오 액션 포함)
- 토스트 카피: `🔒 로그인이 필요해요`
- 액션 버튼: `카카오로 시작` (노란 배경)
- 액션 탭 → `/auth/kakao?returnTo=<원래 의도한 화면>`

### 2. 회원 전용 탭 탭 (예: 검사 결과 탭)

- 위와 동일

### 3. 회원 전용 URL 직접 입력 (예: `/me`)

- **Next.js 미들웨어**가 가로채고 `/auth/kakao?returnTo=/me`로 리다이렉트
- 로그인 성공 후 `/me`로 자동 이동

### 4. 회원 전용 API 호출 (예: 컴포넌트에서 `GET /coins`)

- `401 Unauthorized` 응답
- React Query 글로벌 onError에서 토스트 + 자동 로그아웃 처리

---

## 본인 데이터 vs 타인 데이터

다음 화면들은 **본인 데이터만** 조회 가능:

- `/results/[id]` — 본인이 응시했거나 본인이 평정 요청한 결과
- `/colleagues/[id]` — 본인이 등록한 동료
- `/chemistry/[id]` — 본인이 발행했거나 본인이 대상자인 보고서

→ API 레벨에서 검증 (403 Forbidden). 클라이언트는 결과를 그대로 표시하면 됨.

---

## 외부 평정자 (assessor) 흐름

외부 평정자는 카톡 링크의 `token`만으로 접근:

1. `GET /assessments/{token}` — 토큰 유효성 + 평정 대상자 정보 응답
2. 응시 화면(`assessment-intro` → `test-sheet-step2` 변형, rater=OTHER)
3. `POST /results/score` — 응답 제출 (with token)
4. 토큰 자동 만료

**중요**: 외부 평정자는 회원·비회원 무관. 본인이 회원이어도 그 세션 정보 사용 안 함. 토큰만으로 식별.

---

## Next.js 미들웨어 가이드라인

```ts
// middleware.ts 의사 코드
export function middleware(req) {
  const token = req.cookies.get("accessToken")?.value;
  const path = req.nextUrl.pathname;

  // 공용 경로
  if (PUBLIC_ROUTES.includes(path)) return;

  // 회원 전용 경로 + 비로그인
  if (MEMBER_ROUTES.some((p) => path.startsWith(p)) && !token) {
    return NextResponse.redirect(`/auth/kakao?returnTo=${path}`);
  }
}

const PUBLIC_ROUTES = ["/", "/about", "/me/about", "/me/help"];
const MEMBER_ROUTES = ["/me", "/results", "/colleagues", "/chemistry"];
// /assessments/[token]은 별도 (토큰 검증은 페이지에서)
```
