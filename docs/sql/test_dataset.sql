-- ============================================================
-- MyCPT 테스트 데이터 삽입 쿼리
-- 전제: schema.sql + chemistry_cache_seed.sql 이미 적용 완료
-- 주의: User A의 kakao_id를 실제 카카오 로그인 계정 값으로 교체
-- ============================================================

-- ── 1. users ──────────────────────────────────────────────────────────────

-- User A: 실제 로그인 계정 (kakao_id 교체 필수)
-- coins=2: 케미 발행 1회 가능, 0개 차단 시나리오도 테스트 가능
-- INSERT INTO users (kakao_id, nickname, profile_image_url, birth_year, gender, coins, next_coin_at, created_at)
-- VALUES ('REPLACE_WITH_YOUR_KAKAO_ID', '유신', NULL, 1999, 'M', 2, NULL, NOW());

-- User B: 페어 상대방 (가상 계정 — 로그인 불필요, 데이터만 필요)
-- kakao_id는 실제 존재하지 않는 값으로 충돌 없음
INSERT INTO users (kakao_id, nickname, profile_image_url, birth_year, gender, coins, next_coin_at, created_at)
VALUES ('test-kakao-user-b', '민준', NULL, 1998, 'M', 3, NULL, NOW());

-- ── 2. coin_transactions ──────────────────────────────────────────────────
-- User A 코인 이력: 가입 지급 3개 → 케미 발행으로 1개 차감 → 잔액 2개

INSERT INTO coin_transactions (user_id, amount, reason, balance_after, created_at)
VALUES
    (1, 3,  'SIGNUP',           3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, -1, 'CHEMISTRY_REPORT', 2, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- User B 코인 이력: 가입 지급만
INSERT INTO coin_transactions (user_id, amount, reason, balance_after, created_at)
VALUES
    (2, 3, 'SIGNUP', 3, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ── 3. peer_codes ─────────────────────────────────────────────────────────
-- 두 사람 모두 유효한 코드 보유 (7일 후 만료)
-- /colleagues 화면의 "내 동료 코드" 카드에 표시됨

INSERT INTO peer_codes (user_id, code, expires_at, created_at)
VALUES
    (1, 'YUSHIN01', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
    (2, 'MINJUN02', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW());

-- ── 4. colleagues ─────────────────────────────────────────────────────────
-- user_a_id < user_b_id 규칙 적용 (1 < 2)
-- /colleagues 화면에서 "민준" 카드 표시됨

INSERT INTO colleagues (user_a_id, user_b_id, created_at)
VALUES (1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ── 5. tests + disc_tests (자기 평정 결과) ───────────────────────────────
-- 케미 발행의 선행 조건: 두 사람 모두 자기 평정 결과 필요
-- 버킷값은 disc_cache에 사전 삽입된 값 내에 있어야 함 (1~3)

-- User A 자기 평정
INSERT INTO tests (user_id, rater_type, dtype, label, created_at)
VALUES (1, 'SELF', 'DISC', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY));
-- tests.id = 1 (첫 번째 INSERT)

INSERT INTO disc_tests (test_id, d_score, i_score, s_score, c_score, d_bucket, i_bucket, s_bucket, c_bucket)
VALUES (1, 20, 5, -3, 2, 3, 2, 1, 2);
-- d_bucket=3, i_bucket=2, s_bucket=1, c_bucket=2

-- User B 자기 평정
INSERT INTO tests (user_id, rater_type, dtype, label, created_at)
VALUES (2, 'SELF', 'DISC', NULL, DATE_SUB(NOW(), INTERVAL 4 DAY));
-- tests.id = 2

INSERT INTO disc_tests (test_id, d_score, i_score, s_score, c_score, d_bucket, i_bucket, s_bucket, c_bucket)
VALUES (2, 8, 15, 10, -9, 2, 3, 2, 1);
-- d_bucket=2, i_bucket=3, s_bucket=2, c_bucket=1

-- ── 6. chemistry_cache — READY 보고서용 캐시 세팅 ────────────────────────
-- chemistry_cache는 6,561행이 status='NULL'로 이미 존재함
-- READY 보고서가 참조할 행만 UPDATE

-- A→B 방향 케미 (requester=A 버킷, partner=B 버킷)
-- requester: d=3,i=2,s=1,c=2 / partner: d=2,i=3,s=2,c=1
UPDATE chemistry_cache
SET
    status     = 'READY',
    report     = '## 두 사람의 케미 요약\n\n유신과 민준은 서로 다른 강점을 가진 보완적 관계입니다. 유신의 높은 주도성과 민준의 뛰어난 사교성이 시너지를 만듭니다.\n\n## 소통 스타일\n\n유신은 목표 중심적으로 대화하고, 민준은 관계 중심적으로 반응합니다. 유신이 방향을 제시하면 민준이 분위기를 부드럽게 만드는 역할을 잘 수행합니다.\n\n## 시너지 포인트\n\n- 유신의 결단력 + 민준의 설득력 → 협상 상황에서 강력한 팀\n- 민준의 네트워크 + 유신의 실행력 → 새로운 기회를 빠르게 포착\n\n## 주의할 점\n\n유신이 너무 빠르게 밀어붙이면 민준이 따라가기 버거울 수 있습니다. 중간 점검 포인트를 만드는 것이 좋습니다.\n\n## 갈등 해소법\n\n의견 충돌 시 유신은 감정보다 데이터로, 민준은 데이터보다 맥락으로 설명하는 경향이 있습니다. 서로의 방식을 인정하고 결론을 함께 도출하는 습관이 중요합니다.\n\n## 함께 성장하려면\n\n주기적으로 서로의 현재 상태를 확인하는 짧은 대화를 만드세요. 유신에게는 쉬어가는 타이밍을, 민준에게는 구체적인 목표를 상기시켜 주는 것이 효과적입니다.',
    created_at = NOW()
WHERE requester_d = 3 AND requester_i = 2 AND requester_s = 1 AND requester_c = 2
  AND partner_d   = 2 AND partner_i   = 3 AND partner_s   = 2 AND partner_c   = 1;

-- ── 7. chemistry_reports — 3가지 상태 ────────────────────────────────────

-- 보고서 1: READY — 정상 렌더링 확인용 (A가 B에게 발행, 이미 완료)
-- FK 버킷 컬럼은 READY 상태에서만 세팅
INSERT INTO chemistry_reports
    (requester_id, partner_id, test_type, status,
     requester_d, requester_i, requester_s, requester_c,
     partner_d, partner_i, partner_s, partner_c,
     created_at)
VALUES
    (1, 2, 'DISC', 'READY',
     3, 2, 1, 2,
     2, 3, 2, 1,
     DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 보고서 2: GENERATING — "발행 중…" 카드 확인용 (A가 B에게 발행, 아직 처리 중)
-- FK 버킷 컬럼은 NULL (캐시 락 이전 상태)
INSERT INTO chemistry_reports
    (requester_id, partner_id, test_type, status,
     requester_d, requester_i, requester_s, requester_c,
     partner_d, partner_i, partner_s, partner_c,
     created_at)
VALUES
    (1, 2, 'DISC', 'GENERATING',
     NULL, NULL, NULL, NULL,
     NULL, NULL, NULL, NULL,
     DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- 보고서 3: ERROR — 실패 화면 확인용 (B가 A에게 발행, 실패)
-- FK 버킷 컬럼은 NULL
INSERT INTO chemistry_reports
    (requester_id, partner_id, test_type, status,
     requester_d, requester_i, requester_s, requester_c,
     partner_d, partner_i, partner_s, partner_c,
     created_at)
VALUES
    (2, 1, 'DISC', 'ERROR',
     NULL, NULL, NULL, NULL,
     NULL, NULL, NULL, NULL,
     DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- 전체 삽입 확인
SELECT 'users'              AS tbl, COUNT(*) AS cnt FROM users
UNION ALL
SELECT 'coin_transactions',          COUNT(*) FROM coin_transactions
UNION ALL
SELECT 'peer_codes',                 COUNT(*) FROM peer_codes
UNION ALL
SELECT 'colleagues',                 COUNT(*) FROM colleagues
UNION ALL
SELECT 'tests',                      COUNT(*) FROM tests
UNION ALL
SELECT 'disc_tests',                 COUNT(*) FROM disc_tests
UNION ALL
SELECT 'chemistry_reports',          COUNT(*) FROM chemistry_reports;

-- chemistry_cache READY 확인
SELECT status, LEFT(report, 30) AS report_preview
FROM chemistry_cache
WHERE requester_d=3 AND requester_i=2 AND requester_s=1 AND requester_c=2
  AND partner_d=2   AND partner_i=3   AND partner_s=2   AND partner_c=1;