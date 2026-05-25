package test.spec.translation;

import context.TaskTestContext;
import data.TaskPriority;
import data.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.StepProvider;
import provider.SupportProvider;
import provider.ValidationProvider;

import java.util.List;

public interface ILanguageSupportTest {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();
    TaskTestContext context = TaskTestContext.builder().build();

    SupportProvider support();

    @BeforeEach
    default void init() {
        context.setStatus(TaskStatus.TODO);
        context.setPriority(TaskPriority.LOW);

        var response = context.createTaskResponse();

        support().mock.api().getTasks(List.of(response));
        support().mock.api().createTask(response);
    }

    @Test
    @DisplayName("Should switch all UI text and status/priority tag values to Spanish when ES is selected")
    default void shouldSwitchUIToSpanishWhenESSelected() {
        // given
        steps.navigation.openMainPage();

        // when
        steps.language.selectLanguage("ES");

        // then
        validate.language.uiIsInSpanish();
        validate.language.statusTagShowsText("TODO", "Por hacer");
        validate.language.priorityTagShowsText("LOW", "Baja");
    }
}
