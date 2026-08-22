#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
WORKTREE="${PACT_MASTER_WORKTREE:-/tmp/pact-ios-master-worktree}"

# shellcheck source=pact-participants.sh
source "${SCRIPT_DIR}/pact-participants.sh"

resolve_master_ref() {
  if git -C "${ROOT}" show-ref --verify --quiet "refs/remotes/origin/${PACT_MAIN_BRANCH}"; then
    echo "origin/${PACT_MAIN_BRANCH}"
  elif git -C "${ROOT}" show-ref --verify --quiet "refs/heads/${PACT_MAIN_BRANCH}"; then
    echo "${PACT_MAIN_BRANCH}"
  else
    git -C "${ROOT}" fetch origin "${PACT_MAIN_BRANCH}"
    echo "origin/${PACT_MAIN_BRANCH}"
  fi
}

MASTER_REF="$(resolve_master_ref)"
MASTER_SHA="$(git -C "${ROOT}" rev-parse "${MASTER_REF}")"
PACT_IOS_GENERATE_ONLY="${PACT_IOS_GENERATE_ONLY:-false}"
PACTS_TO_PUBLISH="${PACT_IOS_PACTS_DIR:-}"

git -C "${ROOT}" worktree add "${WORKTREE}" "${MASTER_REF}"

cleanup() {
  git -C "${ROOT}" worktree remove --force "${WORKTREE}" 2>/dev/null || true
}
trap cleanup EXIT

if [[ -z "${PACTS_TO_PUBLISH}" ]]; then
  (
    cd "${WORKTREE}/demo-ios"
    brew install xcodegen
    xcodegen generate
    mkdir -p pacts
    export PACT_OUTPUT_DIR="${WORKTREE}/demo-ios/pacts"
    # Resolve against this checkout so destination logic is available before it lands on master.
    # shellcheck source=../../demo-ios/Scripts/ios-destination.sh
    source "${ROOT}/demo-ios/Scripts/ios-destination.sh"
    resolve_xcodebuild_destination
    xcodebuild test \
      -project Demo.xcodeproj \
      -scheme Demo \
      -destination "$DESTINATION" \
      -only-testing:DemoPactTests \
      -parallel-testing-enabled NO \
      CODE_SIGNING_ALLOWED=NO
    bash Scripts/collect-pacts-from-simulator.sh pacts
  )
  PACTS_TO_PUBLISH="${WORKTREE}/demo-ios/pacts"
fi

if [[ "${PACT_IOS_GENERATE_ONLY}" == "true" ]]; then
  OUTPUT_DIR="${PACT_IOS_OUTPUT_DIR:?PACT_IOS_OUTPUT_DIR is required when PACT_IOS_GENERATE_ONLY=true}"
  mkdir -p "${OUTPUT_DIR}"
  cp -R "${PACTS_TO_PUBLISH}/." "${OUTPUT_DIR}/"
  echo "Generated ${PACT_MAIN_BRANCH} iOS pacts at ${OUTPUT_DIR} (${MASTER_SHA})"
  exit 0
fi

pact_publish "${PACTS_TO_PUBLISH}" \
  --consumer-app-version "${MASTER_SHA}" \
  --branch "${PACT_MAIN_BRANCH}"

(
  cd "${WORKTREE}/demo-service"
  PACT_BROKER_BASE_URL="${PACT_BROKER_BASE_URL}" \
  mvn -q verify -Pintegration-tests \
    -Dit.test="${PACT_TASK_API_PROVIDER_IT_TESTS}" \
    -Djacoco.skip=true \
    -Dpact.verifier.publishResults=true \
    -Dpact.provider.version="${MASTER_SHA}" \
    -Dpact.provider.branch="${PACT_MAIN_BRANCH}" \
    -Djunit.parallel.enabled=false
)

echo "Bootstrapped ${PACT_MAIN_BRANCH} iOS contracts at ${MASTER_SHA}"
