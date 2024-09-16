package test.desktop;

import context.ItemContext;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

import java.util.List;

@Epic("Delete item")
class DeleteItemTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Tag("smoke")
    @Test()
    @DisplayName("Delete item")
    void shouldDeleteItem() {
        // given
        ItemContext context = ItemContext.builder().build();
        actions.api.createItem(context.createItemRequest());

        List<String> oldList = actions.items.getItemNames();

        // when
        actions.items.deleteItem(context.name);

        // then
        validate.items.itemDeleted(oldList, context.name);
    }
}