package test.desktop;

import context.TaskTestContext;
import io.qameta.allure.Epic;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

@Epic("Task info")
class TaskInfoTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @BeforeEach
    void beforeEach() {
        actions.wiremock
                .clearMocks()
                .setIsValidMock(true);
    }

    @Test
    @DisplayName("View task info")
    void shouldViewTaskInfo() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();
        Response response = actions.api.createTask(context.createTaskRequest());
        context.setResponse(response);

        // when
        actions.navigation.refresh();
        actions.tasks.openTaskInfoForm(context.getId());

        // then
        validate.task
                .data(context.createTaskData())
                .isValid();
    }
}
