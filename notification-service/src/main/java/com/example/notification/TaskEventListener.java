package com.example.notification;

import com.example.notification.data.TaskNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final TaskNotificationHandler handler;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.task-event}", groupId = "notification-service")
    public void onTaskEvent(String message) throws Exception {
        TaskNotification notification = objectMapper.readValue(message, TaskNotification.class);
        handler.handle(notification);
    }
}
