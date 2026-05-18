package test;

import context.TaskTestContext;
import data.TaskPriority;
import data.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;

public interface EditTaskTests {

    @Test
    @DisplayName("Edit task")
    default void shouldEditTask() {
        ActionManager actions = new ActionManager();
        ValidationManager validate = new ValidationManager();

        // given
        TaskTestContext context = TaskTestContext.builder()
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Response response = actions.api.createTask(context.createTaskRequest());
        context.setResponse(response);

        TaskTestContext updatedContext = TaskTestContext.builder()
                .title(context.getTitle() + "-Updated")
                .description(context.getDescription() + "-Updated")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // when
        actions.navigation.refresh();
        actions.tasks
                .openTaskEditForm(context.getId())
                .setTaskData(updatedContext.createTaskData())
                .submitForm()
                .openTaskInfoForm(context.getId());

        // then
        validate.task.data(updatedContext.createTaskData());
    }
}
