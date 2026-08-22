#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO="${DEPENDABOT_REPO:-${GITHUB_REPOSITORY:-sergii-h/qa-demo}}"

if ! command -v gh >/dev/null 2>&1; then
  echo "gh CLI is required. Install: https://cli.github.com/" >&2
  exit 1
fi

if ! command -v node >/dev/null 2>&1 && ! command -v asdf >/dev/null 2>&1; then
  echo "node is required to compare Dependabot alerts against this checkout." >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required." >&2
  exit 1
fi

if [[ -z "${GH_TOKEN:-${GITHUB_TOKEN:-}}" ]] && ! gh auth status >/dev/null 2>&1; then
  echo "Run: gh auth login (token needs repo + security_events scopes)" >&2
  exit 1
fi

alerts=$(gh api "repos/${REPO}/dependabot/alerts?state=open" --paginate \
  --jq '[.[] | select(.security_advisory.severity == "moderate" or .security_advisory.severity == "high" or .security_advisory.severity == "critical")]')

count=$(echo "$alerts" | jq 'length')

echo "Repository: ${REPO}"
echo "Open moderate/high/critical Dependabot alerts on GitHub: ${count}"
echo ""

if [ "$count" -eq 0 ]; then
  echo "No open moderate/high/critical Dependabot alerts."
  exit 0
fi

echo "GitHub alerts (may stay open until merge + rescan):"
echo "$alerts" | jq -r '.[] | "#\(.number) | \(.security_advisory.severity) | \(.dependency.package.name) \(.dependency.package.ecosystem) | \(.dependency.manifest_path // "n/a") | patched: \(.security_vulnerability.first_patched_version.identifier // "none") | \((.security_advisory.identifiers // []) | map(select(.type == "GHSA") | .value) | first // "n/a") | \(.security_advisory.summary)"'
echo ""
echo "Checking this checkout:"

node_cwd="$ROOT"
for tv in "$ROOT/demo-interface/.tool-versions" "$ROOT/e2e/cypress-javascript/.tool-versions"; do
  if [ -f "$tv" ]; then
    node_cwd="$(dirname "$tv")"
    break
  fi
done

unfixed=$(echo "$alerts" | (cd "$node_cwd" && node "$ROOT/scripts/unfixed-dependabot-alerts.mjs" "$ROOT"))
unfixed_count=$(echo "$unfixed" | jq 'length')

if [ "$unfixed_count" -eq 0 ]; then
  echo "All ${count} GitHub alert(s) are patched in this checkout. Dependabot will close them after merge and a rescan."
  exit 0
fi

echo ""
echo "Still vulnerable in this checkout (${unfixed_count}):"
echo "$unfixed" | jq -r '.[] | "#\(.number) | \(.security_advisory.severity) | \(.dependency.package.name) \(.dependency.package.ecosystem) | \(.dependency.manifest_path // "n/a") | patched: \(.security_vulnerability.first_patched_version.identifier // "none") | \((.security_advisory.identifiers // []) | map(select(.type == "GHSA") | .value) | first // "n/a") | \(.security_advisory.summary)"'

echo "::error::Found ${unfixed_count} moderate+ Dependabot alert(s) still present in this checkout. See Security → Dependabot or run scripts/list-dependabot-alerts.sh locally."
exit 1
