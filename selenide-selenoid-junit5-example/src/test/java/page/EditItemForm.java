
package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class EditItemForm {
    public SelenideElement saveButton = $("#edit-modal .save-button");
    public SelenideElement closeButton = $("#edit-modal .close-button");
    public SelenideElement closeXButton = $("#edit-modal .p-dialog-header-close");
    public SelenideElement nameField = $("#name");
    public SelenideElement amountField = $("#amount");
    public SelenideElement descriptionField = $("#description");
}