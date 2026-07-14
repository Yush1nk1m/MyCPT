# 벤치마크 하니스 — 설계 문서 효과 측정

설계 문서: [`../docs/benchmark/benchmark-design.md`](../docs/benchmark/benchmark-design.md)
이 디렉터리는 그 설계의 **드라이버·변형 스크립트** 구현이다.

## 구성

| 파일 | 역할 | 설계 절 |
| --- | --- | --- |
| `config.sh` | 공통 설정(기준 커밋·모델·상한·경로) | §6 |
| `targets/{score,chem}.env` | 대상 서비스·테스트 FQCN 정의 | §3.1 |
| `fixtures/*.stub.java` | 스텁 서비스(본문 제거·주석 제거·시그니처 보존) | §4.1 |
| `fixtures/*.spec.txt` | 제거한 주석 원문 → 프롬프트의 "원래 명세" | §4.2 |
| `make_copy.sh` | 히스토리 제거 사본 생성(`git archive`) | §5.2 |
| `apply_transform.sh` | 스텁 적용 · CLAUDE.md/docs 처리 · `git init` | §4.1·§5.2 |
| `build_prompt.sh` | §4.2 고정 지시문 조립 | §4.2 |
| `run_once.sh` | 단일 런: 사본→변형→헤드리스→오라클→JSONL | §5.1·§3.3 |
| `run_cell.sh` | 한 셀 N회 순차 반복 | §5.4 |
| `emit_row.py` | JSON+오라클 → JSONL 1행, `run_outcome` 분류 | §3.4·§8 |
| `aggregate.py` | 셀별 통계 + C2/C1 효율 배수 | §8 |
| `safety_check.sh` | 메인 저장소 무영향 검증 | §5.3 |
| `report/` | 최종 보고서(HTML·Markdown) + 데이터 생성 스크립트(Shapiro→Welch t/U, scipy 의존) | §8·§3.5 |

## 실행 순서 (§9)

```bash
# 0) 사전 안전성
benchmark/safety_check.sh

# 1) 파일럿 Go/No-Go (§7.3): T-chem C1×3, C2×3
benchmark/run_cell.sh C1 chem 3
benchmark/run_cell.sh C2 chem 3            # 파일럿은 상한 없이 관측
benchmark/aggregate.py benchmark/results/runs.jsonl

# 2) (Go 시) 대상별 C1 10회 → C1_med 확정
benchmark/run_cell.sh C1 score 10
benchmark/run_cell.sh C1 chem 10
#    aggregate.py 의 cost_median 이 대상별 C1_med.

# 3) 상한 = 3 × C1_med 를 인자로 C2 10회
benchmark/run_cell.sh C2 score 10 <3×C1_med_score>
benchmark/run_cell.sh C2 chem  10 <3×C1_med_chem>

# 4) 집계·시각화
benchmark/aggregate.py
benchmark/safety_check.sh                  # 사후 무영향 재확인

# 5) 보고서 데이터 재생성 (scipy 필요: Shapiro-Wilk 후 Welch t / Mann–Whitney U)
cd benchmark/report && python3 gen_report_data.py ../results/runs.jsonl report_data.json
```

> **의존성**: 보고서 생성(`report/gen_report_data.py`)은 `scipy`를 사용한다(정규성 검정·검정 통계). 하니스 실행 자체는 불필요.

## 중요 — 실행 권한과 주체

- 각 런은 `claude -p ... --permission-mode bypassPermissions` 로 **무인 에이전트 루프**를 돌린다.
  Claude Code 세션 **안에서는** 안전장치가 이 실행을 차단한다. 따라서 파일럿·본실행은
  **사용자가 자신의 터미널에서 직접** 구동해야 한다(에이전트가 대신 실행하지 않음).
- 각 런은 gradle 테스트(Testcontainers)를 돌리므로 **Docker 데몬**이 필요하다. 순차 실행 권장(§5.4).

## 설계 문서 대비 구현 편차(사용자 Check 필요)

이 CLI 버전(2.1.207) 실측으로 §3.4·§5.2에 반영해야 할 편차:

1. **턴 절대 상한**: `--max-turns` 플래그 부재 → 대신 `--max-budget-usd`(비용 하드 상한)
   로 3×C1_med 상한을 **직접 강제**. 턴은 사후 관측치로만 기록(`num_turns`).
2. **`stop_reason` 컬럼 충돌**: CLI JSON이 자체 `stop_reason`(모델 종료사유)을 이미 내보냄
   → 런 수준 판정은 **`run_outcome`** 로 별도 기록(success/test_red/capped_cost/wall_timeout/cli_error).
3. **CLAUDE.md 균일 제외**: 사본에서 모든 `CLAUDE.md`/`AGENTS.md` 삭제 + `--setting-sources project`
   (user 메모리 제외)로 §1.2 실현.
4. **메타 누출 차단**: 사본에서 `docs/benchmark`·`docs/tmp` 를 **모든 조건에서** 삭제
   (벤치마크 자기 자신을 대상 에이전트가 읽지 못하게).
