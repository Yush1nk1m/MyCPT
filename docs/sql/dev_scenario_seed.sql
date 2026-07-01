-- ============================================================
-- docs/sql/dev_scenario_seed.sql
-- 초대 수락(/invite) + 타인 평정(/assessments) 시나리오 테스트용 시드
-- 전제: schema.sql 적용 완료. 여러 번 재실행 가능 (하단 초기화 블록 먼저 실행)
-- 로그인: 백엔드 GET /api/v1/dev/login?kakaoId={kakao_id}&returnTo=... 로 즉시 로그인
-- ============================================================

-- ── 0. 초기화 (재실행 전 기존 dev 계정 정리) ────────────────────────────
-- kakao_id가 'dev-user-%' 패턴인 계정만 정리 (실계정 영향 없음)

DELETE FROM assessment_tokens WHERE subject_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');
DELETE FROM colleagues WHERE user_a_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
    OR user_b_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');
DELETE FROM peer_codes WHERE user_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');
DELETE FROM coin_transactions WHERE user_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');
DELETE FROM users WHERE kakao_id LIKE 'dev-user-%';

-- ── 1. users ──────────────────────────────────────────────────────────────
-- 4개 계정, 각각 다른 조건을 담당 (dev-login으로 즉시 로그인 가능)

INSERT INTO users (kakao_id, nickname, profile_image_url, birth_year, gender, coins, next_coin_at, created_at) VALUES
('dev-user-a', '차은우', NULL, 1998, 'M', 3, NULL, NOW()),  -- 기본 액터: 유효 코드/토큰 보유
('dev-user-b', '박보영', NULL, 1999, 'F', 3, NULL, NOW()),  -- 기본 상대: A와 미등록 상태
('dev-user-c', '김철수', NULL, 1997, 'M', 3, NULL, NOW()),  -- A와 이미 동료
('dev-user-d', '이영희', NULL, 2000, 'F', 3, NULL, NOW()); -- 만료된 초대 코드 소유자

-- ── 2. coin_transactions — 가입 지급 이력 (users.coins와 정합성 유지용) ──

INSERT INTO coin_transactions (user_id, amount, reason, balance_after, created_at)
SELECT id, 3, 'SIGNUP', 3, NOW() FROM users WHERE kakao_id LIKE 'dev-user-%';

-- ── 3. peer_codes — A/B/C는 유효, D는 만료 ────────────────────────────────

INSERT INTO peer_codes (user_id, code, expires_at, created_at)
SELECT id, 'AAAA1111', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW() FROM users WHERE kakao_id = 'dev-user-a'
UNION ALL
SELECT id, 'BBBB2222', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW() FROM users WHERE kakao_id = 'dev-user-b'
UNION ALL
SELECT id, 'CCCC3333', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW() FROM users WHERE kakao_id = 'dev-user-c'
UNION ALL
-- D의 코드는 이미 만료된 상태로 삽입 — "만료된 초대 코드" 시나리오 전용
SELECT id, 'DDDD4444', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY) FROM users WHERE kakao_id = 'dev-user-d';

-- ── 4. colleagues — A와 C는 이미 동료 (user_a_id < user_b_id 자동 정렬) ──

INSERT INTO colleagues (user_a_id, user_b_id, created_at)
SELECT LEAST(a.id, c.id), GREATEST(a.id, c.id), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM users a, users c
WHERE a.kakao_id = 'dev-user-a' AND c.kakao_id = 'dev-user-c';

-- ── 5. assessment_tokens — A 소유, 3가지 상태 (유효/만료/사용완료) ───────
-- RPAD로 정확히 32자 CHAR 보장 (schema: CHAR(32) NOT NULL)

INSERT INTO assessment_tokens (subject_id, token, label, used, expires_at, created_at)
SELECT id, RPAD('DEVTOKEN-VALID-', 32, '0'), '유효토큰', FALSE,
       DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()
FROM users WHERE kakao_id = 'dev-user-a'
UNION ALL
SELECT id, RPAD('DEVTOKEN-EXPIRED-', 32, '0'), '만료토큰', FALSE,
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)
FROM users WHERE kakao_id = 'dev-user-a'
UNION ALL
SELECT id, RPAD('DEVTOKEN-USED-', 32, '0'), '사용완료토큰', TRUE,
       DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()
FROM users WHERE kakao_id = 'dev-user-a';

-- ── 6. 삽입 확인 ─────────────────────────────────────────────────────────

SELECT kakao_id, nickname, coins FROM users WHERE kakao_id LIKE 'dev-user-%';
SELECT u.kakao_id, p.code, p.expires_at,
       CASE WHEN p.expires_at < NOW() THEN '만료됨' ELSE '유효' END AS status
FROM peer_codes p JOIN users u ON u.id = p.user_id
WHERE u.kakao_id LIKE 'dev-user-%';
SELECT u.kakao_id AS subject, t.label, t.used, t.expires_at
FROM assessment_tokens t JOIN users u ON u.id = t.subject_id
WHERE u.kakao_id LIKE 'dev-user-%';

-- ============================================================
-- 범용 유틸 — 코인 강제 조정 (앱에서 보장 못하는 상태 재현용)
-- 케미 관련 시나리오 문서 작성 시 사용
-- ============================================================
-- UPDATE users SET coins = 0 WHERE kakao_id = 'dev-user-a';
-- INSERT INTO coin_transactions (user_id, amount, reason, balance_after, created_at)
-- SELECT id, -3, 'CHEMISTRY_REPORT', 0, NOW() FROM users WHERE kakao_id = 'dev-user-a';