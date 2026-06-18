from playwright.sync_api import Page, expect
from data.task_data import TaskData
from interactions.pages.task_info_modal import TaskInfoModal
from decorators.step_decorator import step


class TaskValidator:
    def __init__(self, page: Page):
        self._modal = TaskInfoModal(page)

    @step("Validate task info data: {task_data}")
    def data(self, task_data: TaskData) -> "TaskValidator":
        expect(self._modal.title).to_have_text(task_data.title)
        expect(self._modal.description).to_have_text(task_data.description)
        expect(self._modal.status_tag(task_data.status)).to_be_visible()
        expect(self._modal.priority_tag(task_data.priority)).to_be_visible()
        return self

    @step("Validate task is marked as valid")
    def is_valid(self) -> "TaskValidator":
        expect(self._modal.validation_badge).to_be_visible()
        return self

    @step("Validate task is marked as not valid")
    def is_not_valid(self) -> "TaskValidator":
        expect(self._modal.validation_badge).not_to_be_visible()
        return self
