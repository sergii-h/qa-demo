package com.example.demo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskEvent;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated
@ExtendWith(MockitoExtension.class)
class TaskEventProducerTest {
    private static final Instant NOW = Instant.parse("2024-06-15T12:30:45.123Z");
    private static final String TOPIC = "task-events";

    @Mock
    private KafkaTemplate<String, TaskEvent> kafkaTemplate;

    private TaskEventProducer taskEventProducer;

    private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();

    TaskTestContext context;

    @BeforeEach
    void beforeEach() {
        taskEventProducer = new TaskEventProducer(kafkaTemplate, TOPIC, Clock.fixed(NOW, ZoneOffset.UTC));

        context = TaskTestContext.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .eventTimestamp(NOW)
                .build();
    }

    @AfterEach
    void afterEach() {
        ((Logger) LoggerFactory.getLogger(TaskEventProducer.class)).detachAppender(logAppender);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("eventTypes")
    void shouldPublishEventKeyedByTaskIdWhenTaskChanges(String eventType, BiConsumer<TaskEventProducer, Task> produce) {
        // given
        context.setEventType(eventType);
        when(kafkaTemplate.send(anyString(), anyString(), any(TaskEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        // when
        produce.accept(taskEventProducer, context.createTask());

        // then
        verify(kafkaTemplate).send(eq(TOPIC), eq(context.getId()), eventCaptor.capture());
        assertThat(eventCaptor.getValue(), is(context.createExpectedEvent()));
    }

    private static Stream<Arguments> eventTypes() {
        return Stream.of(
                Arguments.of("CREATED", (BiConsumer<TaskEventProducer, Task>) TaskEventProducer::produceTaskCreated),
                Arguments.of("UPDATED", (BiConsumer<TaskEventProducer, Task>) TaskEventProducer::produceTaskUpdated),
                Arguments.of("DELETED", (BiConsumer<TaskEventProducer, Task>) TaskEventProducer::produceTaskDeleted)
        );
    }

    @Test
    void shouldLogErrorWhenKafkaSendFails() {
        // given
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(TaskEventProducer.class)).addAppender(logAppender);

        CompletableFuture<SendResult<String, TaskEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), any(TaskEvent.class))).thenReturn(failedFuture);

        // when
        taskEventProducer.produceTaskCreated(context.createTask());

        // then
        await().untilAsserted(() -> assertThat(
                logAppender.list.stream().anyMatch(event ->
                        Level.ERROR.equals(event.getLevel())
                                && event.getFormattedMessage().contains("Failed to send CREATED event for task " + context.getId())
                                && event.getFormattedMessage().contains("Kafka unavailable")),
                is(true)));
    }
}
