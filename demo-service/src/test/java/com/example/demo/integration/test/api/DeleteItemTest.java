package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.context.ItemTestContext;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class DeleteItemTest extends TestBase {
    @Test
    void shouldDeleteItem() {
        // given
        ItemTestContext context = ItemTestContext.builder().build();

        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/items");

        context.setResponse(postResponse);

        // when
        Response deleteResponse = requestSpec.delete("/items/" + context.getId());

        // and
        Response getResponse = requestSpec.get("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(deleteResponse.statusCode(), is(200)),
                () -> assertThat(deleteResponse.getBody().asString(), is(emptyString())),
                () -> assertThat(getResponse.statusCode(), is(404)),
                () -> assertThat(
                        getResponse.getBody().asString(),
                        is("No value present for request /v1/items/" + context.getId())
                )
        );
    }

    @Test
    void shouldDeleteNotExistingItem() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .id(randomNumeric(10))
                .build();

        // when
        Response deleteResponse = requestSpec.delete("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(deleteResponse.statusCode(), is(404)),
                () -> assertThat(
                        deleteResponse.getBody().asString(),
                        is("No value present for request /v1/items/" + context.getId())
                )
        );
    }
}
