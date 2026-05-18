package step.action;

import io.qameta.allure.Step;
import page.LanguageSwitcherDropdown;

import static com.codeborne.selenide.Condition.text;

public class LanguageSwitcherAction {
    LanguageSwitcherDropdown languageSwitcherDropdown = new LanguageSwitcherDropdown();

    @Step("Select language '{language}'")
    public void selectLanguage(String language) {
        languageSwitcherDropdown.dropdown.click();
        languageSwitcherDropdown.items.findBy(text(language)).click();
        languageSwitcherDropdown.dropdownValue.shouldHave(text(language));
    }
}
