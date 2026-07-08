import platform
from pathlib import Path

import config


def build_allure_environment_info() -> dict[str, str]:
    return {
        "Framework": "Playwright",
        "os_release": platform.release(),
        "os_version": platform.version(),
        "python_version": platform.python_version(),
        "environment": config.base_url,
    }


def write_allure_environment_info(results_dir: Path) -> None:
    lines = "\n".join(f"{key}={value}" for key, value in build_allure_environment_info().items())
    results_dir.mkdir(parents=True, exist_ok=True)
    (results_dir / "environment.properties").write_text(f"{lines}\n", encoding="utf-8")
