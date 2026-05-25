#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=pact-participants.sh
source "${SCRIPT_DIR}/pact-participants.sh"

for name in "${PACT_PARTICIPANTS[@]}"; do
  pact_cli pact-broker create-or-update-pacticipant \
    --name "${name}" \
    --main-branch "${PACT_MAIN_BRANCH}" \
    --broker-base-url "${PACT_BROKER_DOCKER_URL}"
done
