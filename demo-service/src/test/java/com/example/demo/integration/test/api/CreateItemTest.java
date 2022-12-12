package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.integration.context.ItemContext;
import com.example.demo.integration.data.ItemResponse;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.of;

class CreateItemTest extends TestBase {
    private static Stream<Arguments> validPayload() {
        return Stream.of(
                of("name",          1, "description" ),
                of("Name1",         1, "description" ),
                of("first second",  1, "description" ),
                of("null",          1, "description" ),
                of(" ",             1, "description" ),
                of("",              1, "description" ),
                of("name",         -1, "description" ),
                of("name",          0, "description" ),
                of("name",          1, "Description1"),
                of("name",          1, "first second"),
                of("name",          1, "null"        ),
                of("name",          1, " "           )
        );
    }

    @ParameterizedTest
    @MethodSource("validPayload")
    void createItem(String name, long amount, String description) {
        // given
        ItemContext context = ItemContext
                .builder()
                .name(name)
                .amount(amount)
                .description(description)
                .build();

        // when
        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/item");
        context.setId(postResponse.jsonPath().get("id"));

        // and
        Response getResponse = requestSpec.get("/item/" + context.getId());

        // then
        assertThat(postResponse.statusCode(), is(200));
        assertThat(postResponse.getBody().as(ItemResponse.class), is(context.createExpectedResponse()));
        assertThat(getResponse.getBody().as(ItemResponse.class), is(context.createExpectedResponse()));
    }

    @ParameterizedTest
    @ValueSource( strings = {
            "{\"name\": null,     \"amount\": 1, \"description\": \"description\"}",
            "{\"name\": \"name\", \"amount\": 1, \"description\": \"\"}",
            "{                    \"amount\": 1, \"description\": \"description\"}",
            "{\"name\": \"name\", \"amount\": 1}",

    })
    void badRequestWhenNotValidPayload(String notValidPayload) {
        // when
        Response response = requestSpec
                .body(notValidPayload)
                .post("/item");

        JsonPath body = response.jsonPath();

        // then
        assertThat(response.statusCode(), is(400));
        assertThat(body.get("status"), is(400));
        assertThat(body.get("error"), is("Bad Request"));
        assertThat(body.get("path"), is("/v1/item"));
    }

    @ParameterizedTest
    @Tag("FailedTestExample")
    @ValueSource( strings = {
            "{\"name\": \"name\", \"amount\": null, \"description\": \"description\"}",
            "{\"name\": \"name\", \"description\": \"description\"}",
            "{\"name\": \"name\", \"amount\": 0.5, \"description\": \"description\"}",
    })
    void badRequestFailedTestExampleBecauseUnexpectedResponseCode(String notValidPayload) {
        // when
        Response response = requestSpec
                .body(notValidPayload)
                .post("/item");

        JsonPath body = response.jsonPath();

        // then
        assertThat(response.statusCode(), is(400));
        assertThat(body.get("status"), is(400));
        assertThat(body.get("error"), is("Bad Request"));
        assertThat(body.get("path"), is("/v1/item"));
    }
}
