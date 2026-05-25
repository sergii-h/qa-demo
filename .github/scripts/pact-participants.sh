PACT_MAIN_BRANCH="${PACT_MAIN_BRANCH:-master}"
PACT_BROKER_BASE_URL="${PACT_BROKER_BASE_URL:-http://localhost:9292}"
PACT_CLI_IMAGE="pactfoundation/pact-cli:latest"

if [[ "$(uname -s)" == "Darwin" ]]; then
  PACT_BROKER_DOCKER_URL="${PACT_BROKER_DOCKER_URL:-http://host.docker.internal:9292}"
  PACT_USE_HOST_NETWORK=false
else
  PACT_BROKER_DOCKER_URL="${PACT_BROKER_DOCKER_URL:-${PACT_BROKER_BASE_URL}}"
  PACT_USE_HOST_NETWORK=true
fi

PACT_PARTICIPANTS=(
  "demo-interface"
  "notification-service"
  "demo-service-tasks-create"
  "demo-service-tasks-delete"
  "demo-service-tasks-get-all"
  "demo-service-tasks-get-by-id"
  "demo-service-tasks-get-is-valid"
  "demo-service-tasks-update"
  "demo-service-tasks-events"
)

pact_cli() {
  local docker_args=(run --rm)
  if [[ "${PACT_USE_HOST_NETWORK}" == "true" ]]; then
    docker_args+=(--network=host)
  fi

  docker "${docker_args[@]}" \
    -e PACT_BROKER_BASE_URL="${PACT_BROKER_DOCKER_URL}" \
    "${PACT_CLI_IMAGE}" "$@"
}

pact_publish() {
  local pacts_dir="$1"
  shift
  local docker_args=(run --rm -v "${pacts_dir}:/pacts")

  if [[ "${PACT_USE_HOST_NETWORK}" == "true" ]]; then
    docker_args+=(--network=host)
  fi

  docker "${docker_args[@]}" \
    -e PACT_BROKER_BASE_URL="${PACT_BROKER_DOCKER_URL}" \
    "${PACT_CLI_IMAGE}" \
    pact-broker publish /pacts \
    --broker-base-url "${PACT_BROKER_DOCKER_URL}" \
    "$@"
}
