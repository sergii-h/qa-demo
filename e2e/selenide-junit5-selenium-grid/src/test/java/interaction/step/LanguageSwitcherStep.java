package interaction.step;

import interaction.page.LanguageSwitcherDropdown;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;

public class LanguageSwitcherStep {
    LanguageSwitcherDropdown languageSwitcherDropdown = new LanguageSwitcherDropdown();

    @Step("Select language '{language}'")
    public void selectLanguage(String language) {
        languageSwitcherDropdown.dropdown.click();
        languageSwitcherDropdown.items.findBy(text(language)).click();
        languageSwitcherDropdown.dropdownValue.shouldHave(text(language));
    }
}
