package support.api;

import config.PropertyReader;
import data.TaskRequest;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class ApiClient {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();

    RequestSpecification requestSpec = given()
            .baseUri(PROPERTIES_READER.getEnvProperty("test.be.url"))
            .contentType(ContentType.JSON)
            .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    public Response createTask(TaskRequest taskBody) {
        return requestSpec.body(taskBody).post("/tasks");
    }
}
