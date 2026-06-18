from playwright.sync_api import Page, expect
from interactions.pages.language_switcher_dropdown import LanguageSwitcherDropdown
from decorators.step_decorator import step


class LanguageSwitcherStep:
    def __init__(self, page: Page):
        self._dropdown = LanguageSwitcherDropdown(page)

    @step("Select language '{language}'")
    def select_language(self, language: str) -> None:
        self._dropdown.dropdown.click()
        self._dropdown.items.filter(has_text=language).click()
        expect(self._dropdown.dropdown_value).to_have_text(language)
