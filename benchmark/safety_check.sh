#!/usr/bin/env bash
# §5.3 메인 저장소 무영향 검증. 파일럿/본실행 전후로 실행해 clean 유지를 입증한다.
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/config.sh"

fail=0
echo "== 메인 저장소 안전성 검증: $REPO_ROOT =="

# 1) 작업 트리 clean (벤치마크 산출물은 무시 목록으로 제외되어야 함)
dirty="$(git -C "$REPO_ROOT" status --porcelain)"
if [[ -n "$dirty" ]]; then
  echo "✗ 작업 트리 변경 감지:"; echo "$dirty"; fail=1
else
  echo "✓ git status clean"
fi

# 2) worktree 미추가(사본이 worktree를 만들지 않았는지)
wt="$(git -C "$REPO_ROOT" worktree list | wc -l)"
if [[ "$wt" -gt 1 ]]; then
  echo "✗ 예상치 못한 worktree:"; git -C "$REPO_ROOT" worktree list; fail=1
else
  echo "✓ worktree 항목 1개(메인)"
fi

# 3) 오브젝트 DB 무결성
if git -C "$REPO_ROOT" fsck --no-progress >/dev/null 2>&1; then
  echo "✓ git fsck 정상"
else
  echo "✗ git fsck 오류"; fail=1
fi

# 4) 사본이 트리 밖인지 재확인
case "$WORK_BASE"/ in
  "$REPO_ROOT"/*) echo "✗ WORK_BASE가 메인 트리 안: $WORK_BASE"; fail=1 ;;
  *) echo "✓ WORK_BASE 트리 밖: $WORK_BASE" ;;
esac

[[ "$fail" -eq 0 ]] && echo "== PASS ==" || { echo "== FAIL =="; exit 1; }
