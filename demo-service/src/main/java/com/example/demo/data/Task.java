package com.example.demo.data;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("tasks")
@Value
@Builder(toBuilder = true)
public class Task {
    @Id
    String id;
    String title;
    String description;
    TaskStatus status;
    TaskPriority priority;
    Instant createdDate;
    Instant updatedDate;
}

