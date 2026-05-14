package com.example.demo.integration.test.api;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import io.restassured.response.Response;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ListTasksTest extends ApiIntegrationTestBase {

    @Test
    void shouldReturnHttp200WithArrayContainingCreatedTasksViaGet() {
        // given
        String suffix = new ObjectId().toString();

        TaskTestContext firstTask = TaskTestContext.builder()
                .title("First Task " + suffix)
                .description("First Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();

        TaskTestContext secondTask = TaskTestContext.builder()
                .title("Second Task " + suffix)
                .description("Second Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .build();

        TaskTestContext thirdTask = TaskTestContext.builder()
                .title("Third Task " + suffix)
                .description("Third Description")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .build();

        Response createResponse1 = requestSpec.body(firstTask.createTaskRequest()).post("/tasks");
        firstTask.updateFromResponse(createResponse1);

        Response createResponse2 = requestSpec.body(secondTask.createTaskRequest()).post("/tasks");
        secondTask.updateFromResponse(createResponse2);

        Response createResponse3 = requestSpec.body(thirdTask.createTaskRequest()).post("/tasks");
        thirdTask.updateFromResponse(createResponse3);

        // when
        Response response = requestSpec.get("/tasks");

        // then
        List<Task> tasks = Arrays.asList(response.getBody().as(Task[].class));

        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(tasks, hasItems(
                        firstTask.createTask(),
                        secondTask.createTask(),
                        thirdTask.createTask()
                ))
        );
    }
}
