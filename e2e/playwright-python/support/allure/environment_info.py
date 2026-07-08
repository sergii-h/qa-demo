import platform
import re
import subprocess
import sys
from pathlib import Path

import config

BROWSER_LINE = re.compile(
    r"^(.+?)\s+\(playwright (chromium|webkit|firefox)(?:-headless-shell)? v\d+\)"
)


def _extract_version(label: str) -> str:
    match = re.search(r"(\d+(?:\.\d+)*)", label)
    return match.group(1) if match else label


def _resolve_browser_versions_from_cli(browsers: list[str]) -> tuple[str, str]:
    result = subprocess.run(
        [sys.executable, "-m", "playwright", "install", "--dry-run", *browsers],
        capture_output=True,
        text=True,
        check=True,
    )

    versions: dict[str, str] = {}
    for line in result.stdout.splitlines():
        match = BROWSER_LINE.match(line.strip())
        if not match:
            continue

        label, browser_name = match.groups()
        if "headless-shell" in browser_name or browser_name in versions:
            continue

        versions[browser_name] = _extract_version(label)

    browser_versions = "; ".join(f"{name} {versions.get(name, 'unknown')}" for name in browsers)
    return ", ".join(browsers), browser_versions


def build_allure_environment_info(browsers: list[str] | None = None) -> dict[str, str]:
    browser_types = ", ".join(browsers) if browsers else "unknown"
    browser_versions = "unknown"

    if browsers:
        _, browser_versions = _resolve_browser_versions_from_cli(browsers)

    return {
        "Framework": "Playwright",
        "os_release": platform.release(),
        "os_version": platform.version(),
        "python_version": platform.python_version(),
        "environment": config.base_url,
        "browser": browser_types,
        "browser_version": browser_versions,
    }


def write_allure_environment_info(results_dir: Path, browsers: list[str] | None = None) -> None:
    lines = "\n".join(
        f"{key}={value}" for key, value in build_allure_environment_info(browsers).items()
    )
    results_dir.mkdir(parents=True, exist_ok=True)
    (results_dir / "environment.properties").write_text(f"{lines}\n", encoding="utf-8")
