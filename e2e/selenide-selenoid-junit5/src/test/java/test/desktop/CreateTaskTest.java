package test.desktop;

import context.TaskTestContext;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

@Epic("Create task")
class CreateTaskTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Test
    @DisplayName("Create task with all fields")
    void shouldCreateTask() {
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
