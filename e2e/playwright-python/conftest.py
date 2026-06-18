import sys
from pathlib import Path
from typing import Any, Optional

sys.path.insert(0, str(Path(__file__).parent))

import pytest
from playwright.sync_api import Page, Playwright

import config
from providers.step_provider import StepProvider
from providers.validation_provider import ValidationProvider
from providers.support_provider import SupportProvider

DESKTOP_BROWSER = "chromium"
MOBILE_BROWSER = "webkit"
DESKTOP_DEVICE = "Desktop Chrome"
MOBILE_DEVICE = "iPhone 12 Pro"


@pytest.fixture(scope="session")
def base_url() -> str:
    return config.base_url


@pytest.fixture(scope="session")
def browser_context_args(
    pytestconfig: Any,
    playwright: Playwright,
    browser_name: str,
    device: Optional[str],
    base_url: Optional[str],
    _pw_artifacts_folder,
) -> dict:
    context_args: dict = {}
    if device:
        context_args.update(playwright.devices[device])
    elif browser_name == MOBILE_BROWSER:
        context_args.update(playwright.devices[MOBILE_DEVICE])
    elif browser_name == DESKTOP_BROWSER:
        context_args.update(playwright.devices[DESKTOP_DEVICE])
    if base_url:
        context_args["base_url"] = base_url

    video_option = pytestconfig.getoption("--video")
    if video_option in ["on", "retain-on-failure"]:
        context_args["record_video_dir"] = _pw_artifacts_folder.name

    return context_args


@pytest.fixture
def is_mobile(pytestconfig, browser_name: str) -> bool:
    if pytestconfig.getoption("--device", default=None):
        return True
    return browser_name == MOBILE_BROWSER


@pytest.fixture
def step(page: Page, is_mobile: bool) -> StepProvider:
    return StepProvider(page, is_mobile)


@pytest.fixture
def validate(page: Page, is_mobile: bool) -> ValidationProvider:
    return ValidationProvider(page, is_mobile)


@pytest.fixture
def support(page: Page) -> SupportProvider:
    return SupportProvider(page)
