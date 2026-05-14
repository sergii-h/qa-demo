package page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import util.SelenideUtil;

import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class MainPage {
    public SelenideElement formLocator = $("#root");
    public SelenideElement itemsLocator = $(".p-datatable-table");
    public SelenideElement createItemButton = $(".add-item-button");
    public ElementsCollection itemNames = $$(".p-datatable-tbody > tr:not(.p-datatable-emptymessage) > td:first-child");

    public SelenideElement itemInfoButton(String itemName) {
        return getItemElement(itemName, ".item-inf0-button");
    }

    public SelenideElement itemEditButton(String itemName) {
        return getItemElement(itemName, ".edit-item-button");
    }

    public SelenideElement itemDeleteButton(String itemName) {
        return getItemElement(itemName, ".delete-item-button");
    }

    public List<String> getItemNames() {
        Selenide.refresh();
        formLocator.shouldBe(visible);

        SelenideUtil.waitForEquals(itemsLocator::getLocation);
        return itemNames.texts();
    }

    private SelenideElement getItemElement(String itemName, String buttonLocator) {
        return $$(".p-datatable-tbody > tr:not(.p-datatable-emptymessage)")
                .asFixedIterable()
                .stream()
                .filter(item -> item.$("td").getText().equals(itemName))
                .map(item -> item.$("td " + buttonLocator))
                .findFirst()
                .orElseThrow(() -> new InternalError("Element '" + itemName + "' not found"));
    }
}
