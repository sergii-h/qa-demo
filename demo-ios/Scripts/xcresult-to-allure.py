#!/usr/bin/env python3
"""Convert an Xcode xcresult bundle into Allure 2 result JSON files."""

from __future__ import annotations

import json
import os
import re
import subprocess
import sys
import uuid
from pathlib import Path
from typing import Any


ALLURE_PREFIX_EPIC = "allure.epic:"
ALLURE_PREFIX_FEATURE = "allure.feature:"
ALLURE_PREFIX_TMS = "allure.tms:"
STATUS_MAP = {
    "Passed": "passed",
    "Failed": "failed",
    "Skipped": "skipped",
    "Expected Failure": "skipped",
    "unknown": "broken",
}


def run_xcresulttool(*args: str) -> dict[str, Any]:
    command = ["xcrun", "xcresulttool", "get", "test-results", *args, "--compact"]
    completed = subprocess.run(command, check=True, capture_output=True, text=True)
    return json.loads(completed.stdout)


def flatten_test_cases(nodes: list[dict[str, Any]], suite: str = "") -> list[dict[str, Any]]:
    cases: list[dict[str, Any]] = []
    for node in nodes:
        node_type = node.get("nodeType")
        name = node.get("name", "")
        next_suite = name if node_type == "Test Suite" else suite
        if node_type == "Test Case":
            cases.append(
                {
                    "id": node.get("nodeIdentifier") or node.get("nodeIdentifierURL") or name,
                    "name": name,
                    "suite": next_suite,
                    "result": node.get("result", "unknown"),
                    "duration": node.get("durationInSeconds", 0.0) or 0.0,
                    "details": node.get("details", ""),
                    "children": node.get("children", []),
                }
            )
        cases.extend(flatten_test_cases(node.get("children", []), next_suite))
    return cases


def failure_message(node: dict[str, Any]) -> str:
    messages: list[str] = []
    if node.get("nodeType") == "Failure Message":
        messages.append(node.get("name") or node.get("details") or "")
    for child in node.get("children", []):
        messages.append(failure_message(child))
    return "\n".join(part for part in messages if part)


def display_name(raw_name: str) -> str:
    name = re.sub(r"^test_?", "", raw_name)
    if name.startswith("Should"):
        return name[0].lower() + name[1:]
    return name


def collect_activities(xcresult: Path, test_id: str) -> list[dict[str, Any]]:
    try:
        payload = run_xcresulttool("activities", "--path", str(xcresult), "--test-id", test_id)
    except (subprocess.CalledProcessError, json.JSONDecodeError):
        return []
    runs = payload.get("testRuns")
    if isinstance(runs, dict):
        return runs.get("activities", [])
    if isinstance(runs, list) and runs:
        return runs[0].get("activities", [])
    return []


def parse_allure_metadata(activities: list[dict[str, Any]]) -> tuple[str, str, list[str], list[dict[str, Any]]]:
    epic = ""
    feature = ""
    tms: list[str] = []
    steps: list[dict[str, Any]] = []

    def walk(nodes: list[dict[str, Any]]) -> None:
        nonlocal epic, feature
        for node in nodes:
            title = node.get("title") or ""
            start = int((node.get("startTime") or 0) * 1000)
            if title.startswith(ALLURE_PREFIX_EPIC):
                epic = title[len(ALLURE_PREFIX_EPIC) :]
            elif title.startswith(ALLURE_PREFIX_FEATURE):
                feature = title[len(ALLURE_PREFIX_FEATURE) :]
            elif title.startswith(ALLURE_PREFIX_TMS):
                tms.append(title[len(ALLURE_PREFIX_TMS) :])
            elif title:
                child_steps: list[dict[str, Any]] = []
                nested = node.get("childActivities", [])
                if nested:
                    before = len(steps)
                    walk(nested)
                    child_steps = steps[before:]
                    del steps[before:]
                steps.append(
                    {
                        "name": title,
                        "status": "failed" if node.get("isAssociatedWithFailure") else "passed",
                        "stage": "finished",
                        "start": start,
                        "stop": start,
                        "steps": child_steps,
                    }
                )
                continue
            walk(node.get("childActivities", []))

    walk(activities)
    return epic, feature, tms, steps


def write_environment(results_dir: Path, summary: dict[str, Any], api_base_url: str) -> None:
    devices = summary.get("devices") or summary.get("environmentDevices") or []
    device = devices[0] if devices else {}
    lines = [
        "Framework=XCUITest",
        f"OS={device.get('platform', 'iOS')} {device.get('osVersion', '')}".rstrip(),
        f"Device={device.get('deviceName', device.get('modelName', 'iOS Simulator'))}",
        f"API.Base.URL={api_base_url}",
    ]
    (results_dir / "environment.properties").write_text("\n".join(lines) + "\n", encoding="utf-8")


def convert(xcresult: Path, results_dir: Path, api_base_url: str) -> None:
    results_dir.mkdir(parents=True, exist_ok=True)
    tests_payload = run_xcresulttool("tests", "--path", str(xcresult))
    try:
        summary = run_xcresulttool("summary", "--path", str(xcresult))
    except (subprocess.CalledProcessError, json.JSONDecodeError):
        summary = tests_payload

    for case in flatten_test_cases(tests_payload.get("testNodes", [])):
        activities = collect_activities(xcresult, case["id"])
        epic, feature, tms, steps = parse_allure_metadata(activities)
        status = STATUS_MAP.get(case["result"], "broken")
        duration_ms = int(case["duration"] * 1000)
        test_uuid = str(uuid.uuid4())
        name = display_name(case["name"])
        full_name = f"{case['suite']}.{name}" if case["suite"] else name
        labels = [
            {"name": "suite", "value": case["suite"] or "DemoUITests"},
            {"name": "framework", "value": "XCUITest"},
            {"name": "language", "value": "swift"},
            {"name": "package", "value": "DemoUITests"},
        ]
        if epic:
            labels.append({"name": "epic", "value": epic})
        if feature:
            labels.append({"name": "feature", "value": feature})
        links = [{"type": "tms", "name": item, "url": item} for item in tms]
        result: dict[str, Any] = {
            "uuid": test_uuid,
            "historyId": full_name,
            "fullName": full_name,
            "name": name,
            "status": status,
            "stage": "finished",
            "start": 0,
            "stop": duration_ms,
            "labels": labels,
            "links": links,
            "steps": steps,
        }
        details = case["details"] or failure_message({"children": case["children"]})
        if details and status == "failed":
            result["statusDetails"] = {"message": details}
        (results_dir / f"{test_uuid}-result.json").write_text(
            json.dumps(result, indent=2),
            encoding="utf-8",
        )

    write_environment(results_dir, summary, api_base_url)


def main() -> int:
    if len(sys.argv) < 3:
        print("Usage: xcresult-to-allure.py <xcresult> <allure-results-dir>", file=sys.stderr)
        return 2
    xcresult = Path(sys.argv[1])
    results_dir = Path(sys.argv[2])
    api_base_url = os.environ.get("API_BASE_URL", "http://localhost:8085/v1/")
    if not xcresult.exists():
        print(f"xcresult bundle not found: {xcresult}", file=sys.stderr)
        return 1
    convert(xcresult, results_dir, api_base_url)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
