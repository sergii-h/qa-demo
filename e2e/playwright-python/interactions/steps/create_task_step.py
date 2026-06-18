from __future__ import annotations

from playwright.sync_api import Page, expect
from data.task_data import TaskData
from interactions.pages.create_task_form import CreateTaskForm
from decorators.step_decorator import step


class CreateTaskStep:
    def __init__(self, page: Page):
        self._form = CreateTaskForm(page)

    @step("Set task data with {task_data}")
    def set_task_data(self, task_data: TaskData) -> CreateTaskStep:
        self._form.title_input.fill(task_data.title)
        self._form.description_input.fill(task_data.description)
        self._form.status_dropdown.click()
        self._form.status_option(task_data.status).click()
        self._form.priority_dropdown.click()
        self._form.priority_option(task_data.priority).click()
        return self

    @step("Submit create task form")
    def submit_form(self) -> None:
        self._form.submit_button.click()
        expect(self._form.submit_button).not_to_be_visible()
