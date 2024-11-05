package test.desktop;

import context.ItemTestContext;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

@Epic("Delete item")
class DeleteItemTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Tag("smoke")
    @Test()
    @DisplayName("Delete item")
    void shouldDeleteItem() {
        // given
        ItemTestContext context = ItemTestContext.builder().build();
        actions.api.createItem(context.createItemRequest());

        // when
        actions.items.deleteItem(context.getName());

        // then
        validate.items.hasNoItem(context.getName());
    }
}
