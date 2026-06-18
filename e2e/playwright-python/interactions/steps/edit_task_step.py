from __future__ import annotations

from typing import TYPE_CHECKING

from playwright.sync_api import Page, expect
from data.task_data import TaskData
from interactions.pages.edit_task_form import EditTaskForm
from decorators.step_decorator import step

if TYPE_CHECKING:
    from interactions.steps.task_table_step import TaskTableStep


class EditTaskStep:
    def __init__(self, page: Page):
        self._page = page
        self._form = EditTaskForm(page)

    @step("Set task data with {task_data}")
    def set_task_data(self, task_data: TaskData) -> EditTaskStep:
        if task_data.title:
            self._form.title_input.clear()
            self._form.title_input.fill(task_data.title)
        if task_data.description:
            self._form.description_input.clear()
            self._form.description_input.fill(task_data.description)
        if task_data.status:
            self._form.status_dropdown.click()
            self._form.status_option(task_data.status).click()
        if task_data.priority:
            self._form.priority_dropdown.click()
            self._form.priority_option(task_data.priority).click()
        return self

    @step("Submit edit task form")
    def submit_form(self) -> TaskTableStep:
        from interactions.steps.task_table_step import TaskTableStep

        self._form.submit_button.click()
        expect(self._form.submit_button).not_to_be_visible()
        return TaskTableStep(self._page)
