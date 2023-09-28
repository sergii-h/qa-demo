package step.action;


import com.codeborne.selenide.Selenide;
import data.ItemRequest;
import io.qameta.allure.Step;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static test.TestBase.ENV;

public class ApiAction {
    RequestSpecification requestSpec = given()
            .baseUri(ENV.beUrl)
            .contentType(ContentType.JSON)
            .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    @Step("Create item by api")
    public void createItem(ItemRequest itemBody) {
       requestSpec.body(itemBody).post("/item");
       Selenide.refresh();
    }
}
