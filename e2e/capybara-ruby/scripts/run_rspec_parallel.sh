#!/usr/bin/env bash
set -euo pipefail

SUITE="${1:?Usage: run_rspec_parallel.sh <e2e|accessibility|uat>}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

resolve_local_split() {
  case "$SUITE" in
    e2e) echo "${CAPYBARA_SPLIT:-3}" ;;
    accessibility) echo "${CAPYBARA_SPLIT:-2}" ;;
    uat) echo "1" ;;
    *) echo "1" ;;
  esac
}

run_device() {
  local device="$1"
  local results_dir="allure-results/.device-$device"

  if [[ -n "${SPLIT_INDEX:-}" ]]; then
    results_dir="allure-results/.device-$device/.shard-${SPLIT_INDEX}"
  fi

  mkdir -p "$results_dir"
  spec_files=()
  while IFS= read -r spec_file; do
    [[ -n "$spec_file" ]] && spec_files+=("$spec_file")
  done < <(
    CAPYBARA_SUITE="$SUITE" \
    SPLIT="${SPLIT:-1}" \
    SPLIT_INDEX="${SPLIT_INDEX:-0}" \
    bundle exec ruby -r "$ROOT/support/spec_shard" -e '
      puts SpecShard.files_for_shard(
        suite: ENV.fetch("CAPYBARA_SUITE", "e2e"),
        split_count: ENV.fetch("SPLIT", "1").to_i,
        split_index: ENV.fetch("SPLIT_INDEX", "0").to_i
      ).join("\n")
    '
  )

  if [[ "${#spec_files[@]}" -eq 0 ]]; then
    echo "No spec files selected for suite '$SUITE'."
    return 0
  fi

  echo "=== Running $SUITE on $device ==="
  env CAPYBARA_SUITE="$SUITE" \
    CAPYBARA_DEVICE="$device" \
    SPLIT="${SPLIT:-1}" \
    SPLIT_INDEX="${SPLIT_INDEX:-0}" \
    ALLURE_RESULTS_DIR="$results_dir" \
    bundle exec rspec "${spec_files[@]}"
}

run_local_parallel_for_device() {
  local device="$1"
  local split="$2"
  local failed=0
  local pids=()

  for ((index = 0; index < split; index++)); do
    (
      export SPLIT="$split"
      export SPLIT_INDEX="$index"
      run_device "$device"
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

rm -rf allure-results tmp/billy-cache screenshots
failed=0

if [[ -n "${SPLIT:-}" && "${SPLIT}" -gt 1 && -n "${SPLIT_INDEX:-}" ]]; then
  for device in desktop mobile; do
    run_device "$device" || failed=1
  done
elif [[ "$(resolve_local_split)" -gt 1 ]]; then
  local_split="$(resolve_local_split)"
  for device in desktop mobile; do
    run_local_parallel_for_device "$device" "$local_split" || failed=1
  done
else
  for device in desktop mobile; do
    run_device "$device" || failed=1
  done
fi

ruby scripts/merge_allure_results.rb allure-results \
  allure-results/.device-desktop \
  allure-results/.device-mobile

exit "$failed"
