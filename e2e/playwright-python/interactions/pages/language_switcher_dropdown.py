from playwright.sync_api import Page, Locator


class LanguageSwitcherDropdown:
    def __init__(self, page: Page):
        self.dropdown: Locator = page.get_by_test_id("language-switcher")
        self.dropdown_value: Locator = self.dropdown.locator("span.p-dropdown-label")
        self.items: Locator = page.locator(".p-dropdown-item")
