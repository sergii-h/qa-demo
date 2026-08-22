#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=ios-destination.sh
source "$ROOT/Scripts/ios-destination.sh"
XCODEGEN="${XCODEGEN:-/tmp/xcodegen-dist/xcodegen/bin/xcodegen}"
SCHEME="${SCHEME:-Demo}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT/build/DerivedData}"
COVERAGE_SUMMARY="${COVERAGE_SUMMARY:-$ROOT/build/coverage-summary.txt}"
MIN_LINE_COVERAGE="${MIN_LINE_COVERAGE:-90}"
APP_BUNDLE_ID="${APP_BUNDLE_ID:-com.example.demo}"

cd "$ROOT"

if [[ ! -f Demo.xcodeproj/project.pbxproj ]]; then
  "$XCODEGEN" generate
fi

mkdir -p "$(dirname "$COVERAGE_SUMMARY")"

# ViewHosting and StubURLProtocol are process-global; run each suite in its own
# xcodebuild process so hosts and HTTP stubs do not leak between suites.
TEST_SUITES=(
  DemoTests/AppConfigurationTests
  DemoTests/AppLocaleTests
  DemoTests/ErrorBodyParserTests
  DemoTests/TaskLabelsTests
  DemoTests/TaskListViewModelTests
  DemoTests/TaskFormViewModelTests
  DemoTests/TaskDetailViewModelTests
  DemoTests/TaskRepositoryTests
  DemoTests/ErrorMessagesTests
  DemoTests/URLSessionTaskAPITests
  DemoTests/CreateTaskIntegrationTests
  DemoTests/EditTaskIntegrationTests
  DemoTests/LanguageIntegrationTests
  DemoTests/PullToRefreshIntegrationTests
  DemoTests/TaskDetailIntegrationTests
  DemoTests/ExternalValidationIntegrationTests
  DemoTests/TaskListIntegrationTests
  DemoTests/DeleteTaskIntegrationTests
  DemoTests/DemoAppThemeScreenTests
  DemoTests/DemoNavigationScreenTests
  DemoTests/LanguageSwitcherScreenTests
  DemoTests/TaskChipsScreenTests
  DemoTests/TaskDetailScreenTests
  DemoTests/TaskFormScreenTests
  DemoTests/TaskListScreenTests
)

mkdir -p "$(dirname "$COVERAGE_SUMMARY")" "$DERIVED_DATA/Logs"

resolve_xcodebuild_destination

echo "Building once for testing..."
xcodebuild build-for-testing \
  -project Demo.xcodeproj \
  -scheme "$SCHEME" \
  -destination "$DESTINATION" \
  -derivedDataPath "$DERIVED_DATA" \
  -enableCodeCoverage YES \
  CODE_SIGNING_ALLOWED=NO

XCTESTRUN="$(find "$DERIVED_DATA/Build/Products" -maxdepth 1 -name '*.xctestrun' | head -1)"
if [[ -z "$XCTESTRUN" ]]; then
  echo "ERROR: no .xctestrun produced by build-for-testing" >&2
  exit 1
fi

# UserDefaults writes are not guaranteed to reach disk before a short-lived test
# process exits, so a stale app install can leak persisted state (e.g. selected
# language) into the next suite. Clearing the app's preferences before every
# suite guarantees each one starts from clean defaults regardless of that
# timing, without touching the app install itself.
#
# `defaults delete <bundle-id>` is a no-op here: under `simctl spawn` it resolves
# against the simulator's root preferences search path, not the app's own
# sandboxed container, so the file it targets is never the one the app actually
# reads. Passing the container's preferences plist as an explicit file path
# routes the deletion through the same CFPreferences API the app uses, so it
# takes effect for the next launch.
DEVICE_UDID="${SIMULATOR_UDID:?ERROR: simulator UDID was not resolved}"

clear_app_preferences() {
  local container
  container="$(xcrun simctl get_app_container "$DEVICE_UDID" "$APP_BUNDLE_ID" data 2>/dev/null)" || return 0
  local prefs_plist="$container/Library/Preferences/$APP_BUNDLE_ID"
  xcrun simctl spawn "$DEVICE_UDID" defaults delete "$prefs_plist" >/dev/null 2>&1 || true
}

run_suite() {
  local suite="$1"
  local log="$DERIVED_DATA/Logs/run-suite-${suite//\//-}.log"
  echo "Running ${suite}..."
  clear_app_preferences
  set +e
  xcodebuild test-without-building \
    -xctestrun "$XCTESTRUN" \
    -destination "$DESTINATION" \
    -derivedDataPath "$DERIVED_DATA" \
    -only-testing:"${suite}" \
    -parallel-testing-enabled NO \
    CODE_SIGNING_ALLOWED=NO 2>&1 | tee "$log"
  local test_exit="${PIPESTATUS[0]}"
  set -e

  if [[ "$test_exit" -ne 0 ]]; then
    echo "ERROR: ${suite} failed (xcodebuild exit ${test_exit})" >&2
    exit "$test_exit"
  fi

  if grep -qE 'Test run with 0 tests in [0-9]+ suite' "$log"; then
    echo "ERROR: ${suite} executed 0 tests (tests may be commented out)" >&2
    exit 1
  fi

  if grep -q 'Executed 0 tests, with 0 failures' "$log" && ! grep -q '✔ Test ' "$log"; then
    echo "ERROR: ${suite} executed 0 tests (tests may be commented out)" >&2
    exit 1
  fi
}

ACCUM_PROFDATA="$DERIVED_DATA/accumulated.profdata"
rm -f "$ACCUM_PROFDATA"

merge_coverage_profdata() {
  local new_profdata
  new_profdata="$(find "$DERIVED_DATA/Build/ProfileData" -name Coverage.profdata 2>/dev/null | head -1)"
  if [[ ! -f "$new_profdata" ]]; then
    return 0
  fi
  if [[ -f "$ACCUM_PROFDATA" ]]; then
    xcrun llvm-profdata merge -sparse "$new_profdata" "$ACCUM_PROFDATA" -o "$ACCUM_PROFDATA"
  else
    cp "$new_profdata" "$ACCUM_PROFDATA"
  fi
}

install_accumulated_profdata() {
  if [[ ! -f "$ACCUM_PROFDATA" ]]; then
    return 0
  fi
  local target_profdata
  target_profdata="$(find "$DERIVED_DATA/Build/ProfileData" -name Coverage.profdata 2>/dev/null | head -1)"
  if [[ -n "$target_profdata" ]]; then
    cp "$ACCUM_PROFDATA" "$target_profdata"
  fi
}

for suite in "${TEST_SUITES[@]}"; do
  run_suite "${suite}"
  merge_coverage_profdata
done

install_accumulated_profdata

run_slather() {
  local slather_cmd=()

  if [[ -f "$ROOT/Gemfile" ]] && command -v bundle >/dev/null 2>&1; then
    bundle config set --local path "$ROOT/vendor/bundle" >/dev/null 2>&1 || true
    bundle check >/dev/null 2>&1 || bundle install --quiet
    slather_cmd=(bundle exec slather)
  elif command -v slather >/dev/null 2>&1; then
    slather_cmd=(slather)
  else
    return 1
  fi

  mkdir -p "$ROOT/slather-report"
  "${slather_cmd[@]}" coverage --verbose --build-directory "$DERIVED_DATA" \
    | tee "$ROOT/slather-report/slather-stdout.txt"
}

write_coverage_summary_from_slather() {
  {
    echo "Coverage gate: Demo.app line coverage (minimum ${MIN_LINE_COVERAGE}%)"
    echo "Test bundles (DemoTests.xctest, DemoPactTests.xctest) are excluded — they are not product code."
    echo ""
    if [[ -f "$ROOT/slather-report/index.html" ]]; then
      grep -o 'Line Coverage: [0-9.]+%' "$ROOT/slather-report/index.html" | head -1 || true
    fi
    echo ""
    cat "$ROOT/slather-report/slather.txt" 2>/dev/null || true
  } | tee "$COVERAGE_SUMMARY"
}

compute_demo_app_line_coverage() {
  local source_file="$ROOT/slather-report/slather-stdout.txt"
  if [[ ! -f "$source_file" ]]; then
    return 1
  fi

  awk '/^Demo\/.*: [0-9]+ of [0-9]+ lines/ {
    for (i = 1; i <= NF; i++) {
      if ($i == "of" && $(i + 1) ~ /^[0-9]+$/ && $(i - 1) ~ /^[0-9]+$/) {
        covered += $(i - 1)
        total += $(i + 1)
      }
    }
  }
  END {
    if (total == 0) {
      exit 1
    }
    printf "%.2f", (covered * 100.0) / total
  }' "$source_file"
}

extract_demo_app_line_coverage() {
  local from_lines
  from_lines="$(compute_demo_app_line_coverage || true)"
  if [[ -n "$from_lines" ]]; then
    echo "$from_lines"
    return
  fi

  if [[ -f "$ROOT/slather-report/slather.txt" ]]; then
    local from_app
    from_app="$(awk '/Demo.app/ {
      for (i = 1; i <= NF; i++) {
        if ($i ~ /^[0-9]+(\.[0-9]+)?%$/) { print $i; exit }
      }
    }' "$ROOT/slather-report/slather.txt")"
    if [[ -n "$from_app" ]]; then
      echo "${from_app//%}"
      return
    fi
  fi

  if [[ -f "$ROOT/slather-report/slather-stdout.txt" ]]; then
    awk '/Test Coverage:/ { gsub(/%/, "", $3); print $3; exit }' "$ROOT/slather-report/slather-stdout.txt"
  fi
}

enforce_coverage_gate() {
  local line_coverage
  line_coverage="$(extract_demo_app_line_coverage)"
  line_coverage="${line_coverage//%}"

  if [[ -z "${line_coverage:-}" ]]; then
    echo "ERROR: Could not parse Demo.app line coverage from slather output" >&2
    exit 1
  fi

  echo "Demo.app line coverage: ${line_coverage}% (gate: ${MIN_LINE_COVERAGE}%)"

  if awk -v coverage="$line_coverage" -v min="$MIN_LINE_COVERAGE" \
    'BEGIN { exit (coverage + 0 >= min + 0) ? 0 : 1 }'; then
    echo "Coverage gate passed."
    return 0
  fi

  echo "" >&2
  echo "========================================" >&2
  echo "COVERAGE GATE FAILED" >&2
  echo "Line coverage ${line_coverage}% is below ${MIN_LINE_COVERAGE}% gate" >&2
  echo "========================================" >&2
  exit 1
}

if run_slather; then
  echo "Slather coverage report written to slather-report/"
  write_coverage_summary_from_slather
else
  echo "Slather not available (run: bundle install in demo-ios)"
  exit 1
fi

enforce_coverage_gate
