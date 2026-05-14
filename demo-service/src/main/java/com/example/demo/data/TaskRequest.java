package com.example.demo.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    String title;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description;
    
    @NotNull(message = "Status is required")
    TaskStatus status;
    
    @NotNull(message = "Priority is required")
    TaskPriority priority;
}

