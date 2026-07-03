#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${1:?API base URL is required (e.g. http://localhost:8085/v1/)}"
REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
APP_DIR="$REPO_ROOT/demo-react-native"
IOS_DIR="$APP_DIR/ios"
DERIVED_DATA="$IOS_DIR/build/maestro"

# shellcheck source=e2e/maestro/scripts/ios-sdk.sh
source "$REPO_ROOT/e2e/maestro/scripts/ios-sdk.sh"

require_xcode

verify_local_networking_plist() {
  local plist="$1"
  if ! grep -q 'NSAllowsLocalNetworking' "$plist"; then
    cat >&2 <<EOF
Info.plist missing NSAllowsLocalNetworking after expo prebuild.

Expected ios.infoPlist.NSAppTransportSecurity in demo-react-native/app.config.ts.
EOF
    exit 1
  fi
  echo "Info.plist allows local HTTP (from app.config.ts)"
}

cd "$APP_DIR"
npm ci

export API_BASE_URL

echo "Running expo prebuild to sync native iOS project..."
npx expo prebuild --platform ios --no-install

echo "Installing CocoaPods dependencies (via Expo pod-install)..."
(cd "$APP_DIR" && npx pod-install)

patch_fmt_for_xcode26 "$IOS_DIR"

XCODE_TARGET="$(resolve_ios_xcode_target "$IOS_DIR")" || {
  echo "No Xcode workspace or project found under ${IOS_DIR}." >&2
  echo "CocoaPods may have failed — check pod install output above." >&2
  exit 1
}
XCODE_ARG_NAME="${XCODE_TARGET%%|*}"
XCODE_PATH="${XCODE_TARGET#*|}"

SCHEME="$(resolve_ios_app_scheme "$IOS_DIR" "$XCODE_ARG_NAME" "$XCODE_PATH")"
if [[ -z "$SCHEME" ]]; then
  echo "Could not resolve app scheme from ${XCODE_PATH}." >&2
  exit 1
fi

INFO_PLIST="$IOS_DIR/${SCHEME}/Info.plist"
if [[ ! -f "$INFO_PLIST" ]]; then
  INFO_PLIST="$(find "$IOS_DIR" -name Info.plist -print -quit)"
fi
if [[ -f "$INFO_PLIST" ]]; then
  verify_local_networking_plist "$INFO_PLIST"
fi

echo "Building Release simulator app: scheme=${SCHEME}"
echo "API_BASE_URL baked into bundle: $API_BASE_URL"

SIMULATOR_DESTINATION="$(resolve_ios_simulator_destination_for_build)"
echo "Xcode destination: ${SIMULATOR_DESTINATION}"

if ! xcodebuild \
  "-${XCODE_ARG_NAME}" "$XCODE_PATH" \
  -scheme "$SCHEME" \
  -configuration Release \
  -sdk iphonesimulator \
  -derivedDataPath "$DERIVED_DATA" \
  -destination "$SIMULATOR_DESTINATION" \
  ONLY_ACTIVE_ARCH=YES \
  CODE_SIGNING_ALLOWED=NO \
  COMPILER_INDEX_STORE_ENABLE=NO \
  build; then
  echo "xcodebuild failed (exit $?); scheme=${SCHEME} destination=${SIMULATOR_DESTINATION}" >&2
  xcodebuild -showdestinations "-${XCODE_ARG_NAME}" "$XCODE_PATH" -scheme "$SCHEME" >&2 || true
  exit 1
fi

APP_PATH="$(resolve_ios_app_path "$DERIVED_DATA" "$SCHEME")"
if [[ -z "$APP_PATH" || ! -d "$APP_PATH" ]]; then
  echo "Simulator .app not found: ${DERIVED_DATA}/Build/Products/**/${SCHEME}.app" >&2
  exit 1
fi

echo "$APP_PATH"
echo "Install on a booted iOS Simulator:" >&2
echo "  npm run install:ios" >&2
