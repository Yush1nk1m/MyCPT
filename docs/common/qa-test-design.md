# MyCPT 수동 QA 테스트 설계

**문서 버전**: v0.1
**작성일**: '26.07.15.
**작성자**: 김유신

## 변경 이력

| 버전 | 변경 내용 | 날짜 |
| ---- | --------- | ---------- |
| v0.1 | 초안 작성 — `frontend/scenario-test-design.md`(화면 단위 시나리오 24종) 흡수 + E2E 통합 흐름 4종·시드 운용 규약 신설. 테스트 ID 접두사를 `E2E`로 통일(구 `SC-`) | '26.07.15. |

---

## 1. 목적 · 범위

### 목적

자동화하지 않는(또는 자동화할 수 없는) **수동 QA의 정본**이다. 두 층위를 함께 다룬다.

- **화면 단위 시나리오** (§4) — 개별 화면의 분기·안내 문구·상태 표시
- **E2E 통합 흐름** (§5) — 화면을 가로지르는 사용자 여정. 화면 전환·상태 이월·트랜잭션·캐시 적재가 검증 대상

두 층위를 한 문서에 두는 이유는, 화면 단위 시나리오도 결국 백엔드 로직과 협업하여 검증되기 때문이다. 프론트/백 경계로 문서를 가르면 같은 계정·시드 규약을 두 곳에서 중복 서술하게 된다.

### 범위 밖

| 대상 | 담당 문서 |
| --- | --- |
| 프론트 순수 로직 단위 테스트 (`lib/*`·`stores/*`) | `frontend/unit-test-design.md` |
| 백엔드 자동 테스트 (UT/ST/IT) | `backend/test-design.md` |
| 테스트 종류 결정 기준 · ID 포맷 | `common/test-process.md` |

### 테스트 ID 포맷

`common/test-process.md`의 `[종류]-[축약]-[행위]-[상황]`을 따르며, 종류는 **`E2E`로 통일**한다.

- 화면 단위: `E2E-[화면]-[행위]-[상황]` — 예: `E2E-Invite-비회원조회-성공`
- 통합 흐름: `E2E-[흐름]-[행위]-[상황]` — 예: `E2E-Chemistry-발행-성공`

두 층위 모두 실제 스택(프론트 → API → DB)을 통과하는 수동 검증이므로 접두사를 나누지 않는다. 축약어(`Invite`·`Assess`·`About`·`Help`·`Leave` vs `Guest`·`Member`·`Peer`·`Chemistry`)로 구분되며 충돌하지 않는다.

---

## 2. 시드 운용 규약

### 명령

QA는 시나리오 전후로 DB 상태를 되돌린다. 실행 경로는 `infra/docker/dev/`다.

| 명령 | 용도 |
| --- | --- |
| `make qa-reset` | 정리 → 기본 시드 삽입. **흐름 1~3 및 모든 화면 단위 시나리오의 사전 조건** |
| `make qa-flow FLOW=4` | 정리 → 기본 시드 → 흐름 4 전제 픽스처 (케미) |
| `make qa-teardown` | dev 데이터 제거. **QA 종료 후 실행** |
| `make qa-status` | 현재 dev 데이터 · 캐시 적재 현황 확인 |

구성 파일은 `docs/sql/qa/teardown.sql`(제거), `docs/sql/dev_scenario_seed.sql`(기본 삽입), `docs/sql/qa/flow-4-chemistry.sql`(흐름 4 전제)이다.

> `dev_scenario_seed.sql`을 단독 실행하지 않는다. 이 파일에는 초기화 블록이 없어 재실행이 안전하지 않다. 항상 위 make 타겟을 경유한다.

### 흐름은 상호 독립이다

흐름 1~4는 순서 의존이 없다. 각 흐름 시작 전에 `qa-reset`(또는 흐름 4는 `qa-flow FLOW=4`)으로 상태를 되돌리므로 어느 것부터 실행해도 무방하다.

### 캐시는 지우지 않는다

`disc_cache`(81행)와 `chemistry_cache`(6,561행)는 `schema.sql`이 직접 INSERT하는 고정 행이며 `report=NULL`(미생성)로 시작한다.

teardown은 이 행들을 **DELETE하지 않는다.** 두 가지 이유다.

1. `disc_tests → disc_cache`, `chemistry_reports → chemistry_cache` 복합 FK의 대상이다. 지우면 결과 데이터를 넣을 수 없다.
2. `report` 컬럼에 과금된 LLM 산출물이 들어 있다. 지우면 QA를 반복할 때마다 재과금된다.

대신 teardown은 `chemistry_cache`에서 `status <> 'READY'`인 행의 상태만 되돌린다. QA 도중 케미 발행을 중단하면 행이 `GENERATING`에 멈추고 다음 QA가 구독자 대기(`chemistry.subscriber-wait-timeout-seconds=300`)에 **5분간 블로킹**되기 때문이다. `READY`(지불 완료분)는 보존한다.

**LLM 실호출은 허용한다.** Lazy Caching 구조상 같은 버킷·조합은 첫 호출만 과금되며, 배포 직전 검증이라는 목적상 LLM 경로·캐시 적재·SSE 푸시를 실제로 통과시키는 편이 낫다. 캐시가 쌓일수록 QA 반복 비용은 0에 수렴한다.

### 로그인

계정 로그인은 `GET /api/v1/dev/login?kakaoId={id}&returnTo={경로}` (dev 프로필 전용, prod 미존재).

계정별 로그인 URL을 북마크해두면 편하다:

- `http://localhost:8080/api/v1/dev/login?kakaoId=dev-user-a&returnTo=/`
- `http://localhost:8080/api/v1/dev/login?kakaoId=dev-user-b&returnTo=/`
- `http://localhost:8080/api/v1/dev/login?kakaoId=dev-user-c&returnTo=/`

시나리오별로 `returnTo`만 바꿔 바로 목적 페이지로 진입할 수 있다 (예: `returnTo=/invite/AAAA1111`).

### 게스트 → 로그인 전환 흐름의 대체 규약

"게스트가 로그인 완료 후 자동 등록" 흐름은, 실제 카카오 로그인 대신 **로그아웃 상태에서 GET 확인 → dev-login으로 로그인 상태 전환 후 같은 URL 재진입**으로 대체한다. 인증 이후 로직(useEffect 자동 등록)은 로그인 경로와 무관하게 동일하게 동작하므로 검증 목적상 동등하다.

---

## 3. 테스트 계정

`dev_scenario_seed.sql`이 만드는 계정이다.

| 계정   | kakao_id   | 닉네임 | 조건                                                                  |
| ------ | ---------- | ------ | --------------------------------------------------------------------- |
| user-a | dev-user-a | 차은우 | 유효한 초대 코드(`AAAA1111`) + 평정 토큰 3종(유효/만료/사용완료) 보유 |
| user-b | dev-user-b | 박보영 | 유효한 초대 코드(`BBBB2222`). user-a와 미등록 상태                    |
| user-c | dev-user-c | 김철수 | user-a와 이미 동료로 연결됨                                           |
| user-d | dev-user-d | 이영희 | 초대 코드(`DDDD4444`)가 만료됨                                        |

로그인 없이(쿠키 없는 시크릿 창) 접속하는 경우는 "게스트"로 표기한다.

전 계정 코인 3개 보유(가입 지급). 검사 결과는 기본 시드에 없다 — 흐름 4는 `flow-4-chemistry.sql`이 심는다.

---

## 4. 화면 단위 시나리오

> 사전 조건: `make qa-reset`

### `/invite/[code]` — 초대 수락

| Test ID                     | 사전 조건                                   | 시나리오                                   | 연결된 API                                  | 기대 결과                                                                                |
| --------------------------- | ------------------------------------------- | ------------------------------------------ | ------------------------------------------- | ---------------------------------------------------------------------------------------- |
| E2E-Invite-비회원조회-성공   | user-a 유효 코드(`AAAA1111`)                | 게스트가 `/invite/AAAA1111` 접속           | `GET /colleagues/invite/{code}`             | user-a 프로필(닉네임) + 혜택 3줄 + 카카오 로그인 CTA 표시                                |
| E2E-Invite-회원자동등록-성공 | user-a 유효 코드, user-b·user-a 미등록 상태 | user-b 로그인 후 `/invite/AAAA1111` 접속   | `GET .../invite/{code}`, `POST /colleagues` | 프로필 표시 직후 자동으로 "동료가 됐어요" 화면 전환. 양쪽 `/colleagues` 목록에 상호 반영 |
| E2E-Invite-만료코드-실패     | user-d 코드(`DDDD4444`) 만료됨              | user-b 로그인 후 `/invite/DDDD4444` 접속   | `GET .../invite/{code}`                     | "초대장이 만료됐어요" 안내                                                               |
| E2E-Invite-자기초대-실패     | user-a 로그인, 자신의 코드 사용             | user-a 로그인 후 `/invite/AAAA1111` 접속   | `GET .../invite/{code}`                     | "본인의 초대장이에요" 안내                                                               |
| E2E-Invite-이미동료-실패     | user-a·user-c 이미 동료                     | user-c 로그인 후 `/invite/AAAA1111` 접속   | `GET`(성공) → `POST`(실패)                  | 프로필까지는 정상 표시, 자동 등록 시도 시 "이미 동료로 등록된 사람이에요" 안내           |
| E2E-Invite-코드없음-실패     | 없음                                        | 임의 계정으로 `/invite/ZZZZZZZZ` 접속      | `GET .../invite/{code}`                     | "존재하지 않는 초대장이에요" 안내                                                        |
| E2E-Invite-기존흐름-회귀     | user-a, user-b 로그인                       | `/colleagues` 페이지의 코드 직접 입력 등록 | `POST /colleagues`                          | 기존 등록 플로우 정상 동작 (permitAll 전환 영향 없음)                                    |

### `/assessments/[token]` — 타인 평정

| Test ID                     | 사전 조건                                 | 시나리오                                                              | 연결된 API                                    | 기대 결과                                                      |
| --------------------------- | ----------------------------------------- | --------------------------------------------------------------------- | --------------------------------------------- | -------------------------------------------------------------- |
| E2E-Assess-정상응시-성공     | user-a 유효 토큰(`DEVTOKEN-VALID-...`)    | 게스트가 해당 토큰으로 접속 → 24문항 응답 → 제출                      | `GET /assessments/{token}`, `POST .../submit` | 인트로 → 응시 → "감사합니다" 완료 화면. 응답은 user-a에게 귀속 |
| E2E-Assess-재사용방지-실패   | user-a 사용완료 토큰(`DEVTOKEN-USED-...`) | 게스트가 해당 토큰으로 접속                                           | `GET /assessments/{token}`                    | "이미 응시가 완료된 링크예요" 안내                             |
| E2E-Assess-만료토큰-실패     | user-a 만료 토큰(`DEVTOKEN-EXPIRED-...`)  | 게스트가 해당 토큰으로 접속                                           | `GET /assessments/{token}`                    | "초대장이 만료됐어요" 안내                                     |
| E2E-Assess-토큰없음-실패     | 없음                                      | 임의 문자열 토큰으로 접속                                             | `GET /assessments/{token}`                    | "존재하지 않는 링크예요" 안내                                  |
| E2E-Assess-회원접속-성공     | user-a 유효 토큰, user-b 로그인           | user-b 로그인 상태로 해당 토큰 접속 → 응시                            | `GET`, `POST .../submit`                      | 게스트와 동일한 흐름으로 정상 진행 (세션 정보 미사용 확인)     |
| E2E-Assess-제출재시도-성공   | user-a 유효 토큰, 24문항 응답 완료 상태   | devtools로 네트워크 차단 후 제출 → 실패 확인 → 네트워크 복구 → 재시도 | `POST .../submit`                             | 답변 재입력 없이 재제출 성공 (`doSubmit` 재사용 로직 검증)     |
| E2E-Assess-이전응답유지-성공 | user-a 유효 토큰                          | 5번 문항까지 응답 후 "이전" 버튼으로 1번까지 복귀 → 재이동            | 없음 (프론트 로컬 상태)                       | 각 문항의 Most/Least 선택이 정확히 복원됨                      |

### `/me/about` · `/me/help` — 정적/보조 페이지

API가 없는 정적 화면. 로그인 없이도 접근 가능(`auth: public`, middleware `PUBLIC_EXCEPTIONS`).

| Test ID | 사전 조건 | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- | --- |
| E2E-About-공용접근-성공 | 없음 | 게스트가 `/me/about` 접속 | 없음 | 기능 4행(①~④) + 약관/정책 4행 + 푸터 정상 표시, 로그인 리다이렉트 없음 |
| E2E-About-약관탭-안내 | 없음 | 약관/정책 행 탭 | 없음 | "문서 준비 중이에요" 토스트 |
| E2E-Help-공용접근-성공 | 없음 | 게스트가 `/me/help` 접속 | 없음 | 연락 카드 + FAQ 아코디언 정상, 첫 FAQ 기본 펼침 |
| E2E-Help-아코디언-토글 | 없음 | FAQ 항목 탭 반복 | 없음 | 열림/닫힘 토글, 동시에 하나만 펼침 |
| E2E-Help-메일-동작 | 없음 | "이메일 문의" 탭 | 없음 | `mailto:help@mycpt.kr` 메일 앱 연결. "카카오 채널"은 준비 중 토스트 |

### `/me/leave` — 회원탈퇴 (2단)

> ⚠ **파괴적 시나리오** — 완료(E2E-Leave-완료)는 계정을 실제 삭제한다. 희생 계정(예: dev-user-d) 또는 `make qa-reset` 재실행 전제로 진행한다.

| Test ID | 사전 조건 | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- | --- |
| E2E-Leave-카운트-표시 | 데이터 보유 회원 로그인 | `/me/leave` 진입 | `GET /users/me/withdrawal-info` | 삭제 항목이 실측 카운트(검사/케미/동료)로 표시 |
| E2E-Leave-게이트-차단 | Step 2 다이얼로그 열림 | 확인란에 오타 입력 | 없음 | "탈퇴 진행" 버튼 비활성 유지 |
| E2E-Leave-게이트-활성 | Step 2 다이얼로그 열림 | 확인란에 정확히 `탈퇴할게요` 입력 | 없음 | "탈퇴 진행" 버튼 활성화 |
| E2E-Leave-완료 | 희생 계정 로그인, 확인 문구 정확 입력 | "탈퇴 진행" 탭 | `DELETE /users/me` | 200 → 로그아웃 상태 전환(헤더 반영) + 홈 이동 + "탈퇴가 완료됐어요" 토스트 |
| E2E-Leave-취소-유지 | Step 1 또는 Step 2 | "취소하고 돌아가기" / "계정 유지하기" 탭 | 없음 | 탈퇴 미실행, 계정 유지 |

---

## 5. E2E 통합 흐름

화면을 가로지르는 사용자 여정. 개별 화면이 아니라 **화면 사이에서 상태·트랜잭션·캐시가 이월되는지**를 본다.

### 흐름 1 — 비회원 여정 (`E2E-Guest`)

> 사전 조건: `make qa-reset`
> 검증 초점: **sessionStorage 결과가 로그인 경계를 넘어 DB로 이월되는가.**

| Test ID | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- |
| E2E-Guest-검사완주-성공 | 홈 → 소개 시트 → 검사 시작 → 24문항 Most/Least → 제출 | `POST /results/score` | 채점 완료, 결과 화면 진입 |
| E2E-Guest-LLM생성-성공 | `report=NULL`인 버킷으로 최초 진입 | 내부 LLM 호출 | 6섹션 보고서 표시, `disc_cache.report` 적재 (`make qa-status`로 확인) |
| E2E-Guest-캐시히트-성공 | 같은 버킷으로 재응시 | 없음 (캐시) | LLM 미호출, 즉시 표시 |
| E2E-Guest-결과이월-성공 | 결과 화면 → 로그인 유도 → dev-login → `/save-result` | `POST /results` | DB 저장 후 `/results/[id]` 진입, 내용 동일 |
| E2E-Guest-새로고침-현행 | 결과 화면에서 로그인 없이 새로고침 | 없음 | **현행 동작 기록**: 시트가 닫히고 홈 복귀. `sessionStorage['disc_result']`는 잔존하나 이를 읽어 화면을 복원하는 경로가 없다 (개선은 `plan.md` §2 백로그) |

### 흐름 2 — 회원 여정 (`E2E-Member`)

> 사전 조건: `make qa-reset`
> 검증 초점: **결과 누적이 통계·추이·코인에 파생 반영되는가.**

| Test ID | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- |
| E2E-Member-프로필설정-성공 | user-a 로그인 → `/me/profile` 닉네임·생년·성별 저장 | `PATCH /users/me` | 저장 후 헤더 반영 |
| E2E-Member-재응시저장-성공 | 검사 응시 → 즉시 저장 (게스트와 달리 `/save-result` 미경유) | `POST /results` | 이력에 즉시 추가 |
| E2E-Member-이력조회-성공 | `/results` 진입 → 스크롤 | `GET /results` (커서) | 커서 페이지네이션 정상, 중복·누락 없음 |
| E2E-Member-통계반영-성공 | `/me/insights` 진입 (결과 2건 이상 전제) | `GET /statistics/*` | 나이대·성별 비교 + 변화 추이 렌더 |
| E2E-Member-코인이력-성공 | `/me/coins` 진입 | `GET /coins`, `GET /coins/transactions` | 잔액 3개 + SIGNUP 이력 표시 |

### 흐름 3 — 타인 평정 여정 (`E2E-Peer`)

> 사전 조건: `make qa-reset`
> 검증 초점: **일회용 토큰이 응답을 올바른 주체에 귀속시키고 알림이 실시간 도달하는가.**

| Test ID | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- |
| E2E-Peer-링크생성-성공 | user-a 로그인 → 평정 링크 생성 | `POST /assessments/tokens` | 링크·라벨 발급 |
| E2E-Peer-게스트응시-성공 | 게스트(시크릿 창)가 링크 접속 → 24문항 → 제출 | `GET /assessments/{token}`, `POST .../submit` | 완료 화면 |
| E2E-Peer-귀속확인-성공 | user-a 결과 화면 확인 | `GET /results` | `rater_type=OTHER` 결과가 user-a에 귀속 |
| E2E-Peer-알림푸시-성공 | 제출 시점에 user-a 브라우저를 열어둠 | SSE `/notifications/stream` | 실시간 알림 도달 (**자동화 제외 영역** — §6) |
| E2E-Peer-토큰소진-실패 | 같은 토큰 재접속 | `GET /assessments/{token}` | "이미 응시가 완료된 링크예요" |

### 흐름 4 — 케미 여정 (`E2E-Chemistry`)

> 사전 조건: `make qa-flow FLOW=4` — user-a·user-c 양쪽에 DISC 결과가 심어진다
> 검증 초점: **코인 차감 트랜잭션·캐시 적재·알림이 한 흐름에서 정합하게 맞물리는가.**

| Test ID | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- |
| E2E-Chemistry-발행-성공 | user-a → `/colleagues` → user-c 상세 → 케미 발행 | `POST /chemistry` | GENERATING → SSE 수신 → READY, `/chemistry/[id]` 보고서 렌더 |
| E2E-Chemistry-코인차감-정합 | 발행 전후 `/me/coins` 비교 | `GET /coins/transactions` | `coins` 1 감소 + `CHEMISTRY_REPORT` 이력 1건 (금액·잔액 정합) |
| E2E-Chemistry-캐시적재-성공 | 발행 후 캐시 확인 | 없음 (`make qa-status`) | `chemistry_cache.status='READY'`, `report` 채워짐 |
| E2E-Chemistry-캐시히트-성공 | 같은 버킷 조합 재발행 | `POST /chemistry` | LLM 미호출, 즉시 READY |
| E2E-Chemistry-코인부족-차단 | `coins=0`으로 조정 후 발행 시도 (`flow-4-chemistry.sql` 하단 유틸 주석 해제) | `POST /chemistry` | 발행 차단 + 충전 안내 |

> **동시 발행 방어**(SELECT FOR UPDATE + CountDownLatch)는 백엔드 IT(`ChemistryCacheServiceIntegrationTest`)에서 검증 완료되어 수동 QA에서 제외한다. 두 브라우저 동시 클릭은 수동 재현 신뢰도가 낮고 자동 테스트와 중복이다.

---

## 6. 자동화 제외 영역

`common/test-process.md`가 지정한 수동 테스트 영역이다.

- 카카오 OAuth2 전체 플로우
- SSE 연결 및 실시간 푸시
- 카카오 연결 해제

흐름 3(`E2E-Peer-알림푸시-성공`)과 흐름 4(`E2E-Chemistry-발행-성공`)는 SSE 실시간 푸시에 의존한다. 이 두 케이스는 **자동 테스트로 대체할 수 없으며 수동 QA로만 검증 가능하다.** 자동화를 검토하더라도 이 영역은 수동으로 남는다.

카카오 OAuth는 §2의 dev-login으로 우회한다. 인증 이후 로직은 로그인 경로와 무관하게 동일하므로 검증 목적상 동등하다.
