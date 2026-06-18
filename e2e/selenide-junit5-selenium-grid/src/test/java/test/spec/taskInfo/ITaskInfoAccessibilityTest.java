package test.spec.taskInfo;

import context.TaskTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface ITaskInfoAccessibilityTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    SupportProvider support();

    @BeforeEach
    default void init() {
        var response = context.createTaskResponse();

        support().mock.api()
                .getTasks(List.of(response))
                .getTask(response.getId(), response)
                .getIsValid(response.getId(), true);
    }

    @Test
    @Tag("accessibility")
    @DisplayName("Should have no accessibility violations on task info modal")
    default void shouldHaveNoAccessibilityViolationsOnTaskInfoModal() {
        // given
        steps.navigation.openMainPage();
        steps.tasks.openTaskInfoForm(context.getTitle());

        // when
        var results = steps.accessibility.analyze();

        // then
        validate.accessibility.hasNoViolations(results);
    }
}
