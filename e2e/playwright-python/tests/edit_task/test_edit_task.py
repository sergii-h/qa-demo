import allure
import pytest
from context.task_context import TaskContext
from data.allure_epic import AllureEpic
from data.task_status import TaskStatus
from data.task_priority import TaskPriority
from providers.step_provider import StepProvider
from providers.validation_provider import ValidationProvider
from providers.support_provider import SupportProvider


@pytest.fixture(autouse=True)
def allure_metadata():
    allure.dynamic.epic(AllureEpic.TASK_MANAGEMENT)
    allure.dynamic.feature("Edit task")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/101", link_type="tms", name="TMS #101")


@pytest.fixture
def context(support: SupportProvider) -> TaskContext:
    ctx = TaskContext(status=TaskStatus.TODO, priority=TaskPriority.MEDIUM)
    response = ctx.create_task_response()
    support.mock.api \
        .get_tasks([response]) \
        .get_task(response.id, response) \
        .get_is_valid(response.id, True)
    return ctx


def test_should_edit_task(context: TaskContext, step: StepProvider, validate: ValidationProvider, support: SupportProvider):
    # given
    updated_context = TaskContext(
        id=context.id,
        title=context.title + "-Updated",
        description=context.description + "-Updated",
        status=TaskStatus.IN_PROGRESS,
        priority=TaskPriority.HIGH,
    )
    updated_response = updated_context.create_task_response()

    step.navigation.open_main_page()

    # when
    edit_task_step = step.tasks \
        .open_edit_task_form(context.title) \
        .set_task_data(updated_context.create_task_data())

    support.mock.api \
        .update_task(updated_context.id, updated_response) \
        .get_tasks([updated_response]) \
        .get_task(updated_context.id, updated_response) \
        .get_is_valid(updated_context.id, True)

    edit_task_step.submit_form()
    step.tasks.open_task_info_form(updated_context.title)

    # then
    validate.task.data(updated_context.create_task_data())
