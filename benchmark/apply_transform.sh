#!/usr/bin/env bash
# 사본에 조건·대상별 변형 적용 (§4.1, §5.2).
# 사용법: apply_transform.sh <dest_dir> <C1|C2> <target_env_path>
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/config.sh"

dest="${1:?dest_dir 필요}"
cond="${2:?조건(C1|C2) 필요}"
target_env="${3:?target_env 경로 필요}"

[[ -d "$dest" ]] || { echo "치명적: $dest 없음" >&2; exit 1; }
case "$cond" in C1|C2) ;; *) echo "치명적: 조건은 C1|C2" >&2; exit 1;; esac
# shellcheck disable=SC1090
source "$target_env"

# 1) 대상 서비스 스텁화(본문 제거·주석 전량 제거·시그니처 보존, §4.1)
cp "$FIXTURES_DIR/$STUB_FIXTURE" "$dest/$SERVICE_REL"
log "스텁 적용: $CLASS_NAME"

# 2) CLAUDE.md/AGENTS.md 균일 제외 — 측정 축은 레퍼런스 지침뿐(§1.2)
find "$dest" -type f \( -name 'CLAUDE.md' -o -name 'AGENTS.md' \) -delete

# 3) 벤치마크 자체 문서·PDCA tmp 제거 — 메타 누출 차단(모든 조건 공통)
rm -rf "$dest/docs/benchmark" "$dest/docs/tmp" "$dest/benchmark"

# 4) 조건별 설계 문서 처리
if [[ "$cond" == "C2" ]]; then
  # C2: Entry-Point.md + 설계 문서군 물리 삭제 → 순수 TDD 기준선(§5.2)
  rm -rf "$dest/docs"
  log "C2: docs/ 전체 삭제(설계 문서 부재)"
else
  log "C1: 설계 문서 유지(docs/ 존재, benchmark·tmp 제외)"
fi

# 5) 히스토리 없는 새 루트 커밋 — 이 시점의 스텁 상태만 기록(§5.2 step2).
#    이후 git show HEAD:... 는 '정답'이 아니라 '스텁'을 돌려준다.
git -C "$dest" init -q
git -C "$dest" -c user.email=bench@local -c user.name=bench add -A
git -C "$dest" -c user.email=bench@local -c user.name=bench commit -q -m "bench stub baseline ($cond/$TARGET_ID)"

log "변형 완료: $dest [$cond/$TARGET_ID]"
