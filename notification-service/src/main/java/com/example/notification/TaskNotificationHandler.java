package com.example.notification;

import com.example.notification.data.TaskNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskNotificationHandler {

    public void handle(TaskNotification notification) {
        log.info("Handling task notification: taskId={}, status={}", notification.taskId(), notification.status());
    }
}
