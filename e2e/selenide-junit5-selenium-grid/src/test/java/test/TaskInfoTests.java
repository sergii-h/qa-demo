package test;

import context.TaskTestContext;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;

public interface TaskInfoTests {

    @BeforeEach
    default void setUpTaskInfoMocks() {
        new ActionManager().wiremock
                .clearMocks()
                .setIsValidMock(true);
    }

    @Test
    @DisplayName("View task info")
    default void shouldViewTaskInfo() {
        ActionManager actions = new ActionManager();
        ValidationManager validate = new ValidationManager();

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
