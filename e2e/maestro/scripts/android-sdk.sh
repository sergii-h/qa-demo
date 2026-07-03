#!/usr/bin/env bash

resolve_android_sdk() {
  if [[ -n "${ANDROID_HOME:-}" && -d "$ANDROID_HOME" ]]; then
    echo "$ANDROID_HOME"
    return 0
  fi
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "$ANDROID_SDK_ROOT" ]]; then
    echo "$ANDROID_SDK_ROOT"
    return 0
  fi
  if [[ -d "$HOME/Library/Android/sdk" ]]; then
    echo "$HOME/Library/Android/sdk"
    return 0
  fi
  if [[ -d "$HOME/Android/Sdk" ]]; then
    echo "$HOME/Android/Sdk"
    return 0
  fi
  return 1
}

require_android_sdk() {
  if ! ANDROID_SDK="$(resolve_android_sdk)"; then
    cat >&2 <<'EOF'
Android SDK not found.

Set ANDROID_HOME (or ANDROID_SDK_ROOT) to your SDK install path, for example:

  export ANDROID_HOME="$HOME/Library/Android/sdk"
  export PATH="$ANDROID_HOME/platform-tools:$PATH"

Then re-run the command.
EOF
    exit 1
  fi
  export ANDROID_HOME="$ANDROID_SDK"
  export ANDROID_SDK_ROOT="$ANDROID_SDK"
}

require_adb() {
  require_android_sdk
  ADB="$ANDROID_HOME/platform-tools/adb"
  if [[ ! -x "$ADB" ]]; then
    cat >&2 <<EOF
adb not found at $ADB

Install Android SDK platform-tools (Android Studio → SDK Manager → SDK Tools → Android SDK Platform-Tools).
EOF
    exit 1
  fi
}

resolve_react_native_architectures() {
  if [[ -n "${REACT_NATIVE_ARCHITECTURES:-}" ]]; then
    echo "$REACT_NATIVE_ARCHITECTURES"
    return 0
  fi

  require_adb
  local device_abi
  device_abi="$("$ADB" shell getprop ro.product.cpu.abi 2>/dev/null | tr -d '\r' || true)"
  if [[ -n "$device_abi" ]]; then
    echo "$device_abi"
    return 0
  fi

  case "$(uname -m)" in
    arm64 | aarch64) echo "arm64-v8a" ;;
    x86_64) echo "x86_64" ;;
    *)
      echo "arm64-v8a"
      ;;
  esac
}
