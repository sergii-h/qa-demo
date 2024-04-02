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

@Epic("Delete item")
class DeleteItemTest {
    @NonNull private final ActionManager actions;
    @NonNull private final ValidationManager validate;

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