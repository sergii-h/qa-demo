
package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class CreateItemForm {
    public SelenideElement createButton = $("#create-modal .create-button");
    public SelenideElement closeButton = $("#create-modal .close-button");
    public SelenideElement closeXButton = $("#create-modal .p-dialog-header-close");
    public SelenideElement nameField = $("#name");
    public SelenideElement amountField = $("#amount");
    public SelenideElement descriptionField = $("#description");
}