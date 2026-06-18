import allure
import pytest
from data.allure_epic import AllureEpic
from providers.step_provider import StepProvider
from providers.validation_provider import ValidationProvider
from providers.support_provider import SupportProvider


@pytest.fixture(autouse=True)
def allure_metadata():
    allure.dynamic.epic(AllureEpic.ACCESSIBILITY)
    allure.dynamic.feature("Create task")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/100", link_type="tms", name="TMS #100")


@pytest.fixture(autouse=True)
def setup(support: SupportProvider):
    support.mock.api.get_tasks([])


@pytest.mark.accessibility
def test_should_have_no_accessibility_violations_on_create_task_form(step: StepProvider, validate: ValidationProvider):
    # given
    step.navigation.open_main_page()
    step.tasks.open_create_task_form()

    # when
    results = step.accessibility.analyze()

    # then
    validate.accessibility.has_no_violations(results)
