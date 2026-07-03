#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${1:?API base URL is required (e.g. http://10.0.2.2:8080/v1/)}"
NPM_SCRIPT="${2:?Maestro npm script is required (test:e2e, test:uat, or test:accessibility)}"
REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
APK_PATH="$REPO_ROOT/demo-react-native/android/app/build/outputs/apk/release/app-release.apk"

export REACT_NATIVE_ARCHITECTURES="${REACT_NATIVE_ARCHITECTURES:-x86_64}"

bash "$REPO_ROOT/e2e/maestro/scripts/build-android-app.sh" "$API_BASE_URL"

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found after build: $APK_PATH" >&2
  exit 1
fi

adb install -r "$APK_PATH"

cd "$REPO_ROOT/e2e/maestro"
export MAESTRO_CLI_NO_ANALYTICS=true
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true
ALLURE_RESULTS_DIR="$REPO_ROOT/e2e/maestro/allure-results" npm run "$NPM_SCRIPT"
