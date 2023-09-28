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
        ItemContext context = ItemContext.builder().build();

        actions.items
                .openCreateItemForm()
                .setItemData(context.createItemData())
                .submitForm();

        validate.items.itemCreated(oldList, context.name);
    }

    @CsvSource({
            "name1        , 1    , description1    ",
            "''           , 1    , description1    ",
            "''           , ''   , description1    ",
            "null         , 1    , description1    ",
            "name1        , 1    ,         null    ",
            "name1        , 0    , description1    ",
            "name1        , '0,1', description1    ",
            "name1        , -1   , description1    ",
            "' name1 '    , 1    , ' description1 '",
            "' '          , 1    , ' '             ",
            "'name1 name2', 1    , Desc1 Desc2     ",
    })
    @DisplayName("Create item with name {name}")
    @ParameterizedTest(name="Create item \"{argumentsWithNames}\"")
    void shouldCreateItemWithDifferentData(String name, String amount, String description) {
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

    @CsvSource({
            "name1, 1, ''",
    })
    @DisplayName("Item with name {name} not created")
    @ParameterizedTest(name="Item \"{argumentsWithNames}\" not created")
    void shouldNotCreateItemWhenWrongData(String name, String amount, String description) {
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

        validate.items.listIsNotChanged(oldList);
    }

    @Test
    @DisplayName("Item is not created when form is closed before submit")
    void shouldNotCreateItemWhenFormClosed() {
        List<String> oldList = actions.items.getItemNames();

        actions.items
                .openCreateItemForm()
                .setItemData(ItemContext.builder().build().createItemData())
                .closeForm();

        validate.items.listIsNotChanged(oldList);
    }

    @Test
    @DisplayName("Item is not created when form is closed by X button before submit")
    void shouldNotCreateItemWhenFormClosedByXButton() {
        List<String> oldList = actions.items.getItemNames();

        actions.items
                .openCreateItemForm()
                .setItemData(ItemContext.builder().build().createItemData())
                .closeFormByXButton();

        validate.items.listIsNotChanged(oldList);
    }
}