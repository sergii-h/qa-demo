#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${1:?API base URL is required}"
REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
APP_DIR="$REPO_ROOT/demo-react-native"
ANDROID_APP="$APP_DIR/android/app"

# shellcheck source=e2e/maestro/scripts/android-sdk.sh
source "$REPO_ROOT/e2e/maestro/scripts/android-sdk.sh"

require_android_sdk

verify_cleartext_manifest() {
  local manifest="$1"
  if ! grep -q 'usesCleartextTraffic' "$manifest"; then
    cat >&2 <<EOF
AndroidManifest missing usesCleartextTraffic after expo prebuild.

Expected withCleartextTraffic config plugin in demo-react-native/app.config.ts.
EOF
    exit 1
  fi
  echo "AndroidManifest allows cleartext HTTP (from app.config.ts)"
}

cd "$APP_DIR"
npm ci

export API_BASE_URL

echo "Running expo prebuild to sync native project..."
npx expo prebuild --platform android --no-install

LOCAL_PROPERTIES="$APP_DIR/android/local.properties"
printf 'sdk.dir=%s\n' "$ANDROID_SDK" > "$LOCAL_PROPERTIES"

verify_cleartext_manifest "$ANDROID_APP/src/main/AndroidManifest.xml"

bash "$REPO_ROOT/e2e/maestro/scripts/patch-android-icon-font.sh" "$ANDROID_APP"

REACT_NATIVE_ARCHITECTURES="$(resolve_react_native_architectures)"
echo "Building release APK for CPU architecture: $REACT_NATIVE_ARCHITECTURES"
echo "API_BASE_URL baked into bundle: $API_BASE_URL"

cd android
./gradlew assembleRelease "-PreactNativeArchitectures=$REACT_NATIVE_ARCHITECTURES" --no-daemon

APK_PATH="$ANDROID_APP/build/outputs/apk/release/app-release.apk"
if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found after build: $APK_PATH" >&2
  exit 1
fi
echo "$APK_PATH"
echo "Install on a running emulator/device:" >&2
echo "  npm run install:android" >&2
