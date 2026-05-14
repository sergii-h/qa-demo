package step.action;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.PropertyReader;
import io.qameta.allure.Step;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class WireMockAction {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String TEST_ENV = PROPERTIES_READER.getProperty("test.env");

    RequestSpecification requestSpec = given()
            .baseUri(PROPERTIES_READER.getProperty(TEST_ENV + ".test.wiremock.url"))
            .contentType(ContentType.JSON)
            .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    @Step("Set isValid mock to {isValid}")
    public void setIsValidMock(boolean isValid) {
        setMapping(
                Map.of(
                        "request", Map.of(
                                "method", "POST",
                                "url", "/external/validate/item"
                        ),
                        "response", Map.of(
                                "status", 200,
                                "body", objectToJsonString(isValid)
                        )
                )
        );
    }

    public WireMockAction clearMocks() {
        requestSpec
                .delete("/__admin/mappings")
                .then()
                .statusCode(200);

        return this;
    }

    private void setMapping(Map<String, Object> mappingBody) {
        requestSpec
                .body(mappingBody)
                .contentType(ContentType.JSON)
                .post("/__admin/mappings")
                .then()
                .statusCode(201);
    }

    private String objectToJsonString(Object data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new InternalError(e);
        }
    }
}
