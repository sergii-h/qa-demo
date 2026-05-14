package com.example.demo.integration.test.client;

import com.example.demo.context.TaskTestContext;
import com.example.demo.integration.ApiIntegrationTestBase;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@EnableWireMock(
        @ConfigureWireMock(port = 8085)
)
public class ValidateItemTest extends ApiIntegrationTestBase {
    @BeforeEach
    void setupWiremock() {
        WireMock.stubFor(post("/external/validate/task")
                .withRequestBody(matchingJsonPath("$.description", equalTo("not valid")))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("false"))
        );

        WireMock.stubFor(post("/external/validate/task")
                .withRequestBody(matchingJsonPath("$.description", not(equalTo("not valid"))))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("true"))
        );
    }

    @Test
    void shouldReturnHttp200WithTrueWhenTaskExistsAndIsValidViaGet() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();

        context.updateFromResponse(
                requestSpec
                        .body(context.createTaskRequest())
                        .post("/tasks")
        );

        // when
        Response response = requestSpec.get("/tasks/isValid/" + context.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(response.getBody().as(String.class), is("true"))
        );
    }

    @Test
    void shouldReturnNotValidValidationResult() {
        // given
        TaskTestContext invalidContext = TaskTestContext.builder()
                .description("not valid")
                .build();

        invalidContext.updateFromResponse(
                requestSpec
                        .body(invalidContext.createTaskRequest())
                        .post("/tasks")
        );

        // when
        Response invalidResponse = requestSpec.get("/tasks/isValid/" + invalidContext.getId());

        // then
        assertThat(invalidResponse.getBody().as(String.class), is("false"));
    }

    @Test
    void shouldReturnHttp200WithFalseWhenTaskIdDoesNotExist() {
        // given
        TaskTestContext context = TaskTestContext.builder().build();

        // when
        Response response = requestSpec.get("/tasks/isValid/" + context.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(response.getBody().as(String.class), is("false"))
        );
    }

    @Test
    void shouldCallExternalValidationServiceWithTaskData() {
        // given
        TaskTestContext context = TaskTestContext.builder()
                .title("Test Title")
                .description("Test Description")
                .build();

        context.updateFromResponse(
                requestSpec
                        .body(context.createTaskRequest())
                        .post("/tasks")
        );

        // when
        requestSpec.get("/tasks/isValid/" + context.getId());

        // then
        WireMock.verify(postRequestedFor(urlEqualTo("/external/validate/task"))
                .withRequestBody(matchingJsonPath("$.title", equalTo("Test Title")))
                .withRequestBody(matchingJsonPath("$.description", equalTo("Test Description")))
        );
    }
}
