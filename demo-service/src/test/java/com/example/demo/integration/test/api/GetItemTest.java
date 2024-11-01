package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.context.ItemTestContext;
import com.example.demo.data.ItemResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class GetItemTest extends TestBase {
    @Test
    void shouldGetCreatedItem() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        context.setResponse(requestSpec.body(context.createItemRequest()).post("/items"));

        // when
        Response getResponse = requestSpec.get("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(200)),
                () -> assertThat(getResponse.getBody().as(ItemResponse.class), is(context.createExpectedResponse()))
        );
    }

    @Test
    void shouldGetUpdatedItem() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        ItemTestContext updatedContext = ItemTestContext
                .builder()
                .name("updatedName")
                .amount(2)
                .description("updatedDescription")
                .build();

        updatedContext.setResponse(requestSpec.body(context.createItemRequest()).post("/items"));

        // and
        requestSpec
                .body(updatedContext.createItemRequest())
                .put("/items/" + updatedContext.getId());

        // when
        Response getResponse = requestSpec.get("/items/" + updatedContext.getId());

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(200)),
                () -> assertThat(
                        getResponse.getBody().as(ItemResponse.class),
                        is(updatedContext.createExpectedResponse())
                )
        );
    }

    @Test
    void shouldGetDeletedItem() {
        // given
        ItemTestContext context = ItemTestContext.builder().build();

        context.setResponse(requestSpec.body(context.createItemRequest()).post("/items"));
        
        // and
        requestSpec.delete("/items/" + context.getId());

        // when
        Response getResponse = requestSpec.get("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(404)),
                () -> assertThat(
                        getResponse.getBody().asString(),
                        is("No value present for request /v1/items/" + context.getId())
                )
        );
    }

    @Test
    void shouldGetNotExistingItem() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .id(randomNumeric(10))
                .build();

        // when
        Response response = requestSpec.get("/items/" + context.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(404)),
                () -> assertThat(
                        response.getBody().asString(),
                        is("No value present for request /v1/items/" + context.getId())
                )
        );
    }
}
