from playwright.sync_api import Page, Locator
from data.task_status import TaskStatus
from data.task_priority import TaskPriority


class EditTaskForm:
    def __init__(self, page: Page):
        self._page = page
        self.title_input: Locator = page.get_by_test_id("edit-task-title-input")
        self.description_input: Locator = page.locator("#description")
        self.status_dropdown: Locator = page.get_by_test_id("status-dropdown")
        self.priority_dropdown: Locator = page.get_by_test_id("priority-dropdown")
        self.submit_button: Locator = page.get_by_test_id("save-button")

    def status_option(self, status: TaskStatus) -> Locator:
        return self._page.get_by_test_id(f"status-dropdown-option-{status.value}")

    def priority_option(self, priority: TaskPriority) -> Locator:
        return self._page.get_by_test_id(f"priority-dropdown-option-{priority.value}")
