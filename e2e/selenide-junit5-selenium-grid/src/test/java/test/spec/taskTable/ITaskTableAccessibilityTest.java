package test.spec.taskTable;

import context.TaskTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface ITaskTableAccessibilityTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    SupportProvider support();

    @BeforeEach
    default void init() {
        support().mock.api().getTasks(List.of(
                TaskTestContext.builder().build().createTaskResponse(),
                TaskTestContext.builder().build().createTaskResponse()
        ));
    }

    @Test
    @Tag("accessibility")
    @DisplayName("Should have no accessibility violations on task table")
    default void shouldHaveNoAccessibilityViolationsOnTaskTable() {
        // given
        steps.navigation.openMainPage();

        // when
        var results = steps.accessibility.analyze();

        // then
        validate.accessibility.hasNoViolations(results);
    }
}
