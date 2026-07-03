#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
# shellcheck source=e2e/maestro/scripts/ios-sdk.sh
source "$REPO_ROOT/e2e/maestro/scripts/ios-sdk.sh"

IOS_DIR="$REPO_ROOT/demo-react-native/ios"
IOS_SCHEME="$(basename "$(resolve_ios_project "$IOS_DIR")" .xcodeproj)"
DEFAULT_APP_PATH="$(
  resolve_ios_app_path "$IOS_DIR/build/maestro" "$IOS_SCHEME" 2>/dev/null || true
)"
APP_PATH="${1:-$DEFAULT_APP_PATH}"
APP_ID="${MAESTRO_APP_ID:-com.example.demo}"

require_xcode

if [[ -z "$APP_PATH" || ! -d "$APP_PATH" ]]; then
  echo "Simulator .app not found: ${APP_PATH:-<default path empty>}" >&2
  echo "Build it first:" >&2
  echo "  npm run build:ios:wiremock" >&2
  exit 1
fi

if ! is_valid_ios_app_bundle "$APP_PATH"; then
  cat >&2 <<EOF
Simulator .app is incomplete (missing Info.plist or bundle ID): ${APP_PATH}

This usually means the Xcode build failed or was interrupted. Rebuild:

  npm run build:ios:wiremock

If the build fails on fmt/consteval errors, you need Xcode 26 compatibility — rerun build:ios:* so pod-install applies the fmt patch.
EOF
  exit 1
fi

SIMULATOR_UDID="$(resolve_ios_simulator_udid)" || {
  echo "Could not resolve an iOS Simulator." >&2
  exit 1
}
ensure_simulator_booted "$SIMULATOR_UDID"

if xcrun simctl get_app_container "$SIMULATOR_UDID" "$APP_ID" data >/dev/null 2>&1; then
  echo "Removing existing $APP_ID install (signature or build may differ)..."
  xcrun simctl terminate "$SIMULATOR_UDID" "$APP_ID" >/dev/null 2>&1 || true
  xcrun simctl uninstall "$SIMULATOR_UDID" "$APP_ID" >/dev/null
fi

xcrun simctl install "$SIMULATOR_UDID" "$APP_PATH"
echo "Installed $APP_ID on simulator $SIMULATOR_UDID"
