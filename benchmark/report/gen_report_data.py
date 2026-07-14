#!/usr/bin/env python3
"""runs.jsonl → report_data.json (보고서 임베드용 셀별 요약·사분위·유의도 검정).
사용법: gen_report_data.py [runs.jsonl] [out.json]

유의도(설계 문서 §3.5): 각 지표·조건군에 Shapiro-Wilk 정규성 검정(α=.05) 후
  두 군 모두 정규면 Welch t-검정(양측, 등분산 미가정), 아니면 Mann–Whitney U(양측).
기술통계량 정렬: headline 배수(ratio)는 선택된 검정과 일치 — 정규(t)면 평균비, 비정규(U)면 중앙비.
의존성: scipy(Shapiro-Wilk·검정).
"""
import json, sys, statistics as st
from collections import defaultdict
from scipy import stats

runs_path = sys.argv[1] if len(sys.argv) > 1 else "../results/runs.jsonl"
out_path = sys.argv[2] if len(sys.argv) > 2 else "report_data.json"

rows = [json.loads(l) for l in open(runs_path) if l.strip()]
cells = defaultdict(list)
allrows = defaultdict(list)
for r in rows:
    allrows[(r["target"], r["condition"])].append(r)
    if r["run_outcome"] == "success" and r.get("total_cost_usd") is not None:
        cells[(r["target"], r["condition"])].append(r)


def choose_test(a, b):
    """각 군 Shapiro-Wilk(α=.05) → 둘 다 정규면 Welch t, 아니면 MWU.
    반환: (test, p, sw_a, sw_b). test ∈ {welch_t, mwu}."""
    sw_a = float(stats.shapiro(a).pvalue) if len(a) >= 3 else None
    sw_b = float(stats.shapiro(b).pvalue) if len(b) >= 3 else None
    if sw_a is not None and sw_b is not None and sw_a >= 0.05 and sw_b >= 0.05:
        _, p = stats.ttest_ind(a, b, equal_var=False); test = "welch_t"
    else:
        _, p = stats.mannwhitneyu(a, b, alternative="two-sided"); test = "mwu"
    return test, float(p), sw_a, sw_b


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
        test, p, sw_a, sw_b = choose_test(a, b)
        sa, sb = summ(a), summ(b)
        stat = "mean" if test == "welch_t" else "median"
        ratio_med = sb["med"] / sa["med"] if sa["med"] else None
        ratio_mean = sb["mean"] / sa["mean"] if sa["mean"] else None
        ratio = ratio_mean if stat == "mean" else ratio_med  # headline = 검정 정렬
        tobj["metrics"][mkey] = {
            "label": mlabel, "unit": unit, "C1": sa, "C2": sb,
            "test": test, "stat": stat, "sw_c1": sw_a, "sw_c2": sw_b,
            "ratio": ratio, "ratio_med": ratio_med, "ratio_mean": ratio_mean, "p": p,
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
