package step.validation;

import io.qameta.allure.Step;
import page.MainPage;

import java.util.List;

import static com.codeborne.selenide.CollectionCondition.size;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ItemsValidator {
    MainPage mainPage = new MainPage();

    @Step("Validate item '{itemName}' created")
    public void itemCreated(List<String> oldList, String itemName) {
        mainPage.itemNames.shouldHave(size(oldList.size() + 1));

        List<String> newList = mainPage.getItemNames();

        assertThat(newList, hasItem(itemName.trim()));
    }

    @Step("Validate item '{itemName}' deleted")
    public void itemDeleted(List<String> oldList, String itemName) {
        mainPage.itemNames.shouldHave(size(oldList.size() - 1));

        List<String> newList = mainPage.getItemNames();

        oldList.removeAll(newList);

        assertThat(oldList.get(0), is(itemName.trim()));
    }

    @Step("Validate items list size is {expectedListSize}")
    public void listSizeIs(int expectedListSize) {
        mainPage.itemNames.shouldHave(size(expectedListSize));
    }
}
