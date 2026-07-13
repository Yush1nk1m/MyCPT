#!/usr/bin/env bash
# 한 셀(조건×대상)을 N회 순차 반복(§5.4 — Testcontainers 자원 경합 회피).
# 사용법: run_cell.sh <C1|C2> <score|chem> <N> [cost_cap_usd] [start_rep]
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/config.sh"

cond="${1:?}"; target_id="${2:?}"; n="${3:?}"; cost_cap="${4:-0}"; start="${5:-1}"

log "== 셀 실행: $cond/$target_id × $n (cost_cap=$cost_cap, start_rep=$start) =="
end=$(( start + n - 1 ))
for (( rep=start; rep<=end; rep++ )); do
  if ! "$BENCH_ROOT/run_once.sh" "$cond" "$target_id" "$rep" "$cost_cap"; then
    log "경고: rep $rep 런 실패(스크립트 오류). 계속 진행."
  fi
done
log "== 셀 완료: $cond/$target_id =="
