# docs/frontend/scenario-test-design.md

# MyCPT 프론트엔드 시나리오 테스트 설계

> 화면 단위 수동 QA 시나리오. `dev_scenario_seed.sql`로 만든 계정 기준으로 작성.
> 계정 로그인은 `GET /api/v1/dev/login?kakaoId={id}&returnTo={경로}` (dev 프로필 전용, prod 미존재).

## 테스트 계정

| 계정   | kakao_id   | 닉네임 | 조건                                                                  |
| ------ | ---------- | ------ | --------------------------------------------------------------------- |
| user-a | dev-user-a | 차은우 | 유효한 초대 코드(`AAAA1111`) + 평정 토큰 3종(유효/만료/사용완료) 보유 |
| user-b | dev-user-b | 박보영 | 유효한 초대 코드(`BBBB2222`). user-a와 미등록 상태                    |
| user-c | dev-user-c | 김철수 | user-a와 이미 동료로 연결됨                                           |
| user-d | dev-user-d | 이영희 | 초대 코드(`DDDD4444`)가 만료됨                                        |

로그인 없이(쿠키 없는 시크릿 창) 접속하는 경우는 "게스트"로 표기.

## 사용법

1. `dev_scenario_seed.sql` 실행 (0번 초기화 블록 포함 — 재실행 안전)
2. 계정별 로그인 URL을 북마크:
   - `http://localhost:8080/api/v1/dev/login?kakaoId=dev-user-a&returnTo=/`
   - `http://localhost:8080/api/v1/dev/login?kakaoId=dev-user-b&returnTo=/`
   - `http://localhost:8080/api/v1/dev/login?kakaoId=dev-user-c&returnTo=/`
3. 시나리오별로 `returnTo`만 바꿔 바로 목적 페이지로 진입 가능 (예: `returnTo=/invite/AAAA1111`)
4. "게스트가 로그인 완료 후 자동 등록" 흐름은, 실제 카카오 로그인 대신 **로그아웃 상태에서 GET 확인 → dev-login으로 로그인 상태 전환 후 같은 URL 재진입**으로 대체. 인증 이후 로직(useEffect 자동 등록)은 로그인 경로와 무관하게 동일하게 동작하므로 검증 목적상 동등함.

---

## `/invite/[code]` — 초대 수락

| Test ID                     | 사전 조건                                   | 시나리오                                   | 연결된 API                                  | 기대 결과                                                                                |
| --------------------------- | ------------------------------------------- | ------------------------------------------ | ------------------------------------------- | ---------------------------------------------------------------------------------------- |
| SC-Invite-비회원조회-성공   | user-a 유효 코드(`AAAA1111`)                | 게스트가 `/invite/AAAA1111` 접속           | `GET /colleagues/invite/{code}`             | user-a 프로필(닉네임) + 혜택 3줄 + 카카오 로그인 CTA 표시                                |
| SC-Invite-회원자동등록-성공 | user-a 유효 코드, user-b·user-a 미등록 상태 | user-b 로그인 후 `/invite/AAAA1111` 접속   | `GET .../invite/{code}`, `POST /colleagues` | 프로필 표시 직후 자동으로 "동료가 됐어요" 화면 전환. 양쪽 `/colleagues` 목록에 상호 반영 |
| SC-Invite-만료코드-실패     | user-d 코드(`DDDD4444`) 만료됨              | user-b 로그인 후 `/invite/DDDD4444` 접속   | `GET .../invite/{code}`                     | "초대장이 만료됐어요" 안내                                                               |
| SC-Invite-자기초대-실패     | user-a 로그인, 자신의 코드 사용             | user-a 로그인 후 `/invite/AAAA1111` 접속   | `GET .../invite/{code}`                     | "본인의 초대장이에요" 안내                                                               |
| SC-Invite-이미동료-실패     | user-a·user-c 이미 동료                     | user-c 로그인 후 `/invite/AAAA1111` 접속   | `GET`(성공) → `POST`(실패)                  | 프로필까지는 정상 표시, 자동 등록 시도 시 "이미 동료로 등록된 사람이에요" 안내           |
| SC-Invite-코드없음-실패     | 없음                                        | 임의 계정으로 `/invite/ZZZZZZZZ` 접속      | `GET .../invite/{code}`                     | "존재하지 않는 초대장이에요" 안내                                                        |
| SC-Invite-기존흐름-회귀     | user-a, user-b 로그인                       | `/colleagues` 페이지의 코드 직접 입력 등록 | `POST /colleagues`                          | 기존 등록 플로우 정상 동작 (permitAll 전환 영향 없음)                                    |

## `/assessments/[token]` — 타인 평정

| Test ID                     | 사전 조건                                 | 시나리오                                                              | 연결된 API                                    | 기대 결과                                                      |
| --------------------------- | ----------------------------------------- | --------------------------------------------------------------------- | --------------------------------------------- | -------------------------------------------------------------- |
| SC-Assess-정상응시-성공     | user-a 유효 토큰(`DEVTOKEN-VALID-...`)    | 게스트가 해당 토큰으로 접속 → 24문항 응답 → 제출                      | `GET /assessments/{token}`, `POST .../submit` | 인트로 → 응시 → "감사합니다" 완료 화면. 응답은 user-a에게 귀속 |
| SC-Assess-재사용방지-실패   | user-a 사용완료 토큰(`DEVTOKEN-USED-...`) | 게스트가 해당 토큰으로 접속                                           | `GET /assessments/{token}`                    | "이미 응시가 완료된 링크예요" 안내                             |
| SC-Assess-만료토큰-실패     | user-a 만료 토큰(`DEVTOKEN-EXPIRED-...`)  | 게스트가 해당 토큰으로 접속                                           | `GET /assessments/{token}`                    | "초대장이 만료됐어요" 안내                                     |
| SC-Assess-토큰없음-실패     | 없음                                      | 임의 문자열 토큰으로 접속                                             | `GET /assessments/{token}`                    | "존재하지 않는 링크예요" 안내                                  |
| SC-Assess-회원접속-성공     | user-a 유효 토큰, user-b 로그인           | user-b 로그인 상태로 해당 토큰 접속 → 응시                            | `GET`, `POST .../submit`                      | 게스트와 동일한 흐름으로 정상 진행 (세션 정보 미사용 확인)     |
| SC-Assess-제출재시도-성공   | user-a 유효 토큰, 24문항 응답 완료 상태   | devtools로 네트워크 차단 후 제출 → 실패 확인 → 네트워크 복구 → 재시도 | `POST .../submit`                             | 답변 재입력 없이 재제출 성공 (`doSubmit` 재사용 로직 검증)     |
| SC-Assess-이전응답유지-성공 | user-a 유효 토큰                          | 5번 문항까지 응답 후 "이전" 버튼으로 1번까지 복귀 → 재이동            | 없음 (프론트 로컬 상태)                       | 각 문항의 Most/Least 선택이 정확히 복원됨                      |

## `/me/about` · `/me/help` — 정적/보조 페이지

API가 없는 정적 화면. 로그인 없이도 접근 가능(`auth: public`, middleware `PUBLIC_EXCEPTIONS`).

| Test ID | 사전 조건 | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- | --- |
| SC-About-공용접근-성공 | 없음 | 게스트가 `/me/about` 접속 | 없음 | 기능 4행(①~④) + 약관/정책 4행 + 푸터 정상 표시, 로그인 리다이렉트 없음 |
| SC-About-약관탭-안내 | 없음 | 약관/정책 행 탭 | 없음 | "문서 준비 중이에요" 토스트 |
| SC-Help-공용접근-성공 | 없음 | 게스트가 `/me/help` 접속 | 없음 | 연락 카드 + FAQ 아코디언 정상, 첫 FAQ 기본 펼침 |
| SC-Help-아코디언-토글 | 없음 | FAQ 항목 탭 반복 | 없음 | 열림/닫힘 토글, 동시에 하나만 펼침 |
| SC-Help-메일-동작 | 없음 | "이메일 문의" 탭 | 없음 | `mailto:help@mycpt.kr` 메일 앱 연결. "카카오 채널"은 준비 중 토스트 |

## `/me/leave` — 회원탈퇴 (2단)

> ⚠ **파괴적 시나리오** — 완료(SC-Leave-완료)는 계정을 실제 삭제한다. 희생 계정(예: dev-user-d) 또는 `dev_scenario_seed.sql` 재실행 전제로 진행한다.

| Test ID | 사전 조건 | 시나리오 | 연결된 API | 기대 결과 |
| --- | --- | --- | --- | --- |
| SC-Leave-카운트-표시 | 데이터 보유 회원 로그인 | `/me/leave` 진입 | `GET /users/me/withdrawal-info` | 삭제 항목이 실측 카운트(검사/케미/동료)로 표시 |
| SC-Leave-게이트-차단 | Step 2 다이얼로그 열림 | 확인란에 오타 입력 | 없음 | "탈퇴 진행" 버튼 비활성 유지 |
| SC-Leave-게이트-활성 | Step 2 다이얼로그 열림 | 확인란에 정확히 `탈퇴할게요` 입력 | 없음 | "탈퇴 진행" 버튼 활성화 |
| SC-Leave-완료 | 희생 계정 로그인, 확인 문구 정확 입력 | "탈퇴 진행" 탭 | `DELETE /users/me` | 200 → 로그아웃 상태 전환(헤더 반영) + 홈 이동 + "탈퇴가 완료됐어요" 토스트 |
| SC-Leave-취소-유지 | Step 1 또는 Step 2 | "취소하고 돌아가기" / "계정 유지하기" 탭 | 없음 | 탈퇴 미실행, 계정 유지 |
