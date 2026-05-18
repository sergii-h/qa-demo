package test;

import context.TaskTestContext;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;

public interface DeleteTaskTests {

    @Test
    @DisplayName("Delete task")
    default void shouldDeleteTask() {
        ActionManager actions = new ActionManager();
        ValidationManager validate = new ValidationManager();

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
