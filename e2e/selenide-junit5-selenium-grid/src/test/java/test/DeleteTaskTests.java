package test;

import context.TaskTestContext;
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
        support().api.createTask(context.createTaskRequest());

        // when
        steps.navigation.refresh();
        steps.tasks.deleteTask(context.getTitle());

        // then
        validate.tasks.hasNoTask(context.getTitle());
    }
}
