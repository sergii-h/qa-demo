package context;

import data.*;
import io.restassured.response.Response;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.secure;

@Builder
@Data
public class TaskTestContext {
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Builder.Default
    private String title = secure().nextAlphabetic(12);

    @Builder.Default
    private String description = secure().nextAlphabetic(12);

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private Response response;

    public void setResponse(Response response) {
        this.response = response;
        this.id = response.jsonPath().get("id");
    }

    public TaskData createTaskData() {
        return TaskData.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .build();
    }

    public TaskResponse createTaskResponse() {
        return TaskResponse.builder()
                .id(id)
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .build();
    }
}
