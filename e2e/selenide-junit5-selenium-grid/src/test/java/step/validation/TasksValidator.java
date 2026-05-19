package step.validation;

import io.qameta.allure.Step;
import org.hamcrest.CoreMatchers;
import page.MainPage;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class TasksValidator {
    MainPage mainPage = new MainPage();

    @Step("Validate task list has task '{title}'")
    public void hasTask(String title) {
        assertThat(mainPage.getTaskTitles(), CoreMatchers.hasItem(title.trim()));
    }

    @Step("Validate task '{title}' is removed from list")
    public void hasNoTask(String title) {
        assertThat(mainPage.getTaskTitles(), not(CoreMatchers.hasItem(title.trim())));
    }
}
