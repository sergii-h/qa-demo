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

@ExtendWith({
        ActionsResolver.class,
        ValidationsResolver.class,
        TestBase.Desktop.class
})
@RequiredArgsConstructor

@Epic("Item info")
class ItemInfoTest {
    @NonNull private final ActionManager actions;
    @NonNull private final ValidationManager validate;

    @Tag("smoke")
    @Test()
    @DisplayName("Get Item info")
    void shouldGetItemInfo() {
        ItemContext context = ItemContext.builder().build();

        actions.api.createItem(context.createItemRequest());
        actions.items.openItemInfoForm(context.name);

        validate.item.info(context.name, context.amount, context.description);
    }

    @CsvSource({
            "1    , description1    ",
            "1    ,         null    ",
            "0    , description1    ",
            "-1   , description1    ",
            "1    , Desc1 Desc2     ",
    })
    @DisplayName("Item info with amount '{amount}' and description '{description}'")
    @ParameterizedTest(name="Item info \"{argumentsWithNames}\"")
    void shouldGetItemInfoWithDifferentData(String amount, String description) {
        ItemContext context = ItemContext.builder()
                .amount(amount)
                .description(description)
                .build();

        actions.items
                .createItem(context.createItemData())
                .openItemInfoForm(context.name);

        validate.item.info(context.name, context.amount, context.description);
    }

    @CsvSource({
            "''   , 0",
            "'01', 1",
    })
    @DisplayName("Item info with formatted amount for '{amount}'")
    @ParameterizedTest(name="Item info \"{argumentsWithNames}\" formatted amount")
    void shouldGetItemInfoWithFormattedAmount(String amount, String formattedAmount) {
        ItemContext context = ItemContext.builder()
                .amount(amount)
                .build();

        actions.items
                .createItem(context.createItemData())
                .openItemInfoForm(context.name);

        context.setAmount(formattedAmount);

        validate.item.info(context.name, context.amount, context.description);
    }

    @CsvSource({
            "' description1 ', description1",
            "' '             , ''          ",
    })
    @DisplayName("Item info with formatted description for '{description}'")
    @ParameterizedTest(name="Item info \"{argumentsWithNames}\" formatted description")
    void shouldGetItemInfoWithFormattedDescription(String description, String formattedDescription) {
        ItemContext context = ItemContext.builder()
                .description(description)
                .build();

        actions.items
                .createItem(context.createItemData())
                .openItemInfoForm(context.name);

        context.setDescription(formattedDescription);

        validate.item.info(context.name, context.amount, context.description);
    }

    @Test
    @DisplayName("Item info form can be closed by Close button")
    void shouldCloseItemInfoForm() {
        ItemContext context = ItemContext.builder().build();

        actions.api.createItem(context.createItemRequest());
        actions.items.openItemInfoForm(context.name).closeForm();

        validate.item.infoNotDisplayed();
    }
}