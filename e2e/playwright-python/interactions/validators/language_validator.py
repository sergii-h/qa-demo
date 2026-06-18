from playwright.sync_api import Page, expect
from interactions.pages.main_page import MainPage
from decorators.step_decorator import step


class LanguageValidator:
    def __init__(self, page: Page):
        self._main_page = MainPage(page)

    @step("Validate UI is in Spanish")
    def ui_is_in_spanish(self) -> None:
        expect(self._main_page.create_task_button).to_have_text("Crear tarea")
        expect(self._main_page.table_headers).to_contain_text("Título")
        expect(self._main_page.table_headers).to_contain_text("Estado")
        expect(self._main_page.table_headers).to_contain_text("Prioridad")

    @step("Validate status tag for {status} shows {expected_text}")
    def status_tag_shows_text(self, status: str, expected_text: str) -> None:
        expect(self._main_page.status_tag(status)).to_have_text(expected_text)

    @step("Validate priority tag for {priority} shows {expected_text}")
    def priority_tag_shows_text(self, priority: str, expected_text: str) -> None:
        expect(self._main_page.priority_tag(priority)).to_have_text(expected_text)
