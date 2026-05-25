package test.spec.createTask;

import context.TaskTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface ICreateTaskTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    SupportProvider support();

    @BeforeEach
    default void init() {
        var response = context.createTaskResponse();

        support().mock.api().getTasks(List.of(response));
        support().mock.api().createTask(response);
        support().mock.api().getTask(response.getId(), response);
        support().mock.api().getIsValid(response.getId(), true);
    }

    @Test
    @DisplayName("Should create task")
    default void shouldCreateTask() {
        // given
        steps.navigation.openMainPage();

        // when
        steps.tasks
                .openCreateTaskForm()
                .setTaskData(context.createTaskData())
                .submitForm();

        // then
        validate.tasks.hasTask(context.getTitle());

        // when
        steps.tasks.openTaskInfoForm(context.getTitle());

        // then
        validate.task.data(context.createTaskData());
    }
}
