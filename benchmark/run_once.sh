#!/usr/bin/env bash
# 단일 런: 사본 생성 → 변형 → 헤드리스 실행 → 오라클 → JSONL 1행 기록.
# 사용법: run_once.sh <C1|C2> <score|chem> <rep#> [cost_cap_usd]
#   cost_cap_usd 생략/0 → 비용 상한 없음(C1 교정 런).
# 환경변수: KEEP_COPIES=1 이면 작업 사본 보존(디버그용).
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/config.sh"

cond="${1:?조건(C1|C2)}"; target_id="${2:?대상(score|chem)}"; rep="${3:?반복#}"
cost_cap="${4:-0}"
target_env="$TARGETS_DIR/${target_id}.env"
[[ -f "$target_env" ]] || { echo "치명적: 대상 정의 없음 $target_env" >&2; exit 1; }
# shellcheck disable=SC1090
source "$target_env"

ts="$(date +%Y%m%d-%H%M%S)"
run_name="${target_id}_${cond}_r${rep}_${ts}"
dest="$WORK_BASE/$run_name"
raw_dir="$RESULTS_DIR/raw"; mkdir -p "$raw_dir"

log "▶ 런 시작: $run_name (model=$MODEL, cost_cap=${cost_cap})"

# --- 사본 생성 + 변형 ---
"$BENCH_ROOT/make_copy.sh" "$dest"
"$BENCH_ROOT/apply_transform.sh" "$dest" "$cond" "$target_env"
prompt="$("$BENCH_ROOT/build_prompt.sh" "$target_env")"

# --- 헤드리스 실행(§5.1) ---
cap_flag=()
if [[ "$cost_cap" != "0" && -n "$cost_cap" ]]; then
  cap_flag=(--max-budget-usd "$cost_cap")   # §3.4 비용 상대 상한을 CLI에서 강제
fi

run_json="$dest/run.json"; run_err="$dest/run.err"
set +e
( cd "$dest" && timeout "$WALL_TIMEOUT" \
    claude -p "$prompt" \
      --output-format json \
      --model "$MODEL" \
      --setting-sources "$SETTING_SOURCES" \
      --permission-mode "$PERMISSION_MODE" \
      "${cap_flag[@]}" ) >"$run_json" 2>"$run_err"
cli_rc=$?
set -e
log "헤드리스 종료(rc=$cli_rc)"

# --- 오라클(§3.3): wall_timeout/capped 가 아닐 때만 기준 테스트 실행 ---
test_rc=-1
if [[ "$cli_rc" -ne 124 ]]; then
  set +e
  ( cd "$dest/backend" && ./gradlew test --tests "$TEST_FQCN" --console=plain -q ) \
      >"$dest/oracle.log" 2>&1
  test_rc=$?
  set -e
  log "오라클 종료(rc=$test_rc)"
else
  log "벽시계 상한 초과 → 오라클 생략"
fi

# --- 1행 기록(JSONL) ---
RUN_NAME="$run_name" COND="$cond" TARGET_ID="$target_id" REP="$rep" TS="$ts" \
CLI_RC="$cli_rc" TEST_RC="$test_rc" COST_CAP="$cost_cap" \
MODEL="$MODEL" WALL_TIMEOUT="$WALL_TIMEOUT" \
BASE_SHA="$(git -C "$REPO_ROOT" rev-parse --short "$BASE_COMMIT")" \
python3 "$BENCH_ROOT/emit_row.py" "$run_json" >>"$RUNS_JSONL"
log "기록 완료 → $RUNS_JSONL"

# --- 원자 JSON 보존 + 사본 정리 ---
cp -f "$run_json" "$raw_dir/$run_name.json" 2>/dev/null || true
cp -f "$dest/oracle.log" "$raw_dir/$run_name.oracle.log" 2>/dev/null || true
if [[ "${KEEP_COPIES:-0}" != "1" ]]; then
  rm -rf "$dest"
  log "사본 삭제(KEEP_COPIES=1 로 보존 가능)"
fi
