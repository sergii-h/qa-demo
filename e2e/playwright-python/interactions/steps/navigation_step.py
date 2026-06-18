from playwright.sync_api import Page
from decorators.step_decorator import step


class NavigationStep:
    def __init__(self, page: Page):
        self._page = page

    @step("Navigate to main page")
    def open_main_page(self) -> None:
        self._page.goto("/")

    @step("Refresh page")
    def refresh(self) -> None:
        self._page.reload()
