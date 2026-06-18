from playwright.sync_api import Page
from axe_playwright_python.sync_playwright import Axe
from decorators.step_decorator import step


class AccessibilityStep:
    def __init__(self, page: Page):
        self._page = page
        self._axe = Axe()

    @step("Analyze page accessibility with axe")
    def analyze(self, selector: str | None = None):
        context = {"include": [[selector]]} if selector else None
        return self._axe.run(self._page, context=context)
