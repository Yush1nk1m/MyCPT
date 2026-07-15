#!/bin/bash

# 통합 QA 시드 setup/teardown — docs/common/qa-test-design.md §2 시드 운용 규약
# 에러 발생 시 즉시 스크립트 중단
set -e

ENV_FILE="../../../.env.dev"
SQL_DIR="../../../docs/sql"

COMPOSE_CMD="docker compose --env-file $ENV_FILE --profile infra"

# 비밀번호는 컨테이너 안에서 얻는다.
# .env.dev는 compose용 env 파일이지 셸 스크립트가 아니다 — 값에 공백·따옴표·&가 있어
# `source`로 읽으면 셸 문법으로 해석되어 깨진다. compose가 mysql 컨테이너에
# MYSQL_ROOT_PASSWORD를 이미 주입해 두었으므로 그 값을 컨테이너 내부에서 참조한다.
# MYSQL_PWD로 넘겨 커맨드라인 비밀번호 노출 경고도 피한다.
MYSQL_IN_CONTAINER='MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -u root --default-character-set=utf8mb4'

# SQL 파일 실행
run_sql() {
    echo "   ▸ $(basename "$1")"
    $COMPOSE_CMD exec -T mysql sh -c "$MYSQL_IN_CONTAINER mycpt" < "$1"
}

# 인라인 쿼리 실행 (status 전용)
run_query() {
    printf '%s\n' "$1" | $COMPOSE_CMD exec -T mysql sh -c "$MYSQL_IN_CONTAINER --table mycpt"
}

seed_base() {
    run_sql "$SQL_DIR/qa/teardown.sql"
    run_sql "$SQL_DIR/dev_scenario_seed.sql"
}

case "$1" in
    reset)
        echo "🌱 [QA] dev 데이터를 정리하고 기본 시드를 삽입합니다..."
        seed_base
        echo "✅ [QA] 흐름 1~3 준비 완료"
        ;;
    teardown)
        echo "🧹 [QA] dev 계정이 만든 모든 데이터를 제거합니다 (캐시 행은 보존)..."
        run_sql "$SQL_DIR/qa/teardown.sql"
        echo "✅ [QA] 정리 완료"
        ;;
    flow)
        if [ -z "$2" ]; then
            echo "❌ 흐름 번호가 필요합니다. 예: $0 flow 4"
            exit 1
        fi
        echo "🌱 [QA] 흐름 $2 전제를 구성합니다..."
        seed_base
        # 흐름 1~3은 전용 픽스처가 없다 — 기본 시드로 충분 (흐름은 상호 독립)
        found=0
        for f in "$SQL_DIR/qa/flow-$2-"*.sql; do
            if [ -f "$f" ]; then
                run_sql "$f"
                found=1
            fi
        done
        if [ "$found" -eq 0 ]; then
            echo "   ▸ 흐름 $2 전용 픽스처 없음 — 기본 시드로 충분합니다"
        fi
        echo "✅ [QA] 흐름 $2 준비 완료"
        ;;
    status)
        echo "📊 [QA] 현재 dev 데이터 현황입니다..."
        run_query "
            SELECT 'users' AS 테이블, COUNT(*) AS 행 FROM users WHERE kakao_id LIKE 'dev-user-%'
            UNION ALL SELECT 'tests', COUNT(*) FROM tests WHERE user_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
            UNION ALL SELECT 'disc_tests', COUNT(*) FROM disc_tests WHERE test_id IN
                (SELECT id FROM tests WHERE user_id IN
                    (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%'))
            UNION ALL SELECT 'colleagues', COUNT(*) FROM colleagues WHERE user_a_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
                OR user_b_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
            UNION ALL SELECT 'peer_codes', COUNT(*) FROM peer_codes WHERE user_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
            UNION ALL SELECT 'assessment_tokens', COUNT(*) FROM assessment_tokens WHERE subject_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
            UNION ALL SELECT 'coin_transactions', COUNT(*) FROM coin_transactions WHERE user_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
            UNION ALL SELECT 'notifications', COUNT(*) FROM notifications WHERE user_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
            UNION ALL SELECT 'chemistry_reports', COUNT(*) FROM chemistry_reports WHERE requester_id IN
                (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%')
                OR partner_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dev-user-%');"
        echo "📊 [QA] 캐시 적재 현황입니다 (행은 고정, report 생성 여부만 변동)..."
        run_query "
            SELECT 'disc_cache 생성됨' AS 구분, COUNT(*) AS 행 FROM disc_cache WHERE report IS NOT NULL
            UNION ALL SELECT 'disc_cache 미생성', COUNT(*) FROM disc_cache WHERE report IS NULL
            UNION ALL SELECT 'chemistry_cache READY', COUNT(*) FROM chemistry_cache WHERE status = 'READY'
            UNION ALL SELECT 'chemistry_cache 그 외', COUNT(*) FROM chemistry_cache WHERE status <> 'READY';"
        ;;
    *)
        echo "❌ 사용법: $0 {reset|teardown|flow <n>|status}"
        echo "   reset          — 정리 후 기본 시드 삽입 (흐름 1~3)"
        echo "   flow <n>       — 정리 → 기본 시드 → 흐름 n 픽스처 (예: flow 4)"
        echo "   teardown       — dev 데이터 제거 (QA 종료 후)"
        echo "   status         — 현재 dev 데이터 · 캐시 현황"
        exit 1
        ;;
esac
