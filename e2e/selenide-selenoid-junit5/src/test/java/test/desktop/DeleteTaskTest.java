package test.desktop;

import context.TaskTestContext;
import io.qameta.allure.Epic;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

@Epic("Delete task")
class DeleteTaskTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Test
    @DisplayName("Delete task")
    void shouldDeleteTask() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();
        Response response = actions.api.createTask(context.createTaskRequest());
        context.setResponse(response);

        // when
        actions.navigation.refresh();
        actions.tasks.deleteTask(context.getId());

        // then
        validate.tasks.hasNoTask(context.getTitle());
    }
}
