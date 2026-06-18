from playwright.sync_api import Page, expect
from interactions.pages.main_page import MainPage
from decorators.step_decorator import step


class TasksValidator:
    def __init__(self, page: Page):
        self._main_page = MainPage(page)

    @step("Validate task with title {title} is visible")
    def has_task(self, title: str) -> "TasksValidator":
        expect(self._main_page.task_row_by_title(title)).to_be_visible()
        return self

    @step("Validate task with title {title} is not visible")
    def has_no_task(self, title: str) -> "TasksValidator":
        expect(self._main_page.task_row_by_title(title)).not_to_be_visible()
        return self

    @step("Validate task count is {count}")
    def has_task_count(self, count: int) -> "TasksValidator":
        expect(self._main_page.task_rows).to_have_count(count)
        return self
