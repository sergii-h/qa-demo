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


def to_millis(value: Any) -> int:
    if not isinstance(value, (int, float)) or value <= 0:
        return 0
    if value > 10_000_000_000:
        return int(value)
    return int(value * 1000)


def step_times(node: dict[str, Any], child_steps: list[dict[str, Any]]) -> tuple[int, int]:
    start = to_millis(node.get("startTime"))
    stop = to_millis(node.get("finishTime") or node.get("endTime"))
    duration_seconds = node.get("durationInSeconds")
    if isinstance(duration_seconds, (int, float)) and duration_seconds > 0 and start:
        stop = max(stop, start + int(duration_seconds * 1000))
    if child_steps:
        child_starts = [child.get("start") or 0 for child in child_steps]
        child_stops = [child.get("stop") or 0 for child in child_steps]
        if not start:
            start = min((item for item in child_starts if item), default=0)
        stop = max(stop, max(child_stops, default=0))
    if stop < start:
        stop = start
    return start, stop


def fill_sibling_durations(steps: list[dict[str, Any]], parent_stop: int = 0) -> None:
    for index, step in enumerate(steps):
        children = step.get("steps") or []
        next_start = steps[index + 1].get("start") or 0 if index + 1 < len(steps) else parent_stop
        fill_sibling_durations(children, step.get("stop") or next_start)
        if (step.get("stop") or 0) <= (step.get("start") or 0):
            if children:
                step["stop"] = max((child.get("stop") or 0 for child in children), default=step.get("start") or 0)
            elif next_start > (step.get("start") or 0):
                step["stop"] = next_start


def parse_allure_metadata(activities: list[dict[str, Any]]) -> tuple[str, str, list[str], list[dict[str, Any]]]:
    epic = ""
    feature = ""
    tms: list[str] = []
    steps: list[dict[str, Any]] = []

    def walk(nodes: list[dict[str, Any]]) -> None:
        nonlocal epic, feature
        for node in nodes:
            title = node.get("title") or ""
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
                start, stop = step_times(node, child_steps)
                steps.append(
                    {
                        "name": title,
                        "status": "failed" if node.get("isAssociatedWithFailure") else "passed",
                        "stage": "finished",
                        "start": start,
                        "stop": stop,
                        "steps": child_steps,
                    }
                )
                continue
            walk(node.get("childActivities", []))

    walk(activities)
    fill_sibling_durations(steps)
    return epic, feature, tms, steps


def first_device(summary: dict[str, Any]) -> dict[str, Any]:
    configs = summary.get("devicesAndConfigurations") or []
    if configs and isinstance(configs[0], dict) and isinstance(configs[0].get("device"), dict):
        return configs[0]["device"]

    devices = summary.get("devices") or summary.get("environmentDevices") or []
    if devices and isinstance(devices[0], dict):
        first = devices[0]
        nested = first.get("device")
        return nested if isinstance(nested, dict) else first
    return {}


def write_environment(results_dir: Path, summary: dict[str, Any], api_base_url: str) -> None:
    device = first_device(summary)
    platform = str(device.get("platform") or "iOS").strip()
    os_name = "iOS" if "ios" in platform.lower() else platform
    os_version = str(device.get("osVersion") or "").strip()
    os_label = f"{os_name} {os_version}".strip()
    device_name = str(
        device.get("deviceName") or device.get("modelName") or "iOS Simulator"
    ).strip()
    if "simulator" in platform.lower() and "simulator" not in device_name.lower():
        device_name = f"{device_name} (Simulator)"
    lines = [
        "Framework=XCUITest",
        f"OS={os_label}",
        f"Device={device_name}",
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
