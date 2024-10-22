package com.example.demo.integration.test.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;

import com.example.demo.data.DemoEvent;
import com.example.demo.integration.TestBase;
import com.example.demo.context.ItemTestContext;
import com.example.demo.integration.service.KafkaConsumer;

import io.restassured.response.Response;

class DemoEventTest extends TestBase {
    private KafkaConsumer<DemoEvent> demoEventConsumer;

    @Value("${kafka.topic.demo-event}")
    private String topic;

    @Value("${kafka.bootstrap-servers}")
    private String servers;

    @BeforeEach
    void beforeEach () {
        demoEventConsumer = KafkaConsumer.<DemoEvent>builder()
                .config(Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers))
                .topic(topic)
                .messageClass(DemoEvent.class)
                .build();
    }

    @AfterEach
    void tearDown() {
        demoEventConsumer.shutdown();
    }

    private static Stream<Arguments> validPayload() {
        return Stream.of(
                of("First1 second", 1, "description"  ),
                of(" ",             1, "description"  ),
                of("",              1, "description"  ),
                of("name",         -1, "description"  ),
                of("name",          0, "description"  ),
                of("name",          1, "First1 second"),
                of("name",          1, " "            )
        );
    }
        
    @ParameterizedTest
    @MethodSource("validPayload")
    void shouldProduceEvent(String name, long amount, String description) {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .name(name)
                .amount(amount)
                .description(description)
                .build();

        // when
        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/items");
                
        context.setResponse(postResponse);

        // and
        demoEventConsumer.setFilter(context.getId());
        demoEventConsumer.start();

        // then
        await().untilAsserted(
                () -> assertThat(demoEventConsumer.getRecords(), equalTo(List.of(context.createExpectedEvent())))
        );
    }
}
