package com.example.demo.integration.test.api;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CreateTaskTest extends ApiIntegrationTestBase {

    @Test
    void shouldSuccessfullyCreateTaskWithValidDataViaPostAndReturnHttp201() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();

        // when
        Response postResponse = requestSpec
                .body(context.createTaskRequest())
                .post("/tasks");

        context.updateFromResponse(postResponse);

        // then
        assertAll(
                () -> assertThat(postResponse.statusCode(), is(201)),
                () -> assertThat(postResponse.getBody().as(Task.class), is(context.createTask())),
                () -> assertThat(context.getCreatedDate(), is(context.getUpdatedDate())),
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + context.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(context.createTask()));
                }
        );
    }

    @Test
    void shouldReturnHttp409WhenDuplicateTitleExists() {
        // given
        TaskTestContext existingContext = TaskTestContext.builder()
                .title("Duplicate Title")
                .build();

        Response existingResponse = requestSpec
                .body(existingContext.createTaskRequest())
                .post("/tasks");

        existingContext.updateFromResponse(existingResponse);

        TaskTestContext duplicateContext = TaskTestContext.builder()
                .title("Duplicate Title")
                .description("Different description")
                .build();

        // when
        Response response = requestSpec
                .body(duplicateContext.createTaskRequest())
                .post("/tasks");

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(409)),
                () -> assertThat(response.jsonPath().get("message"), is("Task with title 'Duplicate Title' already exists")),
                () -> {
                        Response getResponse = requestSpec.get("/tasks/" + existingContext.getId());
                        assertThat(getResponse.getBody().as(Task.class), is(existingContext.createTask()));
                }
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("validationErrorScenarios")
    void shouldReturnHttp400ForValidationErrors(String field, String message, String requestBody) {
        // when
        Response response = requestSpec
                .body(requestBody)
                .post("/tasks");

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(400)),
                () -> assertThat(response.jsonPath().<String, String>getMap("$"), is(Map.of(field, message)))
        );
    }

    private static Stream<Arguments> validationErrorScenarios() {
        String longTitle = "a".repeat(101);
        String longDescription = "a".repeat(501);
        return Stream.of(
                Arguments.of("title", "Title is required",
                        "{\"title\": \"\", \"description\": \"desc\", \"status\": \"TODO\", \"priority\": \"HIGH\"}"),
                Arguments.of("title", "Title must not exceed 100 characters",
                        "{\"title\": \"" + longTitle + "\", \"description\": \"desc\", \"status\": \"TODO\", \"priority\": \"HIGH\"}"),
                Arguments.of("description", "Description must not exceed 500 characters",
                        "{\"title\": \"Test\", \"description\": \"" + longDescription + "\", \"status\": \"TODO\", \"priority\": \"HIGH\"}"),
                Arguments.of("status", "Status is required",
                        "{\"title\": \"Test\", \"description\": \"desc\", \"priority\": \"HIGH\"}"),
                Arguments.of("priority", "Priority is required",
                        "{\"title\": \"Test\", \"description\": \"desc\", \"status\": \"TODO\"}")
        );
    }

    @Test
    void shouldReturnHttp400WithEveryInvalidFieldWhenBodyViolatesAllRules() {
        // given
        String longDescription = "a".repeat(501);

        // when
        Response response = requestSpec
                .body("{\"title\": \"\", \"description\": \"" + longDescription + "\"}")
                .post("/tasks");

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(400)),
                () -> assertThat(response.jsonPath().<String, String>getMap("$"), is(Map.of(
                        "title", "Title is required",
                        "description", "Description must not exceed 500 characters",
                        "status", "Status is required",
                        "priority", "Priority is required"
                )))
        );
    }
}
