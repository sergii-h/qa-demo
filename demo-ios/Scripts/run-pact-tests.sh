#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
XCODEGEN="${XCODEGEN:-/tmp/xcodegen-dist/xcodegen/bin/xcodegen}"
DESTINATION="${DESTINATION:-platform=iOS Simulator,name=iPhone 17,OS=latest}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT/build/DerivedData}"
RESULT_BUNDLE="${RESULT_BUNDLE:-$ROOT/build/PactTestResults.xcresult}"

cd "$ROOT"
mkdir -p pacts

if [[ ! -f Demo.xcodeproj/project.pbxproj ]]; then
  "$XCODEGEN" generate
fi

export PACT_OUTPUT_DIR="$ROOT/pacts"

rm -rf "$RESULT_BUNDLE"

xcodebuild test \
  -project Demo.xcodeproj \
  -scheme Demo \
  -destination "$DESTINATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -resultBundlePath "$RESULT_BUNDLE" \
  -only-testing:DemoPactTests \
  -parallel-testing-enabled NO \
  CODE_SIGNING_ALLOWED=NO
