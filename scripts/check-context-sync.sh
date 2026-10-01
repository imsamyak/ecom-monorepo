#!/usr/bin/env bash
# Fails if code changed in a module (or the repo root) without that owner's HISTORY.md changing.
# Usage: scripts/check-context-sync.sh            check staged changes (pre-commit)
#        scripts/check-context-sync.sh <base-ref> check everything on HEAD since <base-ref> (review)
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

if [ $# -ge 1 ]; then
  changed=$(git diff --name-only "$1"...HEAD)
else
  changed=$(git diff --cached --name-only)
fi
[ -z "$changed" ] && exit 0

# An owner is the closest parent directory holding a CONTEXT.md; otherwise the repo root.
owner_of() {
  local d
  d=$(dirname "$1")
  while [ "$d" != "." ] && [ "$d" != "/" ]; do
    [ -f "$d/CONTEXT.md" ] && { echo "$d"; return; }
    d=$(dirname "$d")
  done
  echo "."
}

declare -A needs=()
while IFS= read -r f; do
  case "$f" in
    *.md|.gitignore|.idea/*) continue ;;   # docs and tooling noise never require a history entry
  esac
  needs["$(owner_of "$f")"]=1
done <<< "$changed"

status=0
for owner in "${!needs[@]}"; do
  hist="$owner/HISTORY.md"
  [ "$owner" = "." ] && hist="HISTORY.md"
  if ! grep -qxF "$hist" <<< "$changed"; then
    echo "context-sync: code under '$owner' changed but $hist was not updated (see AGENTS.md rule 1)" >&2
    status=1
  fi
done
exit $status
