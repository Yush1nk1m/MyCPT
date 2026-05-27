# MyCPT — 상태 머신 명세 (State Machines)

> 비동기 흐름·시간 기반 상태 전이를 명시. 와이어프레임에 텍스트로 산재된 상태 전이 정보를 한 곳에 모은 SSOT.

**version**: 0.1
**관련 화면**: `specs/screens.yaml`
**관련 API**: `docs/api-design.md`

---

## 1. 케미 보고서 (Chemistry Report)

가장 복잡한 비동기 흐름. AI 본문 생성에 30초~1분 걸려, 발행 즉시 UI에 자리를 잡고 SSE 완료 이벤트로 갱신됨.

```mermaid
stateDiagram-v2
    [*] --> idle: 동료 상세 진입
    idle --> confirming: 케미 발행 CTA 탭
    confirming --> idle: 취소
    confirming --> generating: 발행 확인 (POST /chemistry-reports 202)
    generating --> ready: SSE chemistry.ready
    generating --> error: SSE chemistry.error (or timeout 5분)
    error --> [*]: 코인 자동 환불
    ready --> [*]: 사용자 보고서 열람
```

| 상태         | UI 표현                                                                                              | 사용자 인터랙션                              |
| ------------ | ---------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| `idle`       | 일반 동료 상세 화면. 발행 CTA 활성 (코인 ≥ 1)                                                        | 발행 CTA 탭                                  |
| `confirming` | `chemistry-confirm-modal` 다이얼로그                                                                 | 확인/취소                                    |
| `generating` | chemistry-list에 점선 테두리 카드 + 스피너. chemistry-detail 진입 시 hero(두 아바타) + skeleton 본문 | 닫고 다른 화면 이동 가능, 알림 받으면 돌아옴 |
| `ready`      | chemistry-list 카드에 NEW 뱃지. detail 본문 마크다운 렌더                                            | 열람                                         |
| `error`      | 토스트 "발행에 실패했어요. 코인이 환불되었어요." + 재시도 버튼                                       | 재시도                                       |

**예외 처리**

- `generating` 중 동료가 탈퇴 → 발행 취소, 코인 환불, 토스트 안내
- 5분 타임아웃 시 자동 `error`
- 동시 발행 제한: 같은 동료에 대해 `generating`이 있으면 새 발행 차단

---

## 2. 코인 (Coin Balance)

```mermaid
stateDiagram-v2
    [*] --> full: 신규 가입 (잔량 3)
    full --> partial: 케미 발행 (-1)
    partial --> partial: 케미 발행 (-1)
    partial --> empty: 잔량 0
    empty --> refilling: 자정 도래 대기
    partial --> refilling_partial: 자정 도래 대기 (이미 일부 사용)
    refilling --> partial: 자정 → +1
    refilling_partial --> partial: 자정 → +1
    refilling --> full: +1 후 잔량 3 도달
```

| 상태      | 잔량 | UI 표현                                                     |
| --------- | ---- | ----------------------------------------------------------- |
| `full`    | 3    | CoinPill 타이머 "00:00" (충전 불필요), 코인 슬롯 3/3 채워짐 |
| `partial` | 1~2  | CoinPill 타이머 "23:14" 등 카운트다운, 슬롯 일부 빔         |
| `empty`   | 0    | 케미 발행 CTA 비활성 + 다음 충전 시간 안내                  |

**규칙**

- 매일 **자정 (KST 00:00)**에 1개 충전
- 최대 보관 3개
- 케미 발행 실패 시 자동 환불

---

## 3. 외부 평정 토큰 (Assessment Token)

```mermaid
stateDiagram-v2
    [*] --> created: POST /assessments
    created --> intro: 평정자 링크 접속
    intro --> answering: 시작하기 탭
    answering --> consumed: 응시 완료 (POST /results/score)
    answering --> abandoned: 미완료로 이탈
    abandoned --> answering: 재진입 (만료 전)
    created --> expired: 7일 경과
    consumed --> [*]
    expired --> [*]
```

| 상태        | UI 표현                                                                   |
| ----------- | ------------------------------------------------------------------------- |
| `created`   | 평정자에게 카톡으로 전달된 직후                                           |
| `intro`     | `assessment-intro` 화면 (인사 + 응시 방법)                                |
| `answering` | `test-sheet-step2`와 동일 화면 (rater=OTHER)                              |
| `consumed`  | "감사합니다" 종료 화면. 같은 링크 재접근 시 "이미 응시가 완료된 링크예요" |
| `expired`   | "초대장이 만료됐어요" 안내                                                |

**규칙**

- 토큰 1개 = 1회용
- 만료 기간 7일 (TBD — docs/api-design.md 확인 필요)

---

## 4. 인증 상태 (Auth)

```mermaid
stateDiagram-v2
    [*] --> anonymous: 첫 진입
    anonymous --> kakao_redirect: 카카오 로그인 탭
    kakao_redirect --> callback_processing: /auth/kakao/callback
    callback_processing --> authenticated: 토큰 교환 성공
    callback_processing --> anonymous: 실패/취소
    authenticated --> anonymous: 로그아웃 (POST /auth/logout)
    authenticated --> [*]: 탈퇴 (DELETE /users/me)
```

| 상태                  | UI 표현                                      | 회원 전용 동선                                                |
| --------------------- | -------------------------------------------- | ------------------------------------------------------------- |
| `anonymous`           | 헤더 우측 "카카오로 시작" 버튼, ② ③ CTA 잠금 | 검사 응시 (Step 3 비회원 결과만), 서비스 소개, 외부 평정 응시 |
| `kakao_redirect`      | 카카오 화면으로 이동 (외부)                  | —                                                             |
| `callback_processing` | `kakao-callback` 인터스티셜 (1~2초)          | —                                                             |
| `authenticated`       | 헤더 칩(닉네임 + 사진) + CoinPill + 알림 종  | 모든 기능                                                     |

**팁**

- 회원 전용 CTA를 비회원이 탭하면 LockedToast 표시 (검정 토스트 + 카카오 액션)
- 로그인 후 원래 가려던 곳(`returnTo`)으로 리다이렉트

---

## 5. 검사 시트 (Test Sheet)

```mermaid
stateDiagram-v2
    [*] --> closed
    closed --> step1: 메인 ① CTA 탭 / 시작
    step1 --> closed: ✕ (자유)
    step1 --> step2: 시작하기
    step2 --> close_dialog: ✕ 탭 (진행 중)
    close_dialog --> step2: 계속 응시
    close_dialog --> closed: 중단 (답 손실)
    step2 --> step3_guest: 응시 완료 (비회원)
    step2 --> step3_member: 응시 완료 (회원, 자동 저장)
    step3_guest --> kakao_redirect: 카카오로 저장
    step3_guest --> closed: 저장 없이 닫기
    step3_member --> closed: 닫기 (결과는 /results/[id]에 저장됨)
```

| Step              | 닫기 가능          | 데이터 손실      |
| ----------------- | ------------------ | ---------------- |
| 1 (유형 선택)     | ✓ 자유             | 없음             |
| 2 (응시 중)       | 다이얼로그 확인 후 | 모든 답변        |
| 3 (결과 — 비회원) | ✓ 자유             | 회원 저장 안 함  |
| 3 (결과 — 회원)   | ✓ 자유             | 이미 자동 저장됨 |

---

## 6. 알림 (Notification)

```mermaid
stateDiagram-v2
    [*] --> unread: SSE event 도착
    unread --> read: 알림 행 탭 (목적지 이동 + DELETE)
    unread --> deleted: 30일 경과 (배치)
    read --> [*]
    deleted --> [*]
```

| 상태      | 표현                                                 |
| --------- | ---------------------------------------------------- |
| `unread`  | 옅은 노란 배경 + 우측 빨간 점. 헤더 종 뱃지 카운트 ↑ |
| `read`    | 즉시 삭제됨 (별도 "읽음" 상태 없음 — UI 단순화)      |
| `deleted` | 사라짐                                               |

**규칙**

- 알림 행 탭 = 목적지 이동 + 즉시 삭제 (읽음 처리 X)
- 30일 이전은 자동 삭제 (배치)
- "모두 읽음" = 일괄 삭제

---

## 7. 동료 관계 (Colleague Relationship)

```mermaid
stateDiagram-v2
    [*] --> none
    none --> invited: A가 B의 코드 입력 (POST /colleagues)
    invited --> active: 양방향 등록 완료
    active --> deleted_by_self: 본인이 삭제 (DELETE /colleagues/{id})
    active --> deleted_by_peer: 상대가 삭제
    active --> orphaned: 상대 탈퇴
    deleted_by_self --> [*]
    deleted_by_peer --> [*]
    orphaned --> [*]
```

| 상태              | 본인 화면                                                 | 상대 화면      |
| ----------------- | --------------------------------------------------------- | -------------- |
| `active`          | 동료 카드 노출, 케미 발행 가능                            | 동료 카드 노출 |
| `deleted_by_self` | 사라짐 (이전 케미 보고서는 본인 이력에 유지)              | 사라짐         |
| `deleted_by_peer` | 사라짐 (자동)                                             | 사라짐         |
| `orphaned`        | 사라짐, 단 케미 보고서 상세에 "이 동료가 탈퇴했어요" 라벨 | —              |

---

## 상태 ↔ API 이벤트 매핑

| 상태 변화                           | 트리거 API/이벤트                               |
| ----------------------------------- | ----------------------------------------------- |
| `chemistry.idle → generating`       | `POST /chemistry-reports` (202)                 |
| `chemistry.generating → ready`      | SSE `chemistry.ready`                           |
| `chemistry.generating → error`      | SSE `chemistry.error`                           |
| `coin.* → -1`                       | `POST /chemistry-reports` 성공                  |
| `coin.* → +1`                       | 자정 배치 (서버)                                |
| `session.anonymous → authenticated` | `/auth/kakao/callback` 성공                     |
| `assessment.created → consumed`     | `POST /results/score` (rater=OTHER, with token) |
| `notification.unread → deleted`     | `DELETE /notifications/{id}`                    |
| `colleague.none → active`           | `POST /colleagues` 성공 (양방향)                |
