#!/usr/bin/env bash
set -euo pipefail

TARGET_DIR="${1:?usage: collect-pacts-from-simulator.sh <target-dir>}"
mkdir -p "${TARGET_DIR}"

shopt -s nullglob
for file in /var/folders/*/*/T/pacts/*.json \
  "${HOME}/Library/Developer/CoreSimulator/Devices"/*/data/Containers/Data/Application/*/tmp/pacts/*.json \
  "${HOME}/Library/Developer/CoreSimulator/Devices"/*/data/Containers/Data/Application/*/Library/Caches/pacts/*.json; do
  cp "${file}" "${TARGET_DIR}/"
done

if [[ -z "$(ls -A "${TARGET_DIR}" 2>/dev/null || true)" ]]; then
  echo "No pact files found in simulator temp directories" >&2
  exit 1
fi

echo "Collected pact files into ${TARGET_DIR}"
