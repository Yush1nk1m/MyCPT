#!/usr/bin/env bash
# 벤치마크 공통 설정 — 다른 스크립트가 source 한다.
# 설계 문서: docs/benchmark/benchmark-design.md
set -euo pipefail

BENCH_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$BENCH_ROOT/.." && pwd)"

# 고정 기준 커밋(§6). 모든 사본은 여기서 출발. 서비스가 원본(정답) 상태인 커밋이어야 한다.
BASE_COMMIT="${BASE_COMMIT:-HEAD}"

# 측정 대상 모델(§6). 사용자 통상 작업 모델 Sonnet 고정.
MODEL="${MODEL:-sonnet}"

# 벽시계 상한(§3.4). timeout(1) 형식.
WALL_TIMEOUT="${WALL_TIMEOUT:-20m}"

# 헤드리스 권한 모드. 40런 무인 실행에는 bypassPermissions 필요(사용자 터미널에서 실행).
PERMISSION_MODE="${PERMISSION_MODE:-bypassPermissions}"

# 설정 로딩 통제(§1.2). user 제외 → 사용자 레벨 메모리/설정 오염 최소화.
SETTING_SOURCES="${SETTING_SOURCES:-project}"

# 런 사본을 두는 위치 — 반드시 메인 저장소 트리 밖(§5.3).
WORK_BASE="${WORK_BASE:-$HOME/.mycpt-bench/runs}"

# 원자료(JSONL) 및 집계 산출물 위치.
RESULTS_DIR="${RESULTS_DIR:-$BENCH_ROOT/results}"
RUNS_JSONL="${RUNS_JSONL:-$RESULTS_DIR/runs.jsonl}"

# gradle 의존성 캐시 공유(사본마다 재다운로드 방지, 속도 안정화).
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.mycpt-bench/gradle-home}"

TARGETS_DIR="$BENCH_ROOT/targets"
FIXTURES_DIR="$BENCH_ROOT/fixtures"

mkdir -p "$WORK_BASE" "$RESULTS_DIR" "$GRADLE_USER_HOME"

# 사본 경로가 메인 트리 안이면 즉시 중단(안전장치, §5.3).
case "$WORK_BASE"/ in
  "$REPO_ROOT"/*)
    echo "치명적: WORK_BASE($WORK_BASE)가 메인 저장소 트리 안에 있습니다. 트리 밖으로 지정하세요." >&2
    exit 1 ;;
esac

log() { printf '[%s] %s\n' "$(date +%H:%M:%S)" "$*" >&2; }
