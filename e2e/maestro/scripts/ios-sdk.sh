#!/usr/bin/env bash

require_xcode() {
  if ! command -v xcodebuild >/dev/null 2>&1; then
    cat >&2 <<'EOF'
Xcode command-line tools not found.

Install Xcode from the App Store, then run:

  xcode-select --install
EOF
    exit 1
  fi

  if ! command -v xcrun >/dev/null 2>&1; then
    echo "xcrun not found; install Xcode command-line tools." >&2
    exit 1
  fi
}

require_simulator() {
  if ! xcrun simctl list devices booted 2>/dev/null | grep -q Booted; then
    cat >&2 <<'EOF'
No booted iOS Simulator found.

Start one with:

  open -a Simulator

Or boot a specific device:

  xcrun simctl boot "iPhone 16"
EOF
    exit 1
  fi
}

resolve_ios_workspace() {
  local ios_dir="$1"
  find "$ios_dir" -maxdepth 1 -name '*.xcworkspace' -print -quit
}

resolve_ios_project() {
  local ios_dir="$1"
  find "$ios_dir" -maxdepth 1 -name '*.xcodeproj' -print -quit
}

resolve_ios_app_scheme() {
  local ios_dir="$1"
  local xcode_arg_name="$2"
  local xcode_path="$3"

  local project
  project="$(resolve_ios_project "$ios_dir")"
  if [[ -z "$project" ]]; then
    return 1
  fi

  local app_scheme
  app_scheme="$(basename "$project" .xcodeproj)"

  if xcodebuild -list "-${xcode_arg_name}" "$xcode_path" 2>/dev/null \
    | awk -v scheme="$app_scheme" '/^ *Schemes:$/{flag=1; next} flag && $1==scheme {found=1} END{exit !found}'; then
    echo "$app_scheme"
    return 0
  fi

  echo "App scheme '${app_scheme}' not found in ${xcode_path}." >&2
  return 1
}

is_valid_ios_app_bundle() {
  local app_path="$1"
  local info_plist="$app_path/Info.plist"
  [[ -f "$info_plist" ]] || return 1
  /usr/libexec/PlistBuddy -c "Print CFBundleIdentifier" "$info_plist" >/dev/null 2>&1
}

resolve_ios_app_path() {
  local derived_data="$1"
  local scheme="$2"
  local candidate

  while IFS= read -r candidate; do
    if is_valid_ios_app_bundle "$candidate"; then
      echo "$candidate"
      return 0
    fi
  done < <(find "$derived_data/Build/Products" -type d -name "${scheme}.app" 2>/dev/null)

  return 1
}

patch_fmt_for_xcode26() {
  local ios_dir="$1"
  local fmt_base="$ios_dir/Pods/fmt/include/fmt/base.h"

  if [[ ! -f "$fmt_base" ]]; then
    return 0
  fi

  if grep -q 'Xcode 26 workaround' "$fmt_base"; then
    return 0
  fi

  perl -0777 -i -pe \
    's/(#elif defined\(__cpp_consteval\)\n#  define FMT_USE_CONSTEVAL) 1/$1 0  \/\/ Xcode 26 workaround/s' \
    "$fmt_base"
  echo "Patched fmt for Xcode 26 (disabled FMT_USE_CONSTEVAL)"
}

resolve_ios_xcode_target() {
  local ios_dir="$1"
  local workspace
  workspace="$(resolve_ios_workspace "$ios_dir")"
  if [[ -n "$workspace" ]]; then
    echo "workspace|$workspace"
    return 0
  fi

  local project
  project="$(resolve_ios_project "$ios_dir")"
  if [[ -n "$project" ]]; then
    echo "project|$project"
    return 0
  fi

  return 1
}

resolve_booted_simulator_udid() {
  xcrun simctl list devices booted \
    | awk -F '[()]' '/Booted/ { print $2; exit }'
}

extract_simulator_udid() {
  sed -E 's/.*\(([0-9A-F-]{36})\).*/\1/'
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

resolve_simulator_udid_by_name() {
  local name="$1"
  local sdk os_prefix line udid
  sdk="$(iphonesimulator_sdk_version)"
  local sdk_major="${sdk%%.*}"

  for os_prefix in "$sdk" "${sdk_major}."; do
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

  udid="$(
    xcrun simctl list devices available \
      | grep -F "$name (" \
      | head -1 \
      | extract_simulator_udid
  )" || true
  printf '%s\n' "$udid"
}

resolve_default_iphone_simulator_udid() {
  local sdk os_prefix name line udid
  sdk="$(iphonesimulator_sdk_version)"
  local sdk_major="${sdk%%.*}"

  for os_prefix in "$sdk" "${sdk_major}."; do
    for name in "iPhone 16" "iPhone 16 Pro" "iPhone 17" "iPhone"; do
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

  udid="$(
    xcrun simctl list devices available \
      | grep -E '^\s+iPhone' \
      | head -1 \
      | extract_simulator_udid
  )" || true
  printf '%s\n' "$udid"
}

resolve_ios_simulator_udid() {
  if [[ -n "${MAESTRO_DEVICE:-}" ]]; then
    echo "$MAESTRO_DEVICE"
    return 0
  fi

  # GitHub runners often have a random iOS 26 simulator already booted.
  # Prefer a device that matches the selected Xcode's iOS SDK instead.
  if [[ -z "${GITHUB_ACTIONS:-}" ]]; then
    local booted_udid
    booted_udid="$(resolve_booted_simulator_udid 2>/dev/null || true)"
    if [[ -n "$booted_udid" ]]; then
      echo "$booted_udid"
      return 0
    fi
  fi

  xcrun simctl list >/dev/null 2>&1 || true

  local simulator_name="${MAESTRO_IOS_SIMULATOR:-}"
  if [[ -z "$simulator_name" && -n "${GITHUB_ACTIONS:-}" ]]; then
    simulator_name="iPhone 16"
  fi

  if [[ -n "$simulator_name" ]]; then
    local named_udid
    named_udid="$(resolve_simulator_udid_by_name "$simulator_name")"
    if [[ -n "$named_udid" ]]; then
      echo "$named_udid"
      return 0
    fi
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
      local fallback_udid
      fallback_udid="$(resolve_default_iphone_simulator_udid)"
      if [[ -n "$fallback_udid" ]]; then
        echo "Preferred simulator '${simulator_name}' not found; using ${fallback_udid}." >&2
        echo "$fallback_udid"
        return 0
      fi
    fi
    echo "Simulator not found: ${simulator_name}" >&2
    return 1
  fi

  local default_udid
  default_udid="$(resolve_default_iphone_simulator_udid)"
  if [[ -n "$default_udid" ]]; then
    echo "$default_udid"
    return 0
  fi

  echo "No available iPhone simulators found." >&2
  return 1
}

ensure_simulator_booted() {
  local udid="$1"
  local booted

  xcrun simctl list >/dev/null 2>&1 || true

  booted="$(xcrun simctl list devices booted 2>/dev/null || true)"
  if printf '%s\n' "$booted" | grep -q Booted && ! printf '%s\n' "$booted" | grep -q "$udid"; then
    xcrun simctl shutdown all >/dev/null 2>&1 || true
  fi

  if ! xcrun simctl list devices booted | grep -q "$udid"; then
    echo "Booting simulator ${udid}..." >&2
    xcrun simctl boot "$udid" >/dev/null 2>&1 || true
  fi

  open -a Simulator --args -CurrentDeviceUDID "$udid" >/dev/null 2>&1 || true
  xcrun simctl bootstatus "$udid" -b >&2
}

resolve_ios_simulator_destination_for_build() {
  local udid dest
  udid="$(resolve_ios_simulator_udid)" || return 1
  ensure_simulator_booted "$udid" >&2
  export MAESTRO_DEVICE="$udid"
  dest="platform=iOS Simulator,id=${udid},arch=$(uname -m)"
  echo "Using simulator ${udid} for Xcode build." >&2
  echo "$dest"
}

resolve_ios_simulator_destination() {
  local udid
  udid="$(resolve_ios_simulator_udid)" || return 1
  ensure_simulator_booted "$udid" >&2
  echo "platform=iOS Simulator,id=${udid},arch=$(uname -m)"
}

resolve_simulator_udid() {
  resolve_ios_simulator_udid
}
