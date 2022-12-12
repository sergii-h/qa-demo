package com.example.demo.integration.test.api;

import com.example.demo.integration.context.ItemContext;
import com.example.demo.integration.data.ItemResponse;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("specificProfile")
class GetItemsTest {
    @LocalServerPort
    int port;

    RequestSpecification requestSpec;

    @BeforeEach
    public void baseBeforeEach() {
        requestSpec = given()
                .baseUri("http://localhost:" + port + "/v1")
                .contentType(ContentType.JSON)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        Arrays.stream(requestSpec.get("/item").getBody().as(ItemResponse[].class))
                .forEach(item -> requestSpec.delete("/item/" + item.getId()));
    }

    @Test
    void getEmptyItems() {
        // when
        Response getResponse = requestSpec.get("/item");

        // then
        assertThat(getResponse.statusCode(), is(200));
        assertThat(getResponse.getBody().as(ItemResponse[].class), is(emptyArray()));
    }

    @Test
    @Tag("FailedTestExample")
    void getItemsFailedTestExampleBecauseDescriptionIsNotReturned() {
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

        context1.setId(requestSpec.body(context1.createItemRequest()).post("/item").jsonPath().get("id"));
        context2.setId(requestSpec.body(context2.createItemRequest()).post("/item").jsonPath().get("id"));

        // when
        Response getResponse = requestSpec.get("/item");

        // then
        assertThat(getResponse.statusCode(), is(200));
        assertThat(getResponse.getBody().as(ItemResponse[].class), is(createExpectedResponses(context1, context2)));
    }

    @Test
    @Tag("FailedTestExample")
    void getItemsAfterUpdateFailedTestExampleBecauseDescriptionIsNotReturned() {
        // given
        ItemContext context = ItemContext
                .builder()
                .name("name")
                .build();

        ItemContext updatedContext = ItemContext
                .builder()
                .name("updatedName")
                .build();

        Response postResponse = requestSpec
                .body(context.createItemRequest())
                .post("/item");

        updatedContext.setId(postResponse.jsonPath().get("id"));

        // and
       requestSpec
               .body(updatedContext.createItemRequest())
               .put("/item/" + updatedContext.getId());

        // when
        Response getResponse = requestSpec.get("/item");

        // then
        assertThat(getResponse.statusCode(), is(200));
        assertThat(getResponse.getBody().as(ItemResponse[].class), is(createExpectedResponses(updatedContext)));
    }

    @Test
    @Tag("FailedTestExample")
    void getItemsAfterDeleteFailedTestExampleBecauseItemStillReturned() {
        // given
        ItemContext context = ItemContext
                .builder()
                .build();

        requestSpec.body(context.createItemRequest()).post("/item");

        // and
        requestSpec.delete("/item/" + context.getId());

        // when
        Response getResponse = requestSpec.get("/item");

        // then
        assertThat(getResponse.statusCode(), is(200));
        assertThat(getResponse.getBody().as(ItemResponse[].class), is(emptyArray()));
    }

    private ItemResponse[] createExpectedResponses(ItemContext... contexts) {
        return Arrays.stream(contexts).map(ItemContext::createExpectedResponse).toArray(ItemResponse[]::new);
    }
}
