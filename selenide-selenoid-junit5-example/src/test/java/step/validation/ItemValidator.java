package step.validation;

import io.qameta.allure.Step;
import page.ItemInfoForm;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

public class ItemValidator {
    ItemInfoForm itemInfoForm = new ItemInfoForm();

    @Step("Validate item info")
    public void info(String name, String amount, String description) {
        itemInfoForm.locator.shouldHave(text(name + " Amount: " + amount + " € Description: " + description));
    }

    @Step("Validate item info form is not displayed")
    public void infoNotDisplayed() {
        itemInfoForm.title.shouldNotBe(visible);
    }
}
