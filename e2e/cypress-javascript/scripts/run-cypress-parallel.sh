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

run_suite_for_device() {
  local device="$1"
  local split="$2"

  if [[ "$split" -le 1 ]]; then
    env CYPRESS_SUITE="$SUITE" CYPRESS_DEVICE="$device" \
      ALLURE_RESULTS_DIR="allure-results/.device-$device" \
      cypress run --browser chrome
    return
  fi

  echo "Running Cypress suite '$SUITE' on '$device' in parallel ($split workers)..."

  local failed=0
  local pids=()

  for ((index = 0; index < split; index++)); do
    (
      export CYPRESS_SUITE="$SUITE"
      export CYPRESS_DEVICE="$device"
      export SPLIT="$split"
      export SPLIT_INDEX="$index"
      export ALLURE_RESULTS_DIR="allure-results/.device-$device/.shard-$index"
      mkdir -p "$ALLURE_RESULTS_DIR"
      cypress run --browser chrome
    ) &
    pids+=("$!")
  done

  for pid in "${pids[@]}"; do
    wait "$pid" || failed=1
  done

  local merged_dir="allure-results/.device-$device"
  mkdir -p "$merged_dir"
  for ((index = 0; index < split; index++)); do
    local shard_dir="allure-results/.device-$device/.shard-$index"
    [[ -d "$shard_dir" ]] || continue
    shopt -s nullglob
    for file in "$shard_dir"/*; do
      cp -n "$file" "$merged_dir/"
    done
    rm -rf "$shard_dir"
  done

  return "$failed"
}

SPLIT="$(resolve_split)"
rm -rf allure-results cypress/screenshots cypress/videos
failed=0

for device in desktop mobile; do
  run_suite_for_device "$device" "$SPLIT" || failed=1
done

node scripts/merge-allure-results.js allure-results \
  allure-results/.device-desktop \
  allure-results/.device-mobile

exit "$failed"
