#!/usr/bin/env python3
"""기존 runs.jsonl 행에 v0.5 신규 지표를 raw/<run_name>.json에서 소급 계산해 병합한다.
원본 필드(run_outcome·test_passed·test_rc 등, 셸 컨텍스트 유래)는 보존한다.
사용법: backfill.py [runs.jsonl] [raw_dir]
"""
import json, os, sys

runs_path = sys.argv[1] if len(sys.argv) > 1 else "results/runs.jsonl"
raw_dir = sys.argv[2] if len(sys.argv) > 2 else "results/raw"

def enrich(row):
    raw_path = os.path.join(raw_dir, f"{row['run_name']}.json")
    try:
        d = json.load(open(raw_path))
    except Exception:
        d = {}
    usage = d.get("usage", {}) or {}
    dm, da = d.get("duration_ms"), d.get("duration_api_ms")
    cost, turns = d.get("total_cost_usd"), d.get("num_turns")
    tok = [usage.get(k) for k in ("input_tokens", "output_tokens",
           "cache_creation_input_tokens", "cache_read_input_tokens")]
    row["cost_per_turn"] = (cost / turns) if (cost is not None and turns) else None
    row["total_billable_tokens"] = sum(t for t in tok if t is not None) if any(t is not None for t in tok) else None
    row["duration_api_ms"] = da
    row["local_ms"] = (dm - da) if (dm is not None and da is not None) else None
    row["model_costs"] = {m: mu.get("costUSD") for m, mu in (d.get("modelUsage", {}) or {}).items()}
    return row

rows = [json.loads(l) for l in open(runs_path) if l.strip()]
rows = [enrich(r) for r in rows]
with open(runs_path, "w") as f:
    for r in rows:
        f.write(json.dumps(r, ensure_ascii=False) + "\n")
print(f"소급 보강 완료: {len(rows)}행 → {runs_path}")
n_enriched = sum(1 for r in rows if r.get("local_ms") is not None)
print(f"  신규 지표 채워진 행: {n_enriched}/{len(rows)} (나머지는 raw 없음/cli_error)")
