package com.example.demo.integration.test.event;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.TaskEvent;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import com.example.demo.integration.service.KafkaConsumer;
import io.restassured.response.Response;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class TaskUpdatedEventTest extends ApiIntegrationTestBase {
    private KafkaConsumer<TaskEvent> taskEventConsumer;

    @Value("${kafka.topic.task-event}")
    private String topic;

    @Value("${kafka.bootstrap-servers}")
    private String servers;

    @BeforeEach
    void beforeEach() {
        taskEventConsumer = KafkaConsumer.<TaskEvent>builder()
                .config(Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers))
                .topic(topic)
                .messageClass(TaskEvent.class)
                .build();
    }

    @AfterEach
    void tearDown() {
        taskEventConsumer.shutdown();
    }

    @Test
    void shouldPublishEventToKafkaTopicTaskEventWhenTaskUpdated() {
        // given
        TaskTestContext createContext = TaskTestContext.builder()
                .title("Original Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .build();

        Response createResponse = requestSpec
                .body(createContext.createTaskRequest())
                .post("/tasks");

        createContext.updateFromResponse(createResponse);

        TaskTestContext updateContext = TaskTestContext.builder()
                .id(createContext.getId())
                .title("Updated Task")
                .description("Updated Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .eventType("UPDATED")
                .build();

        // when
        requestSpec
                .body(updateContext.createTaskRequest())
                .put("/tasks/" + createContext.getId());

        // and
        taskEventConsumer.setFilter(createContext.getId());
        taskEventConsumer.start();

        // then
        await().untilAsserted(() -> {
            TaskEvent event = taskEventConsumer.getRecords().stream()
                    .filter(e -> "UPDATED".equals(e.getEventType()))
                    .findFirst()
                    .orElse(null);

            assertThat(event, notNullValue());
            updateContext.setEventTimestamp(event.getTimestamp());
            assertThat(event, is(updateContext.createExpectedEvent()));
        });
    }
}
