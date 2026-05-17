package test.desktop;

import context.TaskTestContext;
import data.TaskPriority;
import data.TaskStatus;
import io.qameta.allure.Epic;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

@Epic("Language support")
class LanguageSupportTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Test
    @DisplayName("Switch all UI text and status/priority tag values to Spanish when ES is selected")
    void shouldSwitchUIToSpanishWhenESSelected() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .build();
        Response response = actions.api.createTask(context.createTaskRequest());
        context.setResponse(response);
        
        actions.navigation.refresh();

        // when
        actions.language.selectLanguage("ES");

        // then
        validate.language.uiIsInSpanish();
        validate.language.statusTagShowsText("TODO", "Por hacer");
        validate.language.priorityTagShowsText("LOW", "Baja");
    }
}
