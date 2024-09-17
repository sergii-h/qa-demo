package com.example.demo.integration.test.api;

import com.example.demo.integration.TestBase;
import com.example.demo.integration.context.ItemContext;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

class DeleteItemTest extends TestBase {
    @Test
    void deleteItem() {
        // given
        ItemContext context = ItemContext.builder().build();

        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/item");

        context.setResponse(postResponse);

        // when
        Response deleteResponse = requestSpec.delete("/item/" + context.getId());

        // and
        Response getResponse = requestSpec.get("/item/" + context.getId());

        // then
        assertThat(deleteResponse.statusCode(), is(200));
        assertThat(deleteResponse.getBody().asString(), is(emptyString()));
        assertThat(getResponse.statusCode(), is(500));
        assertThat(getResponse.getBody().asString(), is("Item with id: " + context.getId() + " not found"));
    }

    @Test
    void deleteNotExistingItem() {
        // given
        ItemContext context = ItemContext
                .builder()
                .id(randomNumeric(10))
                .build();

        // when
        Response deleteResponse = requestSpec.delete("/item/" + context.getId());

        // then
        assertThat(deleteResponse.statusCode(), is(500));
        assertThat(deleteResponse.getBody().asString(), is("Item with id: " + context.getId() + " not found"));
    }
}
