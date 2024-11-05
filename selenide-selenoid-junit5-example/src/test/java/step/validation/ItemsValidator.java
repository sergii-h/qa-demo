package step.validation;

import io.qameta.allure.Step;
import org.hamcrest.CoreMatchers;
import page.MainPage;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ItemsValidator {
    MainPage mainPage = new MainPage();

    @Step("Validate item list has item '{itemName}'")
    public void hasItem(String itemName) {
        assertThat(mainPage.getItemNames(), CoreMatchers.hasItem(itemName.trim()));
    }

    @Step("Validate item '{itemName}' deleted")
    public void hasNoItem(String itemName) {
        assertThat(mainPage.getItemNames(), not(CoreMatchers.hasItem(itemName.trim())));
    }
}
