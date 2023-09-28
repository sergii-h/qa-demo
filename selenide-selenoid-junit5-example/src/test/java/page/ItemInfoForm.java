
package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class ItemInfoForm {
    public SelenideElement locator = $("#info-modal");
    public SelenideElement title = $("#info-modal_header");
    public SelenideElement amount = $("#info-modal_content p");
    public SelenideElement description = $("#info-modal_content p:last-child");
    public SelenideElement closeButton = $("#info-modal .p-dialog-header-close");
}