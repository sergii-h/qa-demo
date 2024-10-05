package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.integration.context.ItemContext;
import com.example.demo.integration.data.ItemResponse;
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
    void getItems() {
        // given
        ItemContext context1 = ItemContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        ItemContext context2 = ItemContext
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
    void getItemsAfterUpdate() {
        // given
        ItemContext context = ItemContext
                .builder()
                .name("name")
                .build();

        ItemContext updatedContext = ItemContext
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
    void getItemsAfterDelete() {
        // given
        ItemContext context = ItemContext
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
