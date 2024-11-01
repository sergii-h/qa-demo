package test.desktop;

import context.ItemTestContext;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;

import java.util.List;

@Epic("Create item")
class CreateItemTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Tag("smoke")
    @Test
    @DisplayName("Create item")
    void shouldCreateItem() {
        List<String> oldList = actions.items.getItemNames();
        ItemTestContext context = ItemTestContext.builder()
                .name("name1")
                .amount("1")
                .description("description1")
                .build();

        actions.items
                .openCreateItemForm()
                .setItemData(context.createItemData())
                .submitForm();

        validate.items.itemCreated(oldList, context.getName());
    }

    @DisplayName("Create item with required 'description' field")
    @Test
    void shouldCreateItemWithRequiredFieldOnly() {
        List<String> oldList = actions.items.getItemNames();
        
        ItemTestContext context = ItemTestContext.builder()
                .name("")
                .amount("")
                .description("description")
                .build();

        actions.items
                .openCreateItemForm()
                .setItemData(context.createItemData())
                .submitForm();

        validate.items.itemCreated(oldList, context.getName());
    }
}