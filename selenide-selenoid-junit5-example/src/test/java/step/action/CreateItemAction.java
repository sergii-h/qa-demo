package step.action;


import data.Item;
import io.qameta.allure.Step;
import page.CreateItemForm;

import static com.codeborne.selenide.Condition.visible;

public class CreateItemAction {
    CreateItemForm createItemForm = new CreateItemForm();

    @Step("Set item data")
    public CreateItemAction setItemData(Item itemData) {
        createItemForm.nameField.setValue(itemData.name);
        createItemForm.amountField.setValue(itemData.amount);
        createItemForm.descriptionField.setValue(itemData.description);

        return this;
    }

    @Step("Submit 'Create item' form")
    public void submitForm() {
        createItemForm.createButton.click();
        createItemForm.createButton.shouldNotBe(visible);
    }

    @Step("Close 'Create item' form")
    public void closeForm() {
        createItemForm.closeButton.click();
        createItemForm.closeButton.shouldNotBe(visible);
    }

    @Step("Close 'Create item' form by X button")
    public void closeFormByXButton() {
        createItemForm.closeXButton.click();
        createItemForm.closeXButton.shouldNotBe(visible);
    }
}
