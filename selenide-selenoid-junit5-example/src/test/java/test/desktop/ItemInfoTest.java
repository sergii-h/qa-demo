package test.desktop;

import context.ItemContext;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

@Epic("Item info")
class ItemInfoTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Tag("smoke")
    @Test()
    @DisplayName("Get Item info")
    void shouldGetItemInfo() {
        ItemContext context = ItemContext.builder().build();

        actions.api.createItem(context.createItemRequest());
        actions.items.openItemInfoForm(context.name);

        validate.item.info(context.name, context.amount, context.description);
    }
}