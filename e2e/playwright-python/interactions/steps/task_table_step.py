from playwright.sync_api import Page, expect
from interactions.pages.main_page import MainPage
from interactions.pages.create_task_form import CreateTaskForm
from interactions.pages.edit_task_form import EditTaskForm
from interactions.pages.task_info_modal import TaskInfoModal
from interactions.steps.create_task_step import CreateTaskStep
from interactions.steps.edit_task_step import EditTaskStep
from decorators.step_decorator import step


class TaskTableStep:
    def __init__(self, page: Page):
        self._page = page
        self._main_page = MainPage(page)
        self._create_task_form = CreateTaskForm(page)
        self._task_info_modal = TaskInfoModal(page)
        self._edit_task_form = EditTaskForm(page)

    @step("Open create task form")
    def open_create_task_form(self) -> CreateTaskStep:
        self._main_page.create_task_button.click()
        expect(self._create_task_form.submit_button).to_be_visible()
        return CreateTaskStep(self._page)

    @step("Open task info for task '{title}'")
    def open_task_info_form(self, title: str) -> None:
        task_id = self._resolve_task_id(title)
        self._main_page.task_info_button_by_id(task_id).click()
        expect(self._task_info_modal.title).to_be_visible()

    @step("Open edit form for task '{title}'")
    def open_edit_task_form(self, title: str) -> EditTaskStep:
        task_id = self._resolve_task_id(title)
        self._main_page.task_edit_button_by_id(task_id).click()
        expect(self._edit_task_form.title_input).to_have_value(title)
        return EditTaskStep(self._page)

    @step("Delete task '{title}'")
    def delete_task(self, title: str) -> None:
        task_id = self._resolve_task_id(title)
        delete_button = self._main_page.task_delete_button_by_id(task_id)
        delete_button.click()
        expect(delete_button).not_to_be_visible()

    def _resolve_task_id(self, title: str) -> str:
        data_test_id = self._main_page.task_title_by_title(title).get_attribute("data-testid")
        return data_test_id.replace("task-title-", "")
