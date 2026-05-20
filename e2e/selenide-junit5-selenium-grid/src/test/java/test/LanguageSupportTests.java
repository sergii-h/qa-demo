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

public interface LanguageSupportTests {
    StepProvider steps = new StepProvider();
    ValidationProvider validate = new ValidationProvider();

    SupportProvider support();

    @Test
    @DisplayName("Switch all UI text and status/priority tag values to Spanish when ES is selected")
    default void shouldSwitchUIToSpanishWhenESSelected() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .build();

        Response response = support().api.createTask(context.createTaskRequest());
        context.setResponse(response);

        steps.navigation.refresh();

        // when
        steps.language.selectLanguage("ES");

        // then
        validate.language.uiIsInSpanish();
        validate.language.statusTagShowsText("TODO", "Por hacer");
        validate.language.priorityTagShowsText("LOW", "Baja");
    }
}
