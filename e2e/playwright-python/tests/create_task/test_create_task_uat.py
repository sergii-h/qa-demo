import allure
import pytest
from context.task_context import TaskContext
from data.allure_epic import AllureEpic
from providers.step_provider import StepProvider
from providers.validation_provider import ValidationProvider


@pytest.fixture(autouse=True)
def allure_metadata():
    allure.dynamic.epic(AllureEpic.TASK_MANAGEMENT)
    allure.dynamic.feature("Create task")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/100", link_type="tms", name="TMS #100")


@pytest.mark.uat
def test_should_create_task(step: StepProvider, validate: ValidationProvider):
    # given
    context = TaskContext()

    # when
    step.navigation.open_main_page()
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
