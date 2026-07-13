#!/usr/bin/env python3
"""runs.jsonl → 셀별 집계표(CSV) + 요약(stdout) + C2/C1 효율 배수(§8).
사용법: aggregate.py [runs.jsonl] [out.csv]
비용·토큰·턴 통계는 run_outcome=='success' 런에 한정(§3.3).
성공률·capped율·timeout율은 전체 런 기준.
"""
import csv, json, statistics as st, sys
from collections import defaultdict

runs_path = sys.argv[1] if len(sys.argv) > 1 else "results/runs.jsonl"
out_csv = sys.argv[2] if len(sys.argv) > 2 else "results/summary.csv"

rows = []
with open(runs_path) as f:
    for line in f:
        line = line.strip()
        if line:
            rows.append(json.loads(line))

cells = defaultdict(list)  # (target, condition) -> [row]
for r in rows:
    cells[(r["target"], r["condition"])].append(r)

def stat(vals):
    vals = [v for v in vals if v is not None]
    if not vals:
        return {"n": 0, "median": None, "mean": None, "std": None}
    return {
        "n": len(vals),
        "median": round(st.median(vals), 6),
        "mean": round(st.mean(vals), 6),
        "std": round(st.pstdev(vals), 6) if len(vals) > 1 else 0.0,
    }

summary = {}
fields = ["target", "condition", "n_total", "n_success", "n_metrics",
          "success_rate", "capped_rate", "wall_timeout_rate", "cli_error_rate",
          "cost_median", "cost_mean", "cost_std",
          "cost_per_turn_median",
          "tokens_median", "tokens_mean", "tokens_std",
          "billable_tokens_median",
          "turns_median", "turns_mean", "turns_std",
          "duration_s_median", "api_s_median", "local_s_median"]

def tot_tokens(r):
    i, o = r.get("input_tokens"), r.get("output_tokens")
    if i is None and o is None:
        return None
    return (i or 0) + (o or 0)

table = []
for (target, cond), rs in sorted(cells.items()):
    succ = [r for r in rs if r["run_outcome"] == "success"]
    n = len(rs)
    def rate(name):
        return round(sum(1 for r in rs if r["run_outcome"] == name) / n, 4) if n else 0
    # 지표가 실제로 수집된 성공 런(비용 None 인 cli_error 유실 제외)
    metr = [r for r in succ if r.get("total_cost_usd") is not None]
    cost = stat([r.get("total_cost_usd") for r in metr])
    cpt = stat([r.get("cost_per_turn") for r in metr])
    toks = stat([tot_tokens(r) for r in metr])
    billable = stat([r.get("total_billable_tokens") for r in metr])
    turns = stat([r.get("num_turns") for r in metr])
    dur = stat([(r.get("duration_ms") or 0) / 1000 for r in metr if r.get("duration_ms")])
    api = stat([(r.get("duration_api_ms") or 0) / 1000 for r in metr if r.get("duration_api_ms")])
    local = stat([(r.get("local_ms") or 0) / 1000 for r in metr if r.get("local_ms") is not None])
    row = {
        "target": target, "condition": cond,
        "n_total": n, "n_success": len(succ), "n_metrics": len(metr),
        "success_rate": round(len(succ) / n, 4) if n else 0,
        "capped_rate": rate("capped_cost"),
        "wall_timeout_rate": rate("wall_timeout"),
        "cli_error_rate": rate("cli_error"),
        "cost_median": cost["median"], "cost_mean": cost["mean"], "cost_std": cost["std"],
        "cost_per_turn_median": cpt["median"],
        "tokens_median": toks["median"], "tokens_mean": toks["mean"], "tokens_std": toks["std"],
        "billable_tokens_median": billable["median"],
        "turns_median": turns["median"], "turns_mean": turns["mean"], "turns_std": turns["std"],
        "duration_s_median": dur["median"], "api_s_median": api["median"], "local_s_median": local["median"],
    }
    table.append(row)
    summary[(target, cond)] = row

with open(out_csv, "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=fields)
    w.writeheader()
    for row in table:
        w.writerow(row)

# --- 콘솔 요약 + C2/C1 효율 배수 ---
print(f"\n집계 원자료: {runs_path}  (총 {len(rows)} 런)")
print(f"집계표 저장: {out_csv}\n")
hdr = f"{'대상':<7}{'조건':<5}{'성공':>7}{'비용중앙':>11}{'토큰중앙':>11}{'턴중앙':>8}{'capped':>8}"
print(hdr); print("-" * len(hdr))
for row in table:
    succ = f"{row['n_success']}/{row['n_total']}"
    cost = "" if row["cost_median"] is None else f"{row['cost_median']:.4f}"
    toks = "" if row["tokens_median"] is None else str(int(row["tokens_median"]))
    turns = "" if row["turns_median"] is None else str(row["turns_median"])
    print(f"{row['target']:<7}{row['condition']:<5}{succ:>7}{cost:>11}{toks:>11}{turns:>8}{row['capped_rate']:>8}")

# 시간 분해 + 총 처리토큰 + 지표수집률(v0.5)
hdr2 = f"{'대상':<7}{'조건':<5}{'지표n':>6}{'벽시계s':>9}{'API s':>8}{'로컬s':>8}{'billable':>11}{'턴당$':>9}"
print("\n" + hdr2); print("-" * len(hdr2))
for row in table:
    def g(k, f="{}"):
        v = row.get(k)
        return "" if v is None else f.format(v)
    print(f"{row['target']:<7}{row['condition']:<5}"
          f"{str(row['n_metrics'])+'/'+str(row['n_success']):>6}"
          f"{g('duration_s_median','{:.0f}'):>9}{g('api_s_median','{:.0f}'):>8}"
          f"{g('local_s_median','{:.0f}'):>8}"
          f"{('' if row['billable_tokens_median'] is None else str(int(row['billable_tokens_median']))):>11}"
          f"{g('cost_per_turn_median','{:.4f}'):>9}")

print("\n== C2/C1 효율 배수(중앙값 기준, §8 대비 분석) ==")
for target in sorted({t for (t, _) in summary}):
    c1 = summary.get((target, "C1")); c2 = summary.get((target, "C2"))
    if not c1 or not c2:
        continue
    def ratio(k):
        a, b = c2.get(k), c1.get(k)
        return f"{a / b:.2f}×" if a and b else "N/A"
    print(f"  {target}: 비용 {ratio('cost_median')}, billable토큰 {ratio('billable_tokens_median')}, "
          f"턴 {ratio('turns_median')}, 벽시계 {ratio('duration_s_median')}, "
          f"API {ratio('api_s_median')}, 로컬 {ratio('local_s_median')}")
    if c2["capped_rate"] > 0:
        print(f"      (C2 capped {c2['capped_rate']*100:.0f}% → 최소 3배 잠정결론 후보)")
    if c2["n_metrics"] < 5 or c1["n_metrics"] < 5:
        print(f"      ⚠️ 표본 부족(C1 n={c1['n_metrics']}, C2 n={c2['n_metrics']}) — 방향성만, 확정 보류")
print()
