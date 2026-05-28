#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# shellcheck source=pact-participants.sh
source "${SCRIPT_DIR}/pact-participants.sh"

CURRENT_BRANCH="$(git -C "${ROOT}" branch --show-current)"
PACT_VERSION="${PACT_VERSION:-$(git -C "${ROOT}" rev-parse HEAD)}"
PACT_BRANCH="${PACT_BRANCH:-${CURRENT_BRANCH}}"

wait_for_broker() {
  for i in {1..30}; do
    if curl -sf "${PACT_BROKER_BASE_URL}" >/dev/null; then
      return 0
    fi
    echo "Waiting for Pact Broker... (${i}/30)"
    sleep 2
  done
  echo "Pact Broker did not become ready at ${PACT_BROKER_BASE_URL}" >&2
  exit 1
}

echo "Starting Pact Broker..."
docker compose -f "${ROOT}/docker/docker-compose/run-pact-broker.yml" up -d
wait_for_broker

echo "Configuring pacticipants..."
bash "${SCRIPT_DIR}/pact-configure-broker.sh"

if [[ "${CURRENT_BRANCH}" != "${PACT_MAIN_BRANCH}" ]]; then
  echo "Bootstrapping ${PACT_MAIN_BRANCH} contracts..."
  bash "${SCRIPT_DIR}/pact-bootstrap-master.sh"
fi

echo "Running notification-service consumer pact tests..."
(
  cd "${ROOT}/notification-service"
  mvn test
)
pact_publish "${ROOT}/notification-service/target/pacts" \
  --consumer-app-version "${PACT_VERSION}" \
  --branch "${PACT_BRANCH}"

echo "Running demo-interface consumer pact tests..."
(
  cd "${ROOT}/demo-interface"
  npm ci
  npm run test:pact
)
pact_publish "${ROOT}/demo-interface/pacts" \
  --consumer-app-version "${PACT_VERSION}" \
  --branch "${PACT_BRANCH}"

echo "Running demo-service provider verification..."
(
  cd "${ROOT}/demo-service"
  PACT_BROKER_BASE_URL="${PACT_BROKER_BASE_URL}" \
  mvn verify -Pintegration-tests -Dit.test="*PactProviderTest" -Djacoco.skip=true \
    -Dpact.verifier.publishResults=true \
    -Dpact.provider.version="${PACT_VERSION}" \
    -Dpact.provider.branch="${PACT_BRANCH}" \
    -Djunit.jupiter.execution.parallel.enabled=false
)

if [[ "${CURRENT_BRANCH}" != "${PACT_MAIN_BRANCH}" ]]; then
  echo "Running can-i-merge check..."
  PACT_VERSION="${PACT_VERSION}" bash "${SCRIPT_DIR}/pact-can-i-merge.sh"
else
  echo "On ${PACT_MAIN_BRANCH}; skipping can-i-merge."
fi

echo "Pact pipeline completed successfully."
