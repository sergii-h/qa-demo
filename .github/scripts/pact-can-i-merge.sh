#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=pact-participants.sh
source "${SCRIPT_DIR}/pact-participants.sh"

PACT_VERSION="${PACT_VERSION:?PACT_VERSION is required}"

case "${PACT_MERGE_PROFILE:-all}" in
  interface)
    MERGE_PARTICIPANTS=("${PACT_INTERFACE_MERGE_PARTICIPANTS[@]}")
    ;;
  notification)
    MERGE_PARTICIPANTS=("${PACT_NOTIFICATION_MERGE_PARTICIPANTS[@]}")
    ;;
  web)
    MERGE_PARTICIPANTS=("${PACT_WEB_MERGE_PARTICIPANTS[@]}")
    ;;
  android)
    MERGE_PARTICIPANTS=("${PACT_ANDROID_MERGE_PARTICIPANTS[@]}")
    ;;
  all)
    MERGE_PARTICIPANTS=("${PACT_PARTICIPANTS[@]}")
    ;;
  *)
    echo "Unknown PACT_MERGE_PROFILE: ${PACT_MERGE_PROFILE}" >&2
    exit 1
    ;;
esac

args=(
  pact-broker can-i-merge
  --broker-base-url "${PACT_BROKER_DOCKER_URL}"
)

for name in "${MERGE_PARTICIPANTS[@]}"; do
  args+=(--pacticipant "${name}" --version "${PACT_VERSION}")
done

pact_cli "${args[@]}"
