#!/usr/bin/env python3
"""runs.jsonl → report_data.json (보고서 임베드용 셀별 요약·사분위·Mann–Whitney U).
사용법: gen_report_data.py [runs.jsonl] [out.json]
scipy 미사용(정규근사·동점보정 U 검정 직접 구현).
"""
import json, math, sys, statistics as st
from collections import defaultdict, Counter

runs_path = sys.argv[1] if len(sys.argv) > 1 else "../results/runs.jsonl"
out_path = sys.argv[2] if len(sys.argv) > 2 else "report_data.json"

rows = [json.loads(l) for l in open(runs_path) if l.strip()]
cells = defaultdict(list)
allrows = defaultdict(list)
for r in rows:
    allrows[(r["target"], r["condition"])].append(r)
    if r["run_outcome"] == "success" and r.get("total_cost_usd") is not None:
        cells[(r["target"], r["condition"])].append(r)


def mwu(a, b):
    """양측 Mann–Whitney U, 정규근사 + 동점보정 → p."""
    n1, n2 = len(a), len(b); N = n1 + n2
    allv = sorted([(v, 0) for v in a] + [(v, 1) for v in b])
    ranks = [0.0] * N; i = 0
    while i < N:
        j = i
        while j + 1 < N and allv[j + 1][0] == allv[i][0]:
            j += 1
        avg = (i + j + 2) / 2.0
        for k in range(i, j + 1):
            ranks[k] = avg
        i = j + 1
    R1 = sum(ranks[k] for k in range(N) if allv[k][1] == 0)
    U1 = R1 - n1 * (n1 + 1) / 2.0; mu = n1 * n2 / 2.0
    tie = Counter(v for v, _ in allv)
    tcorr = sum(t**3 - t for t in tie.values())
    sigma = math.sqrt(n1 * n2 / 12.0 * ((N + 1) - tcorr / (N * (N - 1))))
    if sigma == 0:
        return 1.0
    z = (abs(U1 - mu) - 0.5) / sigma
    return 2 * (1 - 0.5 * (1 + math.erf(z / math.sqrt(2))))


def scale(key, v):
    return v / 1000 if key.endswith("_ms") else v


def summ(vals):
    vals = sorted(vals); n = len(vals)
    def q(p):
        idx = p * (n - 1); lo = int(idx); hi = min(lo + 1, n - 1)
        return vals[lo] + (idx - lo) * (vals[hi] - vals[lo])
    return dict(n=n, min=vals[0], max=vals[-1], q1=q(.25), med=q(.5), q3=q(.75),
                mean=st.mean(vals), sd=st.pstdev(vals) if n > 1 else 0,
                values=[round(v, 4) for v in vals])


METRICS = [("cost", "total_cost_usd", "비용", "$"),
           ("dur", "duration_ms", "벽시계 시간", "s"),
           ("api", "duration_api_ms", "API 시간", "s"),
           ("local", "local_ms", "로컬 도구시간", "s"),
           ("out", "output_tokens", "출력 토큰", "tok"),
           ("bill", "total_billable_tokens", "총 처리토큰", "tok"),
           ("turns", "num_turns", "턴", "turn")]
TARGETS = [("chem", "T-chem · 동시성·캐시·실패복구"),
           ("score", "T-score · 순수 도메인 로직")]

out = {"targets": {}}
for target, tlabel in TARGETS:
    c1, c2 = cells[(target, "C1")], cells[(target, "C2")]
    tobj = {"label": tlabel, "n1": len(c1), "n2": len(c2),
            "n1_total": len(allrows[(target, "C1")]),
            "n2_total": len(allrows[(target, "C2")]), "metrics": {}}
    for mkey, jkey, mlabel, unit in METRICS:
        a = [scale(jkey, r[jkey]) for r in c1 if r.get(jkey) is not None]
        b = [scale(jkey, r[jkey]) for r in c2 if r.get(jkey) is not None]
        p = mwu(a, b); sa, sb = summ(a), summ(b)
        tobj["metrics"][mkey] = {
            "label": mlabel, "unit": unit, "C1": sa, "C2": sb,
            "ratio": sb["med"] / sa["med"] if sa["med"] else None, "p": p,
            "sig": "***" if p < 0.01 else "**" if p < 0.05 else "*" if p < 0.1 else "ns"}
    out["targets"][target] = tobj
out["meta"] = {"total_runs": len(rows),
               "cli_error": sum(1 for r in rows if r["run_outcome"] == "cli_error"),
               "capped": sum(1 for r in rows if r["run_outcome"] == "capped_cost"),
               "wall_timeout": sum(1 for r in rows if r["run_outcome"] == "wall_timeout"),
               "model": "claude-sonnet-5", "base_commit": rows[0]["base_commit"]}
with open(out_path, "w") as f:
    json.dump(out, f, ensure_ascii=False)
print(f"생성 완료: {out_path} ({len(rows)} 런)")
