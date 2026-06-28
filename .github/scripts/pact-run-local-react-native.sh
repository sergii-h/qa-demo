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
  echo "Bootstrapping ${PACT_MAIN_BRANCH} React Native contracts..."
  bash "${SCRIPT_DIR}/pact-bootstrap-react-native-master.sh"
fi

echo "Running demo-react-native consumer pact tests..."
(
  cd "${ROOT}/demo-react-native"
  npm ci
  npm run test:pact
)
pact_publish "${ROOT}/demo-react-native/pacts" \
  --consumer-app-version "${PACT_VERSION}" \
  --branch "${PACT_BRANCH}"

echo "Running demo-service provider verification..."
(
  cd "${ROOT}/demo-service"
  PACT_BROKER_BASE_URL="${PACT_BROKER_BASE_URL}" \
  mvn verify -Pintegration-tests -Dit.test="${PACT_TASK_API_PROVIDER_IT_TESTS}" -Djacoco.skip=true \
    -Dpact.verifier.publishResults=true \
    -Dpact.provider.version="${PACT_VERSION}" \
    -Dpact.provider.branch="${PACT_BRANCH}" \
    -Djunit.parallel.enabled=false
)

if [[ "${CURRENT_BRANCH}" != "${PACT_MAIN_BRANCH}" ]]; then
  echo "Running can-i-merge check..."
  PACT_VERSION="${PACT_VERSION}" PACT_MERGE_PROFILE=react-native bash "${SCRIPT_DIR}/pact-can-i-merge.sh"
else
  echo "On ${PACT_MAIN_BRANCH}; skipping can-i-merge."
fi

echo "React Native Pact pipeline completed successfully."
