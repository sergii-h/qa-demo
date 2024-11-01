package step.validation;

import context.ItemTestContext;
import data.DemoEventMessage;
import io.qameta.allure.Step;
import page.ItemInfoForm;
import util.KafkaConsumer;

import static com.codeborne.selenide.Condition.text;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class ItemValidator {
    ItemInfoForm itemInfoForm = new ItemInfoForm();

    @Step("Validate item info")
    public void info(String name, String amount, String description) {
        itemInfoForm.locator.shouldHave(text(name + " Amount: " + amount + " € Description: " + description));
    }

    @Step("Validate item message is produced")
    public void produced(KafkaConsumer<DemoEventMessage> demoEventConsumer, ItemTestContext context) {
        await().untilAsserted(
                () -> assertThat(demoEventConsumer.getRecords(), hasItem(context.createExpectedEvent()))
        );
    }
}
