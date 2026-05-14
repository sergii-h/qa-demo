package com.example.demo.integration.test.api;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.Task;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.bson.types.ObjectId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public class RetrieveTaskTest extends ApiIntegrationTestBase {

    @Test
    void shouldReturnHttp200WithTaskDetailsWhenValidIdProvidedViaGet() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .title("Retrieve Test Task")
                .build();

        Response createResponse = requestSpec
                .body(context.createTaskRequest())
                .post("/tasks");

        context.updateFromResponse(createResponse);

        // when
        Response getResponse = requestSpec.get("/tasks/" + context.getId());

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(200)),
                () -> assertThat(getResponse.getBody().as(Task.class), is(context.createTask()))
        );
    }

    @Test
    void shouldReturnHttp404WhenTaskIdDoesNotExist() {
        // given
        String nonExistentId = String.valueOf(new ObjectId());

        // when
        Response response = requestSpec.get("/tasks/" + nonExistentId);

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(404)),
                () -> assertThat(response.jsonPath().get("message"), is("Task not found with id: " + nonExistentId))
        );
    }
}
