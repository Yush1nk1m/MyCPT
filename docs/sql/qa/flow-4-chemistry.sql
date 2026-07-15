-- ============================================================
-- docs/sql/qa/flow-4-chemistry.sql
-- E2E 흐름 4(케미 여정) 전제 픽스처
-- 전제: qa/teardown.sql → dev_scenario_seed.sql 선행 실행
-- 정본 실행 경로: cd infra/docker/dev && make qa-flow FLOW=4
-- 시나리오: docs/common/qa-test-design.md §5 흐름 4
--
-- 케미 발행은 양쪽 모두 DISC 결과를 보유해야 가능하다. 기본 시드는 계정과
-- 동료 관계만 만들 뿐 결과를 만들지 않으므로 여기서 심는다.
--
-- 매 QA 반복마다 24문항을 손으로 두 번 찍는 것은 검증 가치 없이 시간만 쓴다.
-- "검사 → 결과 → LLM" 경로는 흐름 1·2가 실제 UI로 검증하므로 커버리지 손실이 없다.
--
-- 안전성: disc_tests → disc_cache(d,i,s,c) 복합 FK는 schema.sql이 disc_cache
-- 81행을 미리 INSERT하므로 항상 만족된다. (report=NULL = 미생성 상태)
--
-- 점수 불변식(ScoringService.validate): 개별 원점수 -24~+48, 합계 = 24
-- 버킷 경계(ScoringService.toBucket): ≤-5 → 1, -4~+11 → 2, ≥+12 → 3
--
--   | 계정   |   D |   I |   S |  C | 합계 | 버킷         |
--   | user-a | +20 |  +8 |  -8 | +4 |  24  | (3, 2, 1, 2) |
--   | user-c |  -6 | +16 | +10 | +4 |  24  | (1, 3, 2, 2) |
--
-- 두 조합 (3,2,1,2) × (1,3,2,2)에 대응하는 chemistry_cache 행도 이미 존재한다.
-- ============================================================

-- ── 1. user-a 자기 평정 결과 ──────────────────────────────────────────────

INSERT INTO tests (user_id, rater_type, dtype, label, created_at)
SELECT id, 'SELF', 'DISC', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM users WHERE kakao_id = 'dev-user-a';

INSERT INTO disc_tests (test_id, d_score, i_score, s_score, c_score,
                        d_bucket, i_bucket, s_bucket, c_bucket)
SELECT t.id, 20, 8, -8, 4, 3, 2, 1, 2
FROM tests t JOIN users u ON u.id = t.user_id
WHERE u.kakao_id = 'dev-user-a';

-- ── 2. user-c 자기 평정 결과 ──────────────────────────────────────────────

INSERT INTO tests (user_id, rater_type, dtype, label, created_at)
SELECT id, 'SELF', 'DISC', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM users WHERE kakao_id = 'dev-user-c';

INSERT INTO disc_tests (test_id, d_score, i_score, s_score, c_score,
                        d_bucket, i_bucket, s_bucket, c_bucket)
SELECT t.id, -6, 16, 10, 4, 1, 3, 2, 2
FROM tests t JOIN users u ON u.id = t.user_id
WHERE u.kakao_id = 'dev-user-c';

-- ── 3. 삽입 확인 ─────────────────────────────────────────────────────────

SELECT u.kakao_id, t.rater_type, t.dtype,
       d.d_score, d.i_score, d.s_score, d.c_score,
       (d.d_score + d.i_score + d.s_score + d.c_score) AS score_sum,
       CONCAT(d.d_bucket, d.i_bucket, d.s_bucket, d.c_bucket) AS buckets
FROM disc_tests d
    JOIN tests t ON t.id = d.test_id
    JOIN users u ON u.id = t.user_id
WHERE u.kakao_id LIKE 'dev-user-%';

SELECT kakao_id, nickname, coins FROM users WHERE kakao_id IN ('dev-user-a', 'dev-user-c');

-- ============================================================
-- 범용 유틸 — 코인 강제 조정 (앱에서 보장 못하는 상태 재현용)
-- E2E-Chemistry-코인부족-차단 검증 시 주석을 해제해 사용한다.
-- 기본 시드가 3개를 지급하므로 평시에는 조정 불필요.
-- ============================================================
-- UPDATE users SET coins = 0 WHERE kakao_id = 'dev-user-a';
-- INSERT INTO coin_transactions (user_id, amount, reason, balance_after, created_at)
-- SELECT id, -3, 'CHEMISTRY_REPORT', 0, NOW() FROM users WHERE kakao_id = 'dev-user-a';
