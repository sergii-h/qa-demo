#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=ios-destination.sh
source "$ROOT/Scripts/ios-destination.sh"
XCODEGEN="${XCODEGEN:-/tmp/xcodegen-dist/xcodegen/bin/xcodegen}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT/build/DerivedData}"
RESULT_BUNDLE="${RESULT_BUNDLE:-$ROOT/build/PactTestResults.xcresult}"

cd "$ROOT"
mkdir -p pacts

if [[ ! -f Demo.xcodeproj/project.pbxproj ]]; then
  "$XCODEGEN" generate
fi

export PACT_OUTPUT_DIR="$ROOT/pacts"

rm -rf "$RESULT_BUNDLE"

resolve_xcodebuild_destination

xcodebuild test \
  -project Demo.xcodeproj \
  -scheme Demo \
  -destination "$DESTINATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -resultBundlePath "$RESULT_BUNDLE" \
  -only-testing:DemoPactTests \
  -parallel-testing-enabled NO \
  CODE_SIGNING_ALLOWED=NO
