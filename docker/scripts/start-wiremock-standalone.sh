#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
WIREMOCK_VERSION="${WIREMOCK_VERSION:-3.9.2}"
WIREMOCK_PORT="${WIREMOCK_PORT:-8085}"
WIREMOCK_ROOT="${REPO_ROOT}/docker/docker-compose"
JAR="${TMPDIR:-/tmp}/wiremock-standalone-${WIREMOCK_VERSION}.jar"
PID_FILE="${TMPDIR:-/tmp}/qa-demo-wiremock-standalone.pid"
LOG_FILE="${TMPDIR:-/tmp}/qa-demo-wiremock-standalone.log"

if ! command -v java >/dev/null 2>&1; then
  echo "Java is required to run WireMock standalone." >&2
  exit 1
fi

if [[ ! -d "${WIREMOCK_ROOT}/mappings" ]]; then
  echo "WireMock mappings not found under ${WIREMOCK_ROOT}/mappings" >&2
  exit 1
fi

if [[ ! -f "$JAR" ]]; then
  echo "Downloading WireMock standalone ${WIREMOCK_VERSION}..."
  curl -fsSL \
    "https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/${WIREMOCK_VERSION}/wiremock-standalone-${WIREMOCK_VERSION}.jar" \
    -o "$JAR"
fi

if [[ -f "$PID_FILE" ]]; then
  existing_pid="$(cat "$PID_FILE")"
  if kill -0 "$existing_pid" 2>/dev/null; then
    echo "WireMock standalone already running (pid ${existing_pid})"
    exit 0
  fi
fi

echo "Starting WireMock standalone on port ${WIREMOCK_PORT}..."
nohup java -jar "$JAR" --port "$WIREMOCK_PORT" --root-dir "$WIREMOCK_ROOT" >"$LOG_FILE" 2>&1 &
echo $! >"$PID_FILE"

for i in {1..30}; do
  if curl -sf "http://localhost:${WIREMOCK_PORT}/__admin/mappings" >/dev/null; then
    echo "WireMock standalone is ready on http://localhost:${WIREMOCK_PORT}"
    exit 0
  fi
  echo "Waiting for WireMock standalone... ($i/30)"
  sleep 2
done

echo "WireMock standalone did not become ready. Log:" >&2
tail -50 "$LOG_FILE" >&2 || true
exit 1
