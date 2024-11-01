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

@Epic("Edit item")
class EditItemTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Tag("smoke")
    @Test()
    @DisplayName("Edit item")
    void shouldEditItem() {
        // given
        ItemTestContext context = ItemTestContext.builder().build();

        actions.api.createItem(context.createItemRequest());

        List<String> oldList = actions.items.getItemNames();

        ItemTestContext changedContext = ItemTestContext.builder()
                .name(context.getName() + "1")
                .amount(context.getAmount() + "1")
                .description(context.getDescription() + "1")
                .build();

        //when
        actions.items
                .openItemEditForm(context.getName())
                .setItemData(changedContext.createItemData())
                .submitForm()
                .openItemInfoForm(changedContext.getName());

        // then
        validate.items.listSizeIs(oldList.size());
        validate.item.info(changedContext.getName(), changedContext.getAmount(), changedContext.getDescription());
    }
}