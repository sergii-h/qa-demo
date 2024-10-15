package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.context.ItemTestContext;
import com.example.demo.data.ItemResponse;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.of;

class CreateItemTest extends TestBase {
    private static Stream<Arguments> validPayload() {
        return Stream.of(
                of("First1 second", 1, "description"  ),
                of(" ",             1, "description"  ),
                of("",              1, "description"  ),
                of("name",         -1, "description"  ),
                of("name",          0, "description"  ),
                of("name",          1, "First1 second"),
                of("name",          1, " "            )
        );
    }

    @ParameterizedTest
    @MethodSource("validPayload")
    void shouldCreateItem(String name, long amount, String description) {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .name(name)
                .amount(amount)
                .description(description)
                .build();

        // when
        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/items");
                
        context.setResponse(postResponse);

        // and
        Response getResponse = requestSpec.get("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(postResponse.statusCode(), is(200)),
                () -> assertThat(postResponse.getBody().as(ItemResponse.class), is(context.createExpectedResponse())),
                () -> assertThat(getResponse.getBody().as(ItemResponse.class), is(context.createExpectedResponse()))
        );
    }

    @ParameterizedTest
    @ValueSource( strings = {
            "{\"name\": null,     \"amount\": 1,    \"description\": \"description\"}",
            "{\"name\": \"name\", \"amount\": 1,    \"description\": \"\"           }",
            "{                    \"amount\": 1,    \"description\": \"description\"}",
            "{\"name\": \"name\", \"amount\": 1                                     }",
            "{\"name\": \"name\", \"amount\": null, \"description\": \"description\"}",
            "{\"name\": \"name\",                   \"description\": \"description\"}",
    })
    void shouldRespondWithBadRequestWhenNotValidPayload(String notValidPayload) {
        // when
        Response response = requestSpec
                .body(notValidPayload)
                .post("/items");

        JsonPath body = response.jsonPath();

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(400)),
                () -> assertThat(body.get("status"), is(400)),
                () -> assertThat(body.get("error"), is("Bad Request")),
                () -> assertThat(body.get("path"), is("/v1/items"))
        );
    }

    @Test
    void shouldRespondWithRoundedAmountWhenDoubleValue() {
        // when
        Response response = requestSpec
                .body("{\"name\": \"name\", \"amount\": 0.5, \"description\": \"description\"}")
                .post("/items");

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(response.jsonPath().get("amount"), is(0))
        );
    }
}
