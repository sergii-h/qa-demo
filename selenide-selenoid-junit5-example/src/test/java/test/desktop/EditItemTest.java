package test.desktop;

import context.ItemContext;
import io.qameta.allure.Epic;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import parameterresolver.ActionsResolver;
import parameterresolver.ValidationsResolver;
import step.ActionManager;
import step.ValidationManager;
import test.TestBase;

import java.util.List;

@ExtendWith({
        ActionsResolver.class,
        ValidationsResolver.class,
        TestBase.Desktop.class
})
@RequiredArgsConstructor

@Epic("Edit item")
class EditItemTest {
    @NonNull private final ActionManager actions;
    @NonNull private final ValidationManager validate;

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
        validate.items.listSizeIsNotChanged(oldList.size());
        validate.item.info(changedContext.name, changedContext.amount, changedContext.description);
    }

    @Test()
    @DisplayName("Edit item with required 'description' field")
    void shouldEditItemWithRequiredFieldOnly() {
        // given
        ItemContext context = ItemContext.builder().build();

        actions.api.createItem(context.createItemRequest());

        List<String> oldList = actions.items.getItemNames();

        ItemContext changedContext = ItemContext.builder()
                .name("")
                .amount("")
                .description(context.description + "1")
                .build();

        //when
        actions.items
                .openItemEditForm(context.name)
                .setItemData(changedContext.createItemData())
                .submitForm()
                .openItemInfoForm(changedContext.name);

        // then
        validate.items.listSizeIsNotChanged(oldList.size());
        validate.item.info(changedContext.name, changedContext.amount, changedContext.description);
    }
}