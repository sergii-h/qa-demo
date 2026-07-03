#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${1:?API base URL is required (e.g. http://localhost:8085/v1/)}"
NPM_SCRIPT="${2:?Maestro npm script is required (test:e2e, test:uat, or test:accessibility)}"
REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
IOS_DIR="$REPO_ROOT/demo-react-native/ios"
DERIVED_DATA="$IOS_DIR/build/maestro"

# shellcheck source=e2e/maestro/scripts/ios-sdk.sh
source "$REPO_ROOT/e2e/maestro/scripts/ios-sdk.sh"

bash "$REPO_ROOT/e2e/maestro/scripts/build-ios-app.sh" "$API_BASE_URL"

SCHEME="$(basename "$(resolve_ios_project "$IOS_DIR")" .xcodeproj)"
APP_PATH="$(resolve_ios_app_path "$DERIVED_DATA" "$SCHEME")"
if [[ -z "$APP_PATH" || ! -d "$APP_PATH" ]]; then
  echo "Simulator .app not found after build: ${DERIVED_DATA}/Build/Products/**/${SCHEME}.app" >&2
  exit 1
fi

bash "$REPO_ROOT/e2e/maestro/scripts/install-ios-app.sh" "$APP_PATH"

cd "$REPO_ROOT/e2e/maestro"
export MAESTRO_CLI_NO_ANALYTICS=true
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true
ALLURE_RESULTS_DIR="$REPO_ROOT/e2e/maestro/allure-results" npm run "$NPM_SCRIPT"
