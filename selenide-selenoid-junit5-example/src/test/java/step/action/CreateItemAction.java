package step.action;


import data.Item;
import io.qameta.allure.Step;
import page.CreateItemForm;

import static com.codeborne.selenide.Condition.visible;

public class CreateItemAction {
    CreateItemForm createItemForm = new CreateItemForm();

    @Step("Set item data")
    public CreateItemAction setItemData(Item itemData) {
        createItemForm.nameField.setValue(itemData.getName());
        createItemForm.amountField.setValue(itemData.getAmount());
        createItemForm.descriptionField.setValue(itemData.getDescription());

        return this;
    }

    @Step("Submit 'Create item' form")
    public void submitForm() {
        createItemForm.createButton.click();
        createItemForm.createButton.shouldNotBe(visible);
    }
}
