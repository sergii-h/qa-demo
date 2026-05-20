package test;

import context.TaskTestContext;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

public interface DeleteTaskTests {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    SupportProvider support();

    @Test
    @DisplayName("Delete task")
    default void shouldDeleteTask() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();
        Response response = support().api.createTask(context.createTaskRequest());
        context.setResponse(response);

        // when
        steps.navigation.refresh();
        steps.tasks.deleteTask(context.getId());

        // then
        validate.tasks.hasNoTask(context.getTitle());
    }
}
