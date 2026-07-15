-- ============================================================
-- docs/sql/qa/teardown.sql
-- 통합 QA 시드 제거 — dev 계정이 만든 모든 데이터를 정리
-- 전제: schema.sql 적용 완료
-- 정본 실행 경로: cd infra/docker/dev && make qa-teardown
--
-- 삭제 순서는 FK 의존의 역순(자식 → 부모)이다. schema.sql의 FK 13개는
-- 전부 기본값 RESTRICT(ON DELETE CASCADE 없음)이므로 순서를 어기면
-- 즉시 제약 위반으로 실패한다.
--
--   chemistry_notifications → colleague_notifications → notifications
--   → chemistry_reports → disc_tests → tests
--   → assessment_tokens → colleagues → peer_codes → coin_transactions → users
--
-- 대상은 kakao_id가 'dev-user-%'인 계정에 한정 (실계정 영향 없음)
-- ============================================================

-- ── 1. chemistry_notifications ────────────────────────────────────────────
-- notifications / chemistry_reports 삭제를 막는 자식. 가장 먼저 제거.
-- 수신자가 dev이거나, 참조 보고서가 dev 소유인 경우 모두 대상

DELETE FROM chemistry_notifications
WHERE id IN (
        SELECT id FROM notifications WHERE user_id IN
            (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%'))
   OR chemistry_report_id IN (
        SELECT id FROM chemistry_reports
        WHERE requester_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
           OR partner_id   IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%'));

-- ── 2. colleague_notifications ────────────────────────────────────────────
-- notifications / colleagues 삭제를 막는 자식.
-- 수신자가 dev이거나, 참조 동료 관계가 dev를 포함하는 경우 모두 대상

DELETE FROM colleague_notifications
WHERE id IN (
        SELECT id FROM notifications WHERE user_id IN
            (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%'))
   OR colleague_id IN (
        SELECT id FROM colleagues
        WHERE user_a_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
           OR user_b_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%'));

-- ── 3. notifications ──────────────────────────────────────────────────────

DELETE FROM notifications WHERE user_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 4. chemistry_reports ──────────────────────────────────────────────────
-- 발행자/대상자 중 한쪽만 dev여도 users 삭제를 막으므로 양방향 조건

DELETE FROM chemistry_reports
WHERE requester_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
   OR partner_id   IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 5. disc_tests ─────────────────────────────────────────────────────────
-- tests JOINED 상속 자식. 부모보다 먼저 제거.

DELETE FROM disc_tests WHERE test_id IN (
    SELECT id FROM tests WHERE user_id IN
        (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%'));

-- ── 6. tests ──────────────────────────────────────────────────────────────

DELETE FROM tests WHERE user_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 7. assessment_tokens ──────────────────────────────────────────────────

DELETE FROM assessment_tokens WHERE subject_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 8. colleagues ─────────────────────────────────────────────────────────
-- user_a_id / user_b_id 양방향 조건 (등록 시 LEAST/GREATEST로 정렬 삽입됨)

DELETE FROM colleagues
WHERE user_a_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
   OR user_b_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 9. peer_codes ─────────────────────────────────────────────────────────

DELETE FROM peer_codes WHERE user_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 10. coin_transactions ─────────────────────────────────────────────────

DELETE FROM coin_transactions WHERE user_id IN
    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');

-- ── 11. users ─────────────────────────────────────────────────────────────

DELETE FROM users WHERE kakao_id LIKE 'dev-user-%';

-- ============================================================
-- 캐시 정리 — 행은 삭제하지 않는다
--
-- disc_cache(81행) / chemistry_cache(6,561행)는 schema.sql이 직접 INSERT하는
-- 고정 행이며 disc_tests·chemistry_reports의 복합 FK 대상이다. 게다가 report
-- 컬럼에는 과금된 LLM 산출물이 들어 있어, 지우면 QA를 반복할 때마다 재과금된다.
-- 따라서 DELETE가 아니라 상태만 되돌린다.
-- ============================================================

-- QA 도중 케미 발행을 중단하면 행이 GENERATING에 멈추고, 다음 QA가 구독자 대기
-- (chemistry.subscriber-wait-timeout-seconds=300)에 5분간 블로킹된다.
-- 교착을 풀되 READY(지불 완료분)는 보존한다.

UPDATE chemistry_cache
SET status = 'NULL', report = NULL, created_at = NULL, updated_at = NULL
WHERE status <> 'READY';

-- disc_cache는 status 컬럼이 없어 교착 상태가 존재하지 않는다. 리셋 대상 아님.
