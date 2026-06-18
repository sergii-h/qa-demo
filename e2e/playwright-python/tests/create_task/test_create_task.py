import allure
import pytest
from context.task_context import TaskContext
from data.allure_epic import AllureEpic
from providers.step_provider import StepProvider
from providers.validation_provider import ValidationProvider
from providers.support_provider import SupportProvider


@pytest.fixture(autouse=True)
def allure_metadata():
    allure.dynamic.epic(AllureEpic.TASK_MANAGEMENT)
    allure.dynamic.feature("Create task")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/100", link_type="tms", name="TMS #100")


@pytest.fixture
def context(support: SupportProvider) -> TaskContext:
    ctx = TaskContext()
    response = ctx.create_task_response()
    support.mock.api \
        .get_tasks([response]) \
        .create_task(response) \
        .get_task(response.id, response) \
        .get_is_valid(response.id, True)
    return ctx


def test_should_create_task(context: TaskContext, step: StepProvider, validate: ValidationProvider):
    # given
    step.navigation.open_main_page()

    # when
    step.tasks \
        .open_create_task_form() \
        .set_task_data(context.create_task_data()) \
        .submit_form()

    # then
    validate.tasks.has_task(context.title)

    # when
    step.tasks.open_task_info_form(context.title)

    # then
    validate.task.data(context.create_task_data())
