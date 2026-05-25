package test.spec.taskInfo;

import context.TaskTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface ITaskInfoTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    SupportProvider support();

    @BeforeEach
    default void init() {
        var response = context.createTaskResponse();

        support().mock.api().getTasks(List.of(response));
        support().mock.api().getTask(response.getId(), response);
        support().mock.api().getIsValid(response.getId(), true);
    }

    @Test
    @DisplayName("Should view task info")
    default void shouldViewTaskInfo() {
        // given
        steps.navigation.openMainPage();

        // when
        steps.tasks.openTaskInfoForm(context.getTitle());

        // then
        validate.task
                .data(context.createTaskData())
                .isValid();
    }
}
