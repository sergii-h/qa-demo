package data;

import lombok.Builder;
import lombok.Data;

import static org.apache.commons.lang3.RandomStringUtils.secure;

@Builder
@Data
public class TaskData {
    @Builder.Default
    private String title = secure().nextAlphabetic(12);

    @Builder.Default
    private String description = secure().nextAlphabetic(12);

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;
}
