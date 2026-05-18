package step.validation;

import io.qameta.allure.Step;
import page.MainPage;

import static com.codeborne.selenide.Condition.text;

public class LanguageValidator {
    MainPage mainPage = new MainPage();

    @Step("Validate table headers and buttons are in Spanish")
    public void uiIsInSpanish() {
        mainPage.createTaskButton.shouldHave(text("Crear tarea"));
        mainPage.tableHeaders.shouldHave(text("Título"));
        mainPage.tableHeaders.shouldHave(text("Estado"));
        mainPage.tableHeaders.shouldHave(text("Prioridad"));
    }

    @Step("Validate status tag for '{status}' shows '{expectedText}'")
    public void statusTagShowsText(String status, String expectedText) {
        mainPage.statusTag(status).shouldHave(text(expectedText));
    }

    @Step("Validate priority tag for '{priority}' shows '{expectedText}'")
    public void priorityTagShowsText(String priority, String expectedText) {
        mainPage.priorityTag(priority).shouldHave(text(expectedText));
    }
}
