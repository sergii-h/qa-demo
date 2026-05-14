package com.example.demo.data;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TaskEvent {
    String taskId;
    String title;
    TaskStatus status;
    TaskPriority priority;
    Instant timestamp;
    String eventType; // CREATED, UPDATED, DELETED
}

