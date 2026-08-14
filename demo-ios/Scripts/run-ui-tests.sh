#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
XCODEGEN="${XCODEGEN:-/tmp/xcodegen-dist/xcodegen/bin/xcodegen}"
DESTINATION="${DESTINATION:-platform=iOS Simulator,name=iPhone 17,OS=latest}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT/build/DerivedData}"
RESULT_BUNDLE="${RESULT_BUNDLE:-$ROOT/build/UITestResults.xcresult}"
ALLURE_RESULTS="${ALLURE_RESULTS:-$ROOT/build/allure-results}"

cd "$ROOT"

if [[ ! -f Demo.xcodeproj/project.pbxproj ]]; then
  "$XCODEGEN" generate
fi

rm -rf "$RESULT_BUNDLE" "$ALLURE_RESULTS"
mkdir -p "$ALLURE_RESULTS"

xcodebuild test \
  -project Demo.xcodeproj \
  -scheme DemoUITests \
  -destination "$DESTINATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -resultBundlePath "$RESULT_BUNDLE" \
  -only-testing:DemoUITests \
  -parallel-testing-enabled NO \
  API_BASE_URL="${API_BASE_URL:-http://localhost:8085/v1/}" \
  CODE_SIGNING_ALLOWED=NO

if command -v allure >/dev/null 2>&1; then
  allure generate "$RESULT_BUNDLE" -o "$ROOT/build/allure-report" --clean
else
  echo "Allure CLI not installed; xcresult bundle kept at $RESULT_BUNDLE"
fi
