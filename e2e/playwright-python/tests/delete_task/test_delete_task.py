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
    allure.dynamic.feature("Delete task")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/99", link_type="tms", name="TMS #99")


@pytest.fixture
def context(support: SupportProvider) -> TaskContext:
    ctx = TaskContext()
    response = ctx.create_task_response()
    support.mock.api \
        .get_tasks([response]) \
        .delete_task(response.id)
    return ctx


def test_should_delete_task(context: TaskContext, step: StepProvider, validate: ValidationProvider):
    # given
    step.navigation.open_main_page()

    # when
    step.tasks.delete_task(context.title)

    # then
    validate.tasks.has_no_task(context.title)
