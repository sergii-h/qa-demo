#!/usr/bin/env bash

extract_simulator_udid() {
  sed -E 's/.*\(([0-9A-F]{8}-([0-9A-F]{4}-){3}[0-9A-F]{12})\).*/\1/'
}

iphonesimulator_sdk_version() {
  xcrun --sdk iphonesimulator --show-sdk-version
}

pick_iphone_on_runtime() {
  local os_prefix="$1"
  local name="$2"
  local needle="$name"

  if [[ "$name" != "iPhone" ]]; then
    needle="$name ("
  fi

  xcrun simctl list devices available \
    | awk -v os="$os_prefix" -v needle="$needle" '
        /^-- iOS / {
          in_ios = (index($3, os) == 1)
          next
        }
        /^-- / { in_ios = 0; next }
        in_ios && index($0, needle) { print; exit }
      '
}

resolve_ios_simulator_udid() {
  if [[ -n "${SIMULATOR_UDID:-}" ]]; then
    echo "$SIMULATOR_UDID"
    return 0
  fi

  xcrun simctl list >/dev/null 2>&1 || true

  local sdk
  sdk="$(iphonesimulator_sdk_version)"
  local sdk_major="${sdk%%.*}"

  local os_prefix name line udid
  local names=()
  if [[ -n "${SIMULATOR_NAME:-}" ]]; then
    names+=("$SIMULATOR_NAME")
  fi
  names+=("iPhone 16" "iPhone 17" "iPhone")

  for os_prefix in "$sdk" "${sdk_major}."; do
    for name in "${names[@]}"; do
      line="$(pick_iphone_on_runtime "$os_prefix" "$name")"
      if [[ -z "$line" ]]; then
        continue
      fi
      udid="$(printf '%s\n' "$line" | extract_simulator_udid)"
      if [[ -n "$udid" ]]; then
        echo "Using simulator ${line} (iOS SDK ${sdk})" >&2
        echo "$udid"
        return 0
      fi
    done
  done

  echo "No available iPhone simulator found for iOS SDK ${sdk}." >&2
  return 1
}

ensure_simulator_booted() {
  local udid="$1"

  xcrun simctl list >/dev/null 2>&1 || true

  if ! xcrun simctl list devices booted | grep -q "$udid"; then
    echo "Booting simulator ${udid}..." >&2
    xcrun simctl boot "$udid" >/dev/null 2>&1 || true
  fi

  xcrun simctl bootstatus "$udid" -b >&2
}

resolve_xcodebuild_destination() {
  local udid arch
  udid="$(resolve_ios_simulator_udid)" || return 1
  ensure_simulator_booted "$udid"
  arch="$(uname -m)"
  export SIMULATOR_UDID="$udid"
  export DESTINATION="platform=iOS Simulator,id=${udid},arch=${arch}"
  echo "Xcode destination: ${DESTINATION}" >&2
}
