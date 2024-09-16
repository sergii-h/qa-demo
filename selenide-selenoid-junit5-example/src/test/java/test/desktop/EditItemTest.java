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

@Epic("Edit item")
class EditItemTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();

    @Tag("smoke")
    @Test()
    @DisplayName("Edit item")
    void shouldEditItem() {
        // given
        ItemContext context = ItemContext.builder().build();

        actions.api.createItem(context.createItemRequest());

        List<String> oldList = actions.items.getItemNames();

        ItemContext changedContext = ItemContext.builder()
                .name(context.name + "1")
                .amount(context.amount + "1")
                .description(context.description + "1")
                .build();

        //when
        actions.items
                .openItemEditForm(context.name)
                .setItemData(changedContext.createItemData())
                .submitForm()
                .openItemInfoForm(changedContext.name);

        // then
        validate.items.listSizeIs(oldList.size());
        validate.item.info(changedContext.name, changedContext.amount, changedContext.description);
    }
}