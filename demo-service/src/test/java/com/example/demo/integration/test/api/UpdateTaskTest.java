package com.example.demo.integration.test.api;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import io.restassured.response.Response;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public class UpdateTaskTest extends ApiIntegrationTestBase {

    @Test
    void shouldSuccessfullyUpdateTaskWithValidDataViaPutAndReturnHttp200() {
        // given
        TaskTestContext createContext = TaskTestContext.builder()
                .title("Original Title")
                .description("Original Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .build();

        Response createResponse = requestSpec
                .body(createContext.createTaskRequest())
                .post("/tasks");

        createContext.updateFromResponse(createResponse);

        TaskTestContext updateContext = TaskTestContext.builder()
                .id(createContext.getId())
                .title("Updated Title")
                .description("Updated Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // when
        Response updateResponse = requestSpec
                .body(updateContext.createTaskRequest())
                .put("/tasks/" + updateContext.getId());

        updateContext.updateFromResponse(updateResponse);

        assertAll(
                () -> assertThat(updateResponse.statusCode(), is(200)),
                () -> assertThat(updateResponse.getBody().as(Task.class), is(updateContext.createTask())),
                () -> assertThat(updateContext.getUpdatedDate(), is(greaterThan(updateContext.getCreatedDate()))),
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + updateContext.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(updateContext.createTask()));
                }
        );
    }

    @Test
    void shouldReturnHttp404WhenTaskIdDoesNotExist() {
        // given
        TaskTestContext nonExistentContext = TaskTestContext.builder().build();

        // when
        Response response = requestSpec
                .body(nonExistentContext.createTaskRequest())
                .put("/tasks/" + nonExistentContext.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(404)),
                () -> assertThat(response.jsonPath().get("message"), is("Task not found with id: " + nonExistentContext.getId()))
        );
    }

    @Test
    void shouldReturnHttp409WhenNewTitleConflictsWithExistingTask() {
        // given
        String suffix = new ObjectId().toString();

        TaskTestContext firstTaskContext = TaskTestContext.builder()
                .title("First Task " + suffix)
                .build();

        Response firstCreateResponse = requestSpec
                .body(firstTaskContext.createTaskRequest())
                .post("/tasks");

        firstTaskContext.updateFromResponse(firstCreateResponse);

        TaskTestContext secondTaskContext = TaskTestContext.builder()
                .title("Second Task " + suffix)
                .build();

        Response createResponse = requestSpec
                .body(secondTaskContext.createTaskRequest())
                .post("/tasks");

        secondTaskContext.updateFromResponse(createResponse);
        secondTaskContext.setTitle("First Task " + suffix);

        // when
        Response response = requestSpec
                .body(secondTaskContext.createTaskRequest())
                .put("/tasks/" + secondTaskContext.getId());

        secondTaskContext.setTitle("Second Task " + suffix);

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(409)),
                () -> assertThat(response.jsonPath().get("message"), is("Task with title '" + firstTaskContext.getTitle() + "' already exists")),
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + firstTaskContext.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(firstTaskContext.createTask()));
                },
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + secondTaskContext.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(secondTaskContext.createTask()));
                }
        );
    }

    @Test
    void shouldAllowUpdateWhenTitleRemainsTheSame() {
        // given
        TaskTestContext createContext = TaskTestContext.builder()
                .title("Same Title")
                .description("Original Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .build();

        Response createResponse = requestSpec
                .body(createContext.createTaskRequest())
                .post("/tasks");

        createContext.updateFromResponse(createResponse);

        TaskTestContext updateContext = TaskTestContext.builder()
                .title(createContext.getTitle())
                .description("Updated Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        // when
        Response updateResponse = requestSpec.body(updateContext.createTaskRequest()).put("/tasks/" + createContext.getId());
        updateContext.updateFromResponse(updateResponse);

        // then
        assertAll(
                () -> assertThat(updateResponse.statusCode(), is(200)),
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + updateContext.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(updateContext.createTask()));
                }
        );
    }

    @Test
    void shouldReturnHttp400ForValidationErrors() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();

        Response createResponse = requestSpec
                .body(context.createTaskRequest())
                .post("/tasks");

        context.updateFromResponse(createResponse);

        String longTitle = "a".repeat(101);

        // when
        Response response = requestSpec
                .body("{\"title\": \"" + longTitle + "\", \"status\": \"TODO\", \"priority\": \"HIGH\"}")
                .put("/tasks/" + context.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(400)),
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + context.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(context.createTask()));
                }
        );
    }
}
