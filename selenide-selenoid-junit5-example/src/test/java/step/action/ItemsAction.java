package step.action;


import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import page.CreateItemForm;
import page.EditItemForm;
import page.ItemInfoForm;
import page.MainPage;
import util.SelenideUtil;

import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static org.awaitility.Awaitility.with;

public class ItemsAction {
    MainPage mainPage = new MainPage();
    CreateItemForm createItemForm = new CreateItemForm();
    ItemInfoForm itemInfoForm = new ItemInfoForm();
    EditItemForm itemEditForm = new EditItemForm();

    @Step("Open 'Create item' form")
    public CreateItemAction openCreateItemForm() {
        mainPage.createItemButton.click();
        createItemForm.createButton.shouldBe(visible);

        return new CreateItemAction();
    }

    @Step("Open 'Item info' form for '{itemName}' item")
    public void openItemInfoForm(String itemName) {
        waitForItem(itemName);

        mainPage.itemInfoButton(itemName).click();
        itemInfoForm.title.shouldBe(visible);
    }

    @Step("Open 'Item edit' form for '{itemName}' item")
    public EditItemAction openItemEditForm(String itemName) {
        waitForItem(itemName);

        mainPage.itemEditButton(itemName).click();
        itemEditForm.saveButton.shouldBe(visible);

        return new EditItemAction();
    }

    @Step("Delete '{itemName}' item")
    public void deleteItem(String itemName) {
        List<String> oldList = mainPage.getItemNames();

        mainPage.itemDeleteButton(itemName).click();
        Selenide.refresh();

        with().pollInSameThread().await().until(() -> mainPage.getItemNames().size() != oldList.size());
    }

    @Step("Get item list")
    public List<String> getItemNames() {
        return mainPage.getItemNames();
    }

    private void waitForItem(String itemName) {
        with().pollInSameThread()
        .await()
        .ignoreException(InternalError.class)
        .until(() -> {
            Selenide.refresh();
            return mainPage.itemInfoButton(itemName).isDisplayed();
        });
        
        SelenideUtil.waitForEquals(mainPage.itemInfoButton(itemName)::getLocation);
    }
}
