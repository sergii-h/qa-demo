#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
# shellcheck source=e2e/maestro/scripts/android-sdk.sh
source "$REPO_ROOT/e2e/maestro/scripts/android-sdk.sh"

APK_PATH="${1:-$REPO_ROOT/demo-react-native/android/app/build/outputs/apk/release/app-release.apk}"
APP_ID="${MAESTRO_APP_ID:-com.example.demo}"

require_adb

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found: $APK_PATH" >&2
  echo "Build it first:" >&2
  echo "  npm run build:android:wiremock" >&2
  exit 1
fi

if "$ADB" shell pm path "$APP_ID" >/dev/null 2>&1; then
  echo "Removing existing $APP_ID install (signature or build may differ)..."
  "$ADB" shell am force-stop "$APP_ID" >/dev/null 2>&1 || true
  "$ADB" shell pm clear "$APP_ID" >/dev/null 2>&1 || true
  "$ADB" uninstall "$APP_ID" >/dev/null
fi

"$ADB" install "$APK_PATH"
