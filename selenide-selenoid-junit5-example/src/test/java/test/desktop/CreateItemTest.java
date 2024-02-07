package test.desktop;

import context.ItemContext;
import io.qameta.allure.Epic;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

@Epic("Create item")
class CreateItemTest {
    @NonNull private final ActionManager actions;
    @NonNull private final ValidationManager validate;

    @Tag("smoke")
    @Test
    @DisplayName("Create item")
    void shouldCreateItem() {
        List<String> oldList = actions.items.getItemNames();
        ItemContext context = ItemContext.builder()
                .name("name1")
                .amount("1")
                .description("description1")
                .build();

        actions.items
                .openCreateItemForm()
                .setItemData(context.createItemData())
                .submitForm();

        validate.items.itemCreated(oldList, context.name);
    }

    @CsvSource({
            "''           , 1    , description1    ",
            "''           , ''   , description1    ",
    })
    @DisplayName("Create item with name {name}")
    @ParameterizedTest(name="Create item \"{argumentsWithNames}\"")
    void shouldCreateItemWithOptionalData(String name, String amount, String description) {
        List<String> oldList = actions.items.getItemNames();

        ItemContext context = ItemContext.builder()
                .name(name)
                .amount(amount)
                .description(description)
                .build();

        actions.items
                .openCreateItemForm()
                .setItemData(context.createItemData())
                .submitForm();

        validate.items.itemCreated(oldList, context.name);
    }

    @DisplayName("Item without description is not created")
    @Test()
    void shouldNotCreateItemWithoutDescription() {
        List<String> oldList = actions.items.getItemNames();

        ItemContext context = ItemContext.builder()
                .name("name")
                .amount("1")
                .description("")
                .build();

        actions.items
                .openCreateItemForm()
                .setItemData(context.createItemData())
                .submitForm();

        validate.items.listIsNotChanged(oldList);
    }
}