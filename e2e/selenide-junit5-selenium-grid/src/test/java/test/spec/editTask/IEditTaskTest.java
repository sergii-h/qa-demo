package test.spec.editTask;

import context.TaskTestContext;
import data.TaskPriority;
import data.TaskStatus;
import interaction.step.EditTaskStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface IEditTaskTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    SupportProvider support();

    @BeforeEach
    default void init() {
        context.setStatus(TaskStatus.TODO);
        context.setPriority(TaskPriority.MEDIUM);

        support().mock.api().getTasks(List.of(context.createTaskResponse()));
    }

    @Test
    @DisplayName("Should edit task")
    default void shouldEditTask() {
        // given
        steps.navigation.openMainPage();

        TaskTestContext updatedContext = TaskTestContext.builder()
                .id(context.getId())
                .title(context.getTitle() + "-Updated")
                .description(context.getDescription() + "-Updated")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // when
        EditTaskStep editTaskStep = steps.tasks
                .openTaskEditForm(context.getTitle())
                .setTaskData(updatedContext.createTaskData());

        var updatedResponse = updatedContext.createTaskResponse();

        support().mock.api().updateTask(updatedContext.getId(), updatedResponse);
        support().mock.api().getTasks(List.of(updatedResponse));
        support().mock.api().getTask(updatedContext.getId(), updatedResponse);
        support().mock.api().getIsValid(updatedContext.getId(), true);

        editTaskStep.submitForm();
        steps.tasks.openTaskInfoForm(updatedContext.getTitle());

        // then
        validate.task.data(updatedContext.createTaskData());
    }
}
