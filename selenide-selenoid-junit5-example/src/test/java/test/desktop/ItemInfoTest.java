package test.desktop;

import context.ItemTestContext;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void beforeEach() {
        actions.wiremock
                .clearMocks()
                .setIsValidMock(true);
    }

    @Tag("smoke")
    @Test()
    @DisplayName("Get Item info")
    void shouldGetItemInfo() {
        ItemTestContext context = ItemTestContext.builder().build();

        actions.api.createItem(context.createItemRequest());
        actions.items.openItemInfoForm(context.getName());

        validate.item.info(context.getName(), context.getAmount(), context.getDescription());
    }
}