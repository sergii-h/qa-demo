package step.action;


import io.qameta.allure.Step;
import page.ItemInfoForm;

import static com.codeborne.selenide.Condition.visible;

public class ItemInfoAction {
    ItemInfoForm itemInfoForm = new ItemInfoForm();

    @Step("Close 'Item info' form")
    public void closeForm() {
        itemInfoForm.closeButton.click();
        itemInfoForm.closeButton.shouldNotBe(visible);
    }
}
