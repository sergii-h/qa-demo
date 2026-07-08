#!/usr/bin/env bash
set -euo pipefail

SUITE="${1:?Usage: run-cypress-parallel.sh <e2e|accessibility|uat>}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

resolve_split() {
  case "$SUITE" in
    e2e) echo "${CYPRESS_SPLIT:-3}" ;;
    accessibility) echo "${CYPRESS_SPLIT:-2}" ;;
    uat) echo "1" ;;
    *) echo "1" ;;
  esac
}

SPLIT="$(resolve_split)"

if [[ "$SPLIT" -le 1 ]]; then
  exec env CYPRESS_SUITE="$SUITE" cypress run --browser chrome
fi

echo "Running Cypress suite '$SUITE' in parallel ($SPLIT workers)..."

rm -rf allure-results/.shard-* cypress/screenshots cypress/videos
failed=0
pids=()

for ((index = 0; index < SPLIT; index++)); do
  (
    export CYPRESS_SUITE="$SUITE"
    export SPLIT="$SPLIT"
    export SPLIT_INDEX="$index"
    export ALLURE_RESULTS_DIR="allure-results/.shard-$index"
    mkdir -p "$ALLURE_RESULTS_DIR"
    cypress run --browser chrome
  ) &
  pids+=("$!")
done

for pid in "${pids[@]}"; do
  wait "$pid" || failed=1
done

mkdir -p allure-results
for ((index = 0; index < SPLIT; index++)); do
  shard_dir="allure-results/.shard-$index"
  [[ -d "$shard_dir" ]] || continue
  shopt -s nullglob
  for file in "$shard_dir"/*; do
    cp -n "$file" allure-results/
  done
  rm -rf "$shard_dir"
done

exit "$failed"
