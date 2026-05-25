package test.spec.createTask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface ICreateTaskAccessibilityTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    SupportProvider support();

    @BeforeEach
    default void init() {
        support().mock.api().getTasks(List.of());
    }

    @Test
    @Tag("accessibility")
    @DisplayName("Should have no accessibility violations on create task form")
    default void shouldHaveNoAccessibilityViolationsOnCreateTaskForm() {
        // given
        steps.navigation.openMainPage();
        steps.tasks.openCreateTaskForm();

        // when
        var results = steps.accessibility.analyze();

        // then
        validate.accessibility.hasNoViolations(results);
    }
}
