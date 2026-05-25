package test.spec.createTask;

import context.TaskTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.ValidationProvider;

public interface ICreateTaskUatTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    @Test
    @Tag("uat")
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
