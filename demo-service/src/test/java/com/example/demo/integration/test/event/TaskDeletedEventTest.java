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

public class TaskDeletedEventTest extends ApiIntegrationTestBase {
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
    void shouldPublishEventToKafkaTopicTaskEventWhenTaskDeleted() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .title("Task to Delete")
                .description("Details before deletion")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .eventType("DELETED")
                .build();

        Response createResponse = requestSpec
                .body(context.createTaskRequest())
                .post("/tasks");

        context.updateFromResponse(createResponse);

        // when
        requestSpec.delete("/tasks/" + context.getId());

        // and
        taskEventConsumer.setFilter(context.getId());
        taskEventConsumer.start();

        // then
        await().untilAsserted(() -> {
            TaskEvent event = taskEventConsumer.getRecords().stream()
                    .filter(e -> "DELETED".equals(e.getEventType()))
                    .findFirst()
                    .orElse(null);

            assertThat(event, notNullValue());
            context.setEventTimestamp(event.getTimestamp());
            assertThat(event, is(context.createExpectedEvent()));
        });
    }
}
