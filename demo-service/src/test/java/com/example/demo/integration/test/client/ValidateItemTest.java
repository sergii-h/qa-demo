package com.example.demo.integration.test.client;

import com.example.demo.context.ItemTestContext;
import com.example.demo.data.ItemResponse;
import com.example.demo.integration.TestBase;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.response.Response;
import org.apache.http.impl.conn.Wire;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.net.URI;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@EnableWireMock(
        @ConfigureWireMock(port = 8084)
)
class ValidateItemTest extends TestBase {
    @BeforeEach
    void setupWiremock() {
        WireMock.stubFor(post("/external/validate/item")
                .withRequestBody(matchingJsonPath("$.description", equalTo("not valid")))
                .willReturn(aResponse().withBody("false"))
        );

        WireMock.stubFor(post("/external/validate/item")
                .withRequestBody(matchingJsonPath("$.description", not(equalTo("not valid"))))
                .willReturn(aResponse().withBody("true"))
        );
    }

    @Test
    void shouldGetItemIsValid() {
        // given
        ItemTestContext context = ItemTestContext.builder().build();

        context.setResponse(
                requestSpec
                        .body(context.createItemRequest())
                        .post("/items")
        );

        // when
        Response response = requestSpec.get("/items/isValid/" + context.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(response.getBody().as(String.class), is("true"))
        );
    }

    @Test
    void shouldGetItemIsNotValid() {
        // given
        ItemTestContext context = ItemTestContext.builder()
                .description("not valid")
                .build();

        context.setResponse(
                requestSpec
                        .body(context.createItemRequest())
                        .post("/items")
        );

        // when
        Response response = requestSpec.get("/items/isValid/" + context.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(response.getBody().as(String.class), is("false"))
        );
    }

    @Test
    void shouldGetItemIsNotValidForNotExistingItem() {
        // when
        Response response = requestSpec.get("/items/isValid/" + UUID.randomUUID() + "-not-found");

        // then
        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(response.getBody().as(String.class), is("false"))
        );
    }
}
