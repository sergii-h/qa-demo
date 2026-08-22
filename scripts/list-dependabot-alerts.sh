#!/usr/bin/env bash
set -euo pipefail

REPO="${DEPENDABOT_REPO:-sergii-h/qa-demo}"
STATE="${DEPENDABOT_STATE:-open}"

if ! command -v gh >/dev/null 2>&1; then
  echo "gh CLI is required. Install: https://cli.github.com/" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "Run: gh auth login (token needs repo + security_events scopes)" >&2
  exit 1
fi

alerts=$(gh api "repos/${REPO}/dependabot/alerts?state=${STATE}" --paginate \
  --jq '[.[] | select(.security_advisory.severity == "moderate" or .security_advisory.severity == "high" or .security_advisory.severity == "critical")]')

count=$(echo "$alerts" | jq 'length')

echo "Repository: ${REPO}"
echo "Open moderate/high/critical Dependabot alerts: ${count}"
echo ""

if [ "$count" -eq 0 ]; then
  exit 0
fi

echo "$alerts" | jq -r '.[] | "#\(.number) | \(.security_advisory.severity) | \(.dependency.package.name) \(.dependency.package.ecosystem) | \(.dependency.manifest_path // "n/a") | patched: \(.security_vulnerability.first_patched_version.identifier // "none") | \((.security_advisory.identifiers // []) | map(select(.type == "GHSA") | .value) | first // "n/a") | \(.security_advisory.summary)"'
