import allure
import pytest
from context.task_context import TaskContext
from data.allure_epic import AllureEpic
from providers.step_provider import StepProvider
from providers.validation_provider import ValidationProvider
from providers.support_provider import SupportProvider


@pytest.fixture(autouse=True)
def allure_metadata():
    allure.dynamic.epic(AllureEpic.ACCESSIBILITY)
    allure.dynamic.feature("View task info")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/102", link_type="tms", name="TMS #102")


@pytest.fixture
def context(support: SupportProvider) -> TaskContext:
    ctx = TaskContext()
    response = ctx.create_task_response()
    support.mock.api \
        .get_tasks([response]) \
        .get_task(response.id, response) \
        .get_is_valid(response.id, True)
    return ctx


@pytest.mark.accessibility
def test_should_have_no_accessibility_violations_on_task_info_modal(
    context: TaskContext, step: StepProvider, validate: ValidationProvider
):
    # given
    step.navigation.open_main_page()
    step.tasks.open_task_info_form(context.title)

    # when
    results = step.accessibility.analyze()

    # then
    validate.accessibility.has_no_violations(results)
