package com.example.demo;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.TaskEvent;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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
}
