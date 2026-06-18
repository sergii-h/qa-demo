from playwright.sync_api import Page, Locator
from data.task_status import TaskStatus
from data.task_priority import TaskPriority


class TaskInfoModal:
    def __init__(self, page: Page):
        self._page = page
        self.title: Locator = page.get_by_test_id("modal-title")
        self.description: Locator = page.get_by_test_id("description")
        self.validation_badge: Locator = page.get_by_test_id("valid")

    def status_tag(self, status: TaskStatus) -> Locator:
        return self._page.get_by_test_id("status").get_by_test_id(f"status-tag-{status.value}")

    def priority_tag(self, priority: TaskPriority) -> Locator:
        return self._page.get_by_test_id("priority").get_by_test_id(f"priority-tag-{priority.value}")
