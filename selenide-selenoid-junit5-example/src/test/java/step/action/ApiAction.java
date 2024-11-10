package step.action;


import config.PropertyReader;
import data.ItemRequest;
import data.ItemResponse;
import io.qameta.allure.Step;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

public class ApiAction {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String TEST_ENV = PROPERTIES_READER.getProperty("test.env");

    RequestSpecification requestSpec = given()
            .baseUri(PROPERTIES_READER.getProperty(TEST_ENV + ".test.be.url"))
            .contentType(ContentType.JSON)
            .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    @Step("Create item by api")
    public Response createItem(ItemRequest itemBody) {
        Response response = requestSpec.body(itemBody).post("/items");

        await().until(
                () -> Arrays.stream(requestSpec.get("/items").getBody().as(ItemResponse[].class)).toList()
                        .contains(response.getBody().as(ItemResponse.class))
        );

        return response;
    }
}
