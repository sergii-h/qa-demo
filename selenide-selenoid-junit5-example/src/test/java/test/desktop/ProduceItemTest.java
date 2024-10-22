package test.desktop;

import context.ItemTestContext;
import data.DemoEventMessage;
import io.qameta.allure.Epic;
import io.restassured.response.Response;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.*;
import step.ActionManager;
import step.ValidationManager;
import test.DesktopTest;
import util.KafkaConsumer;

import java.util.Map;

@Epic("Produce item")
class ProduceItemTest extends DesktopTest {
    ActionManager actions = new ActionManager();
    ValidationManager validate = new ValidationManager();
    private KafkaConsumer<DemoEventMessage> demoEventConsumer;

    @BeforeEach
    void beforeEach () {
        demoEventConsumer = KafkaConsumer.<DemoEventMessage>builder()
                .config(Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094"))
                .topic("demo-event")
                .messageClass(DemoEventMessage.class)
                .build();

        demoEventConsumer.start();
    }

    @AfterEach
    void tearDown() {
        demoEventConsumer.shutdown();
    }

    @Tag("smoke")
    @Test
    @DisplayName("Produce item message")
    void shouldProduceItem() {
        //given
        ItemTestContext context = ItemTestContext.builder().build();

        // when
        Response response = actions.api.createItem(context.createItemRequest());
        context.setResponse(response);

        // then
        validate.item.produced(demoEventConsumer, context);
    }
}