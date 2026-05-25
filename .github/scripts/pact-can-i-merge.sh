#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=pact-participants.sh
source "${SCRIPT_DIR}/pact-participants.sh"

PACT_VERSION="${PACT_VERSION:?PACT_VERSION is required}"

args=(
  pact-broker can-i-merge
  --broker-base-url "${PACT_BROKER_DOCKER_URL}"
)

for name in "${PACT_PARTICIPANTS[@]}"; do
  args+=(--pacticipant "${name}" --version "${PACT_VERSION}")
done

pact_cli "${args[@]}"
