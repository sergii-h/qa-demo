package test;

import context.TaskTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.ValidationProvider;

public interface CreateTaskTests {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    @Test
    @DisplayName("Create task with all fields")
    default void shouldCreateTask() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();

        // when
        steps.tasks
                .openCreateTaskForm()
                .setTaskData(context.createTaskData())
                .submitForm();

        // then
        validate.tasks.hasTask(context.getTitle());

        // when
        String taskId = steps.tasks.getTaskIdByTitle(context.getTitle());
        steps.tasks.openTaskInfoForm(taskId);

        // then
        validate.task.data(context.createTaskData());
    }
}
