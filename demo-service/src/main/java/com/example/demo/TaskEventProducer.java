package com.example.demo;

import com.example.demo.data.Task;
import com.example.demo.data.TaskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Component
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final String topic;
    private final Clock clock;

    public TaskEventProducer(KafkaTemplate<String, TaskEvent> kafkaTemplate,
                             @Value("${kafka.topic.task-event}") String topic,
                             Clock clock) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.clock = clock;
    }

    public void produceTaskCreated(Task task) {
        produceEvent(task, "CREATED");
    }

    public void produceTaskUpdated(Task task) {
        produceEvent(task, "UPDATED");
    }

    public void produceTaskDeleted(Task task) {
        produceEvent(task, "DELETED");
    }

    private void produceEvent(Task task, String eventType) {
        TaskEvent event = TaskEvent.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .timestamp(Instant.now(clock))
                .eventType(eventType)
                .build();

        log.info("Sending task event to {}: {}", topic, event);
        kafkaTemplate.send(topic, event.getTaskId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send {} event for task {}: {}", event.getEventType(), event.getTaskId(), ex.getMessage());
                    } else {
                        log.info("Sent {} event for task {}", event.getEventType(), event.getTaskId());
                    }
                });
    }
}
