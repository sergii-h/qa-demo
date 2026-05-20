package test;

import context.TaskTestContext;
import data.TaskPriority;
import data.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

public interface EditTaskTests {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    SupportProvider support();

    @Test
    @DisplayName("Edit task")
    default void shouldEditTask() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Response response = support().api.createTask(context.createTaskRequest());
        context.setResponse(response);

        TaskTestContext updatedContext = TaskTestContext.builder()
                .title(context.getTitle() + "-Updated")
                .description(context.getDescription() + "-Updated")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // when
        steps.navigation.refresh();
        steps.tasks
                .openTaskEditForm(context.getId())
                .setTaskData(updatedContext.createTaskData())
                .submitForm()
                .openTaskInfoForm(context.getId());

        // then
        validate.task.data(updatedContext.createTaskData());
    }
}
