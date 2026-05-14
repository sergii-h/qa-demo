package step.action;


import data.Item;
import io.qameta.allure.Step;
import page.EditItemForm;

import static com.codeborne.selenide.Condition.visible;

public class EditItemAction {
    EditItemForm editItemForm = new EditItemForm();

    @Step("Set item data")
    public EditItemAction setItemData(Item itemData) {
        editItemForm.nameField.setValue(itemData.getName());
        editItemForm.amountField.setValue(itemData.getAmount());
        editItemForm.descriptionField.setValue(itemData.getDescription());

        return this;
    }

    @Step("Submit 'Edit item' form")
    public ItemsAction submitForm() {
        editItemForm.saveButton.click();
        editItemForm.saveButton.shouldNotBe(visible);

        return new ItemsAction();
    }

    @Step("Close 'Edit item' form")
    public ItemsAction closeForm() {
        editItemForm.closeButton.click();
        editItemForm.closeButton.shouldNotBe(visible);

        return new ItemsAction();
    }

    @Step("Close 'Edit item' form by X button")
    public ItemsAction closeFormByXButton() {
        editItemForm.closeXButton.click();
        editItemForm.closeXButton.shouldNotBe(visible);

        return new ItemsAction();
    }
}
