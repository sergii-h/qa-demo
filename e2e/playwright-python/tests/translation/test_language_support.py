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
    allure.dynamic.epic(AllureEpic.TRANSLATION)
    allure.dynamic.feature("Language support")
    allure.dynamic.link("https://github.com/sergii-h/qa-demo/issues/104", link_type="tms", name="TMS #104")


@pytest.fixture
def context(support: SupportProvider) -> TaskContext:
    ctx = TaskContext(status=TaskStatus.TODO, priority=TaskPriority.LOW)
    response = ctx.create_task_response()
    support.mock.api \
        .get_tasks([response]) \
        .create_task(response)
    return ctx


def test_should_switch_ui_to_spanish_when_es_is_selected(
    context: TaskContext, step: StepProvider, validate: ValidationProvider
):
    # given
    step.navigation.open_main_page()

    # when
    step.language.select_language("ES")

    # then
    validate.language.ui_is_in_spanish()
    validate.language.status_tag_shows_text("TODO", "Por hacer")
    validate.language.priority_tag_shows_text("LOW", "Baja")
