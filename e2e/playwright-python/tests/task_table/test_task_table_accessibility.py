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
    allure.dynamic.feature("Task table")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/98", link_type="tms", name="TMS #98")


@pytest.fixture(autouse=True)
def setup(support: SupportProvider):
    support.mock.api.get_tasks([
        TaskContext().create_task_response(),
        TaskContext().create_task_response(),
    ])


@pytest.mark.accessibility
def test_should_have_no_accessibility_violations_on_task_table(step: StepProvider, validate: ValidationProvider):
    # given
    step.navigation.open_main_page()

    # when
    results = step.accessibility.analyze()

    # then
    validate.accessibility.has_no_violations(results)
