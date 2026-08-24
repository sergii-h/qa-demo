#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=ios-destination.sh
source "$ROOT/Scripts/ios-destination.sh"
XCODEGEN="${XCODEGEN:-/tmp/xcodegen-dist/xcodegen/bin/xcodegen}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT/build/DerivedData}"
RESULT_BUNDLE="${RESULT_BUNDLE:-$ROOT/build/UITestResults.xcresult}"
ALLURE_RESULTS="${ALLURE_RESULTS:-$ROOT/build/allure-results}"
E2E_SUITE="${E2E_SUITE:-e2e}"
API_BASE_URL="${API_BASE_URL:-http://localhost:8085/v1/}"

cd "$ROOT"

if [[ ! -f Demo.xcodeproj/project.pbxproj ]]; then
  "$XCODEGEN" generate
fi

rm -rf "$RESULT_BUNDLE" "$ALLURE_RESULTS"
mkdir -p "$ALLURE_RESULTS"

resolve_xcodebuild_destination

uitest_classes() {
  local pattern="$1"
  find "$ROOT/DemoUITests" -name "$pattern" -print \
    | sed 's|.*/||; s|\.swift$||' \
    | sort
}

append_testing_filters() {
  local flag="$1"
  local pattern="$2"
  local classes
  classes="$(uitest_classes "$pattern")"
  if [[ -z "$classes" ]]; then
    if [[ "$flag" == "-only-testing" ]]; then
      echo "No UI test classes matching ${pattern} for E2E_SUITE=${E2E_SUITE}" >&2
      exit 1
    fi
    return 0
  fi
  while IFS= read -r class; do
    filter_args+=("${flag}:DemoUITests/${class}")
  done <<< "$classes"
}

filter_args=()
case "$E2E_SUITE" in
  uat)
    append_testing_filters -only-testing '*UatTest.swift'
    ;;
  accessibility)
    append_testing_filters -only-testing '*AccessibilityTest.swift'
    ;;
  e2e)
    filter_args+=(-only-testing:DemoUITests)
    append_testing_filters -skip-testing '*UatTest.swift'
    append_testing_filters -skip-testing '*AccessibilityTest.swift'
    ;;
  *)
    echo "Unknown E2E_SUITE=${E2E_SUITE} (expected e2e, uat, or accessibility)" >&2
    exit 1
    ;;
esac

TEST_RUNNER_E2E_SUITE="$E2E_SUITE" \
TEST_RUNNER_API_BASE_URL="$API_BASE_URL" \
TEST_RUNNER_WIREMOCK_URL="${WIREMOCK_URL:-http://localhost:8085}" \
xcodebuild test \
  -project Demo.xcodeproj \
  -scheme DemoUITests \
  -destination "$DESTINATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -resultBundlePath "$RESULT_BUNDLE" \
  "${filter_args[@]}" \
  -parallel-testing-enabled NO \
  API_BASE_URL="$API_BASE_URL" \
  CODE_SIGNING_ALLOWED=NO

API_BASE_URL="$API_BASE_URL" /usr/bin/python3 "$ROOT/Scripts/xcresult-to-allure.py" "$RESULT_BUNDLE" "$ALLURE_RESULTS"

if command -v allure >/dev/null 2>&1; then
  allure generate "$ALLURE_RESULTS" -o "$ROOT/build/allure-report" --clean
else
  echo "Allure CLI not installed; results kept at $ALLURE_RESULTS"
fi
