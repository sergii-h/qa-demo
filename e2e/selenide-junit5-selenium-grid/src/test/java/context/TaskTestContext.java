package context;

import data.TaskData;
import data.TaskPriority;
import data.TaskRequest;
import data.TaskStatus;
import io.restassured.response.Response;
import lombok.Builder;
import lombok.Data;

import static org.apache.commons.lang3.RandomStringUtils.secure;

@Builder
@Data
public class TaskTestContext {
    @Builder.Default
    private String title = secure().nextAlphabetic(12);

    @Builder.Default
    private String description = secure().nextAlphabetic(12);

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private String id;
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

    public TaskRequest createTaskRequest() {
        return TaskRequest.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .build();
    }
}
