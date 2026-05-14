package com.example.demo.integration.test.api;

import com.example.demo.context.TaskTestContext;
import com.example.demo.data.TaskPriority;
import com.example.demo.data.TaskStatus;
import com.example.demo.integration.ApiIntegrationTestBase;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.bson.types.ObjectId;

public class DeleteTaskTest extends ApiIntegrationTestBase {

    @Test
    void shouldSuccessfullyDeleteTaskViaDeleteAndReturnHttp204() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .title("Task to Delete")
                .build();

        Response createResponse = requestSpec
                .body(context.createTaskRequest())
                .post("/tasks");

        context.updateFromResponse(createResponse);

        // when
        Response deleteResponse = requestSpec
                .delete("/tasks/" + context.getId());

        // then
        assertAll(
                () -> assertThat(deleteResponse.statusCode(), is(204)),
                () -> {
                    Response getResponse = requestSpec.get("/tasks/" + context.getId());
                    assertThat(getResponse.statusCode(), is(404));
                }
        );
    }

    @Test
    void shouldReturnHttp404WhenTaskIdDoesNotExist() {
        // given
        String nonExistentId = String.valueOf(new ObjectId());

        // when
        Response response = requestSpec.delete("/tasks/" + nonExistentId);

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(404)),
                () -> assertThat(response.jsonPath().get("message"), is("Task not found with id: " + nonExistentId))
        );
    }
}
