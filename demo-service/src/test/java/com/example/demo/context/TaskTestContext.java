package com.example.demo.context;

import com.example.demo.data.Task;
import com.example.demo.data.TaskEvent;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskRequest;
import com.example.demo.data.TaskStatus;
import io.restassured.response.Response;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.Instant;

/**
 * Test Context Pattern for Task entity validation.
 *
 * <h2>Purpose:</h2>
 * This class solves the problem of validating complex DTOs (with many fields) in tests
 * without writing repetitive field-by-field assertions.
 *
 * <h2>Key Benefits:</h2>
 * <ul>
 *   <li><b>Complete Object Validation:</b> Validates ALL fields, not just a subset</li>
 *   <li><b>Minimal Test Code:</b> Only mutate dynamic fields (ID, timestamps), validate entire object</li>
 *   <li><b>Safety Net:</b> If a new field is added to Task, tests fail until properly handled</li>
 *   <li><b>Scales:</b> Works equally well for 5 fields or 100+ fields</li>
 * </ul>
 *
 * <h2>Usage Pattern - Mutation Approach:</h2>
 * <pre>
 * // 1. Setup test data in @BeforeEach
 * context = TaskTestContext.builder()
 *     .title("Test Task")
 *     .description("Test Description")
 *     .status(TaskStatus.TODO)
 *     .priority(TaskPriority.HIGH)
 *     .build();
 *
 * // 2. In test - capture actual result
 * ArgumentCaptor&lt;Task&gt; taskCaptor = ArgumentCaptor.forClass(Task.class);
 * taskController.createTask(context.createTaskRequest());
 * Task capturedTask = taskCaptor.getValue();
 *
 * // 3. Update context with dynamic fields (intentional mutation)
 * context.setId(capturedTask.getId());
 * context.setCreatedDate(capturedTask.getCreatedDate());
 * context.setUpdatedDate(capturedTask.getUpdatedDate());
 *
 * // 4. Validate complete object - ALL fields validated in one assertion
 * assertThat(capturedTask, is(context.createTask()));
 * </pre>
 *
 * <h2>Why Mutation is Intentional:</h2>
 * <p>
 * The mutation approach (setting dynamic fields on context) is a deliberate design choice that
 * prioritizes complete object validation over immutability. Alternative approaches:
 * </p>
 * <ul>
 *   <li><b>Field-by-field assertions:</b> Error-prone, can miss fields, doesn't scale</li>
 *   <li><b>Creating new context with all fields:</b> Verbose, 100+ lines for complex DTOs</li>
 *   <li><b>Ignoring fields:</b> Loses safety net of complete validation</li>
 * </ul>
 *
 * <h2>Integration Tests:</h2>
 * <p>
 * Use {@code updateFromResponse(response)} after each API call to sync the context with
 * server-generated fields (id, createdDate, updatedDate) before building assertions.
 * </p>
 *
 * @see com.example.demo.TaskControllerTest for usage examples
 */
@Builder
@Data
public class TaskTestContext {
    @Builder.Default
    private String id = String.valueOf(new ObjectId());
    
    @Builder.Default
    private String title = "Test Task " + new ObjectId();
    
    @Builder.Default
    private String description = "Test Description";
    
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;
    
    @Builder.Default
    private TaskPriority priority = TaskPriority.HIGH;

    @Builder.Default
    private Instant createdDate = Instant.now();

    @Builder.Default
    private Instant updatedDate = Instant.now();

    private Instant eventTimestamp;

    @Builder.Default
    private String eventType = "CREATED";

    public TaskRequest createTaskRequest() {
        return TaskRequest.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .build();
    }

    public Task createTask() {
        return Task.builder()
                .id(id)
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .createdDate(createdDate)
                .updatedDate(updatedDate)
                .build();
    }

    public TaskEvent createExpectedEvent() {
        return TaskEvent.builder()
                .taskId(id)
                .title(title)
                .status(status)
                .priority(priority)
                .timestamp(eventTimestamp)
                .eventType(eventType)
                .build();
    }

    public void updateFromResponse(Response response) {
        this.id = response.jsonPath().get("id");
        String createdDateStr = response.jsonPath().get("createdDate");
        if (createdDateStr != null) this.createdDate = Instant.parse(createdDateStr);
        String updatedDateStr = response.jsonPath().get("updatedDate");
        if (updatedDateStr != null) this.updatedDate = Instant.parse(updatedDateStr);
    }
}

