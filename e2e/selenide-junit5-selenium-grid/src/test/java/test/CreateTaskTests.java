package test;

import context.TaskTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;

public interface CreateTaskTests {

    @Test
    @DisplayName("Create task with all fields")
    default void shouldCreateTask() {
        ActionManager actions = new ActionManager();
        ValidationManager validate = new ValidationManager();

        // given
        TaskTestContext context = TaskTestContext.builder().build();

        // when
        actions.tasks
                .openCreateTaskForm()
                .setTaskData(context.createTaskData())
                .submitForm();

        // then
        validate.tasks.hasTask(context.getTitle());

        // when
        String taskId = actions.tasks.getTaskIdByTitle(context.getTitle());
        actions.tasks.openTaskInfoForm(taskId);

        // then
        validate.task.data(context.createTaskData());
    }
}
