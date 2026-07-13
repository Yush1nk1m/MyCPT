#!/usr/bin/env bash
# 히스토리 제거 사본 생성 (§5.2). 사용법: make_copy.sh <dest_dir>
# 고정 커밋의 '작업 트리 파일만' 사본에 복사한다. .git은 포함하지 않으므로
# git show <base>:... 로 원본 구현/문서를 복원할 수 없다(몰래 읽기 원천 차단).
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/config.sh"

dest="${1:?dest_dir 필요}"

if [[ -e "$dest" ]]; then
  echo "치명적: $dest 이미 존재" >&2; exit 1
fi
mkdir -p "$dest"

# .git 없이 커밋 시점의 트리 스냅샷만 추출(read-only, 원본 .git에 쓰지 않음, §5.3).
git -C "$REPO_ROOT" archive --format=tar "$BASE_COMMIT" | tar -x -C "$dest"

log "사본 생성 완료: $dest (base=$(git -C "$REPO_ROOT" rev-parse --short "$BASE_COMMIT"))"
