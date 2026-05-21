package test;

import context.TaskTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

public interface TaskInfoTests {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    SupportProvider support();

    @BeforeEach
    default void setUpTaskInfoMocks() {
        support().wiremock
                .clearMocks()
                .setIsValidMock(true);
    }

    @Test
    @DisplayName("View task info")
    default void shouldViewTaskInfo() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();
        support().api.createTask(context.createTaskRequest());

        // when
        steps.navigation.refresh();
        steps.tasks.openTaskInfoForm(context.getTitle());

        // then
        validate.task
                .data(context.createTaskData())
                .isValid();
    }
}
