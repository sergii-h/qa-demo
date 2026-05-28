#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
WORKTREE="${PACT_MASTER_WORKTREE:-/tmp/pact-master-worktree}"

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

git -C "${ROOT}" worktree add "${WORKTREE}" "${MASTER_REF}"

cleanup() {
  git -C "${ROOT}" worktree remove --force "${WORKTREE}" 2>/dev/null || true
}
trap cleanup EXIT

(
  cd "${WORKTREE}/notification-service"
  mvn -q test
)
pact_publish "${WORKTREE}/notification-service/target/pacts" \
  --consumer-app-version "${MASTER_SHA}" \
  --branch "${PACT_MAIN_BRANCH}"

(
  cd "${WORKTREE}/demo-interface"
  npm ci
  npm run test:pact
)
pact_publish "${WORKTREE}/demo-interface/pacts" \
  --consumer-app-version "${MASTER_SHA}" \
  --branch "${PACT_MAIN_BRANCH}"

(
  cd "${WORKTREE}/demo-service"
  PACT_BROKER_BASE_URL="${PACT_BROKER_BASE_URL}" \
  PACT_PROVIDER_VERSION="${MASTER_SHA}" \
  PACT_PROVIDER_BRANCH="${PACT_MAIN_BRANCH}" \
  mvn -q verify -Pintegration-tests -Dit.test="*PactProviderTest" -Djacoco.skip=true \
    -Dpact.verifier.publishResults=true \
    -Dpact.provider.version="${MASTER_SHA}" \
    -Dpact.provider.branch="${PACT_MAIN_BRANCH}" \
    -Djunit.parallel.enabled=false
)

echo "Bootstrapped ${PACT_MAIN_BRANCH} contracts at ${MASTER_SHA}"
