package com.example.demo;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.TaskEvent;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskEventProducerTest {

    @Mock
    private KafkaTemplate<String, TaskEvent> kafkaTemplate;

    @InjectMocks
    private TaskEventProducer taskEventProducer;

    TaskTestContext context;

    @BeforeEach
    void beforeEach() {
        ReflectionTestUtils.setField(taskEventProducer, "topic", "task-events");
        lenient().when(kafkaTemplate.send(anyString(), anyString(), any(TaskEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        context = TaskTestContext.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();
    }

    @Test
    void shouldProduceTaskCreatedEvent() {
        // given
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        // when
        taskEventProducer.produceTaskCreated(context.createTask());

        // then
        verify(kafkaTemplate).send(eq("task-events"), eq(context.getId()), eventCaptor.capture());
        context.setEventTimestamp(eventCaptor.getValue().getTimestamp());
        assertThat(eventCaptor.getValue(), is(context.createExpectedEvent()));
    }

    @Test
    void shouldProduceTaskUpdatedEvent() {
        // given
        TaskTestContext updateContext = TaskTestContext.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .eventType("UPDATED")
                .build();
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        // when
        taskEventProducer.produceTaskUpdated(updateContext.createTask());

        // then
        verify(kafkaTemplate).send(eq("task-events"), eq(updateContext.getId()), eventCaptor.capture());
        updateContext.setEventTimestamp(eventCaptor.getValue().getTimestamp());
        assertThat(eventCaptor.getValue(), is(updateContext.createExpectedEvent()));
    }

    @Test
    void shouldProduceTaskDeletedEvent() {
        // given
        TaskTestContext deleteContext = TaskTestContext.builder()
                .title("Deleted Task")
                .description("Deleted Description")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .eventType("DELETED")
                .build();
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);

        // when
        taskEventProducer.produceTaskDeleted(deleteContext.createTask());

        // then
        verify(kafkaTemplate).send(eq("task-events"), eq(deleteContext.getId()), eventCaptor.capture());
        deleteContext.setEventTimestamp(eventCaptor.getValue().getTimestamp());
        assertThat(eventCaptor.getValue(), is(deleteContext.createExpectedEvent()));
    }

    @Test
    void shouldLogErrorWhenKafkaSendFails() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(TaskEventProducer.class);
        ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        CompletableFuture<SendResult<String, TaskEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), any(TaskEvent.class)))
                .thenReturn(failedFuture);

        // when
        taskEventProducer.produceTaskCreated(context.createTask());

        // then
        await().untilAsserted(() -> assertThat(
                logAppender.list.stream().anyMatch(event ->
                        Level.ERROR.equals(event.getLevel())
                                && event.getFormattedMessage().contains("Failed to send CREATED event for task " + context.getId())
                                && event.getFormattedMessage().contains("Kafka unavailable")),
                is(true)));

        logger.detachAppender(logAppender);
    }
}
