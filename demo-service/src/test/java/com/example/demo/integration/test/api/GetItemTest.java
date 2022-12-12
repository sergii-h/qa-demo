package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.integration.context.ItemContext;
import com.example.demo.integration.data.ItemResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GetItemTest extends TestBase {
    @Test
    void getCreatedItem() {
        // given
        ItemContext context = ItemContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/item");
        context.setId(postResponse.jsonPath().get("id"));

        // when
        Response getResponse = requestSpec.get("/item/" + context.getId());

        // then
        assertThat(getResponse.statusCode(), is(200));
        assertThat(getResponse.getBody().as(ItemResponse.class), is(context.createExpectedResponse()));
    }

    @Test
    @Tag("FailedTestExample")
    void getUpdatedItemFailedTestExampleBecauseAmountIsNotUpdated() {
        // given
        ItemContext context = ItemContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        ItemContext updatedContext = ItemContext
                .builder()
                .name("updatedName")
                .amount(2)
                .description("updatedDescription")
                .build();

        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/item");

        updatedContext.setId(postResponse.jsonPath().get("id"));

        // when
        Response getResponse = requestSpec.get("/item/" + updatedContext.getId());

        // then
        assertThat(getResponse.statusCode(), is(200));
        assertThat(getResponse.getBody().as(ItemResponse.class), is(updatedContext.createExpectedResponse()));
    }

    @Test
    void getDeletedItem() {
        // given
        ItemContext context = ItemContext
                .builder()
                .name("name")
                .amount(1)
                .description("description")
                .build();

        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/item");

        context.setId(postResponse.jsonPath().get("id"));

        // and
        requestSpec.delete("/item/" + context.getId());

        // when
        Response getResponse = requestSpec.get("/item/" + context.getId());

        // then
        assertThat(getResponse.statusCode(), is(500));
        assertThat(getResponse.getBody().asString(), is("Item with id: " + context.getId() + " not found"));
    }

    @Test
    void getNotExistingItem() {
        // given
        ItemContext context = ItemContext
                .builder()
                .id(randomNumeric(10))
                .build();

        // when
        Response response = requestSpec.get("/item/" + context.getId());

        // then
        assertThat(response.statusCode(), is(500));
        assertThat(response.getBody().asString(), is("Item with id: " + context.getId() + " not found"));
    }
}
