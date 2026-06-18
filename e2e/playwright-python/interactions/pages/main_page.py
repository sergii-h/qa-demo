from playwright.sync_api import Page, Locator


class MainPage:
    def __init__(self, page: Page):
        self._page = page
        self.task_rows: Locator = page.locator('[data-testid^="task-title-"]')
        self.create_task_button: Locator = page.get_by_test_id("add-task-button")
        self.language_switcher: Locator = page.get_by_test_id("language-switcher")
        self.table_headers: Locator = page.locator(".p-datatable-thead")

    def task_row_by_title(self, title: str) -> Locator:
        return self._page.locator('[data-testid^="task-title-"]').filter(has_text=title)

    def task_info_button_by_id(self, task_id: str) -> Locator:
        return self._page.get_by_test_id(f"info-button-{task_id}")

    def task_edit_button_by_id(self, task_id: str) -> Locator:
        return self._page.get_by_test_id(f"edit-button-{task_id}")

    def task_delete_button_by_id(self, task_id: str) -> Locator:
        return self._page.get_by_test_id(f"delete-button-{task_id}")

    def task_title_by_title(self, title: str) -> Locator:
        return self._page.locator('[data-testid^="task-title-"]').filter(has_text=title)

    def status_tag(self, status: str) -> Locator:
        return self._page.get_by_test_id(f"status-tag-{status}").first

    def priority_tag(self, priority: str) -> Locator:
        return self._page.get_by_test_id(f"priority-tag-{priority}").first
