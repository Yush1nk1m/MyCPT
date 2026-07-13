#!/usr/bin/env python3
"""run.json + 환경변수를 합쳐 JSONL 원자료 1행을 stdout으로 출력한다.
run_outcome 분류(§3.4): success | test_red | capped_cost | wall_timeout | cli_error
(주의: CLI JSON의 'stop_reason'은 모델 종료사유이므로, 런 수준 판정은 run_outcome로 별도 기록)
"""
import json, os, sys

run_json_path = sys.argv[1] if len(sys.argv) > 1 else ""

def env(k, d=""):
    return os.environ.get(k, d)

cli_rc = int(env("CLI_RC", "-1"))
test_rc = int(env("TEST_RC", "-1"))
cost_cap = float(env("COST_CAP", "0") or 0)

data = {}
try:
    with open(run_json_path) as f:
        data = json.load(f)
except Exception:
    data = {}

usage = data.get("usage", {}) or {}
cost = data.get("total_cost_usd")
cli_is_error = bool(data.get("is_error", False))
terminal_reason = str(data.get("terminal_reason", "") or "")

# --- 신규 지표(v0.5): 시간 분해·총 처리토큰·턴당 비용·모델 분해 ---
duration_ms = data.get("duration_ms")
duration_api_ms = data.get("duration_api_ms")
local_ms = (duration_ms - duration_api_ms) if (duration_ms is not None and duration_api_ms is not None) else None

_tok = [usage.get(k) for k in
        ("input_tokens", "output_tokens", "cache_creation_input_tokens", "cache_read_input_tokens")]
total_billable_tokens = sum(t for t in _tok if t is not None) if any(t is not None for t in _tok) else None

num_turns = data.get("num_turns")
cost_per_turn = (cost / num_turns) if (cost is not None and num_turns) else None

# 모델별 비용 분해(§6 통제 확인). {모델: costUSD}
model_costs = {m: mu.get("costUSD") for m, mu in (data.get("modelUsage", {}) or {}).items()}

test_passed = None
if test_rc == 0:
    test_passed = True
elif test_rc >= 1:
    test_passed = False

# --- run_outcome 분류 ---
if cli_rc == 124:
    outcome = "wall_timeout"
elif not data:
    outcome = "cli_error"
elif cost_cap > 0 and cost is not None and (
        cost >= 0.98 * cost_cap or "budget" in terminal_reason.lower()):
    outcome = "capped_cost"
elif test_rc == 0:
    outcome = "success"
elif test_rc >= 1:
    outcome = "test_red"
else:
    # 오라클 미실행인데 timeout/capped도 아님 → 비정상
    outcome = "cli_error"

row = {
    "run_name": env("RUN_NAME"),
    "target": env("TARGET_ID"),
    "condition": env("COND"),
    "rep": int(env("REP", "0")),
    "ts": env("TS"),
    "run_outcome": outcome,
    "test_passed": test_passed,
    "total_cost_usd": cost,
    "cost_per_turn": cost_per_turn,
    "num_turns": num_turns,
    "input_tokens": usage.get("input_tokens"),
    "output_tokens": usage.get("output_tokens"),
    "cache_creation_input_tokens": usage.get("cache_creation_input_tokens"),
    "cache_read_input_tokens": usage.get("cache_read_input_tokens"),
    "total_billable_tokens": total_billable_tokens,
    "duration_ms": duration_ms,
    "duration_api_ms": duration_api_ms,
    "local_ms": local_ms,
    "model_costs": model_costs,
    "cli_rc": cli_rc,
    "cli_is_error": cli_is_error,
    "cli_subtype": data.get("subtype"),
    "cli_stop_reason": data.get("stop_reason"),
    "cli_terminal_reason": terminal_reason or None,
    "test_rc": test_rc,
    "cost_cap_usd": cost_cap,
    "model": env("MODEL"),
    "wall_timeout": env("WALL_TIMEOUT"),
    "base_commit": env("BASE_SHA"),
    "session_id": data.get("session_id"),
}
print(json.dumps(row, ensure_ascii=False))
