package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.context.ItemTestContext;
import com.example.demo.data.ItemResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.of;

class UpdateItemTest extends TestBase {
    private static Stream<Arguments> validPayload() {
        return Stream.of(
                of("name", 1, "description", "name2", 2, "description2"),
                of("name", 1, "description", "name" , 1, "description" ),
                of(""    , 1, " "          , "name" , 1, "description" ),
                of("name", 1, "description", ""     , 1, " "           )
        );
    }

    @ParameterizedTest
    @MethodSource("validPayload")
    void shouldUpdateItem(String name, long amount, String description,
                    String updatedName, long updatedAmount, String updatedDescription
    ) {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .name(name)
                .amount(amount)
                .description(description)
                .build();

        ItemTestContext updatedContext = ItemTestContext
                .builder()
                .name(updatedName)
                .amount(updatedAmount)
                .description(updatedDescription)
                .build();

        updatedContext.setResponse(requestSpec.body(context.createItemRequest()).post("/items"));

        // when
        Response putResponse = requestSpec
                .body(updatedContext.createItemRequest())
                .put("/items/" + updatedContext.getId());

        // and
        Response getResponse = requestSpec.get("/items/" + updatedContext.getId());

        // then
        assertAll(
                () -> assertThat(putResponse.statusCode(), is(200)),
                () -> assertThat(putResponse.getBody().asString(), is(emptyString())),
                () -> assertThat(
                        getResponse.getBody().as(ItemResponse.class),
                        is(updatedContext.createExpectedResponse())
                )
        );
    }

    @Test
    void shouldUpdateNotExistingItem() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .id(randomNumeric(10))
                .build();

        // when
        Response putResponse = requestSpec
                .body(context.createItemRequest())
                .put("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(putResponse.statusCode(), is(404)),
                () -> assertThat(
                        putResponse.getBody().asString(),
                        is("No value present for request /v1/items/" + context.getId()
                        )
                )
        );
    }
}
