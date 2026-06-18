package test.spec.deleteTask;

import context.TaskTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface IDeleteTaskTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    SupportProvider support();

    @BeforeEach
    default void init() {
        var response = context.createTaskResponse();

        support().mock.api()
                .getTasks(List.of(response))
                .deleteTask(response.getId());
    }

    @Test
    @DisplayName("Should delete task")
    default void shouldDeleteTask() {
        // given
        steps.navigation.openMainPage();

        // when
        steps.tasks.deleteTask(context.getTitle());

        // then
        validate.tasks.hasNoTask(context.getTitle());
    }
}
