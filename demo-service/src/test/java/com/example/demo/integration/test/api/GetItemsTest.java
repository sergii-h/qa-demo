package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.context.ItemTestContext;
import com.example.demo.data.ItemResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

class GetItemsTest extends TestBase {
    @Test
    void shouldGetItems() {
        // given
        ItemTestContext context1 = ItemTestContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        ItemTestContext context2 = ItemTestContext
                .builder()
                .name("name2")
                .amount(2)
                .description("description2")
                .build();

        context1.setResponse(requestSpec.body(context1.createItemRequest()).post("/items"));
        context2.setResponse(requestSpec.body(context2.createItemRequest()).post("/items"));

        // when
        Response getResponse = requestSpec.get("/items");

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(200)),
                () -> assertThat(
                        Arrays.stream(getResponse.getBody().as(ItemResponse[].class)).toList(),
                        hasItems(context1.createExpectedResponse(), context2.createExpectedResponse())
                )
        );
    }

    @Test
    void shouldGetItemsAfterUpdate() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .name("name")
                .build();

        ItemTestContext updatedContext = ItemTestContext
                .builder()
                .name("updatedName")
                .build();

        updatedContext.setResponse(requestSpec.body(context.createItemRequest()).post("/items"));

        // and
       requestSpec
               .body(updatedContext.createItemRequest())
               .put("/items/" + updatedContext.getId());

        // when
        Response getResponse = requestSpec.get("/items");

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(200)),
                () -> assertThat(
                        Arrays.stream(getResponse.getBody().as(ItemResponse[].class)).toList(),
                        hasItem(updatedContext.createExpectedResponse())
                )
        );
    }

    @Test
    void shouldGetItemsAfterDelete() {
        // given
        ItemTestContext context = ItemTestContext
                .builder()
                .build();

        context.setResponse(requestSpec.body(context.createItemRequest()).post("/items"));

        // and
        requestSpec.delete("/items/" + context.getId());

        // when
        Response getResponse = requestSpec.get("/items");

        // then
        assertAll(
                () -> assertThat(getResponse.statusCode(), is(200)),
                () -> assertThat(
                        Arrays.stream(getResponse.getBody().as(ItemResponse[].class)).toList(),
                        not(hasItem(context.createExpectedResponse()))
                )
        );
    }
}
