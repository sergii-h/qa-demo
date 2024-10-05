package step.action;


import com.codeborne.selenide.Selenide;
import config.PropertyReader;
import data.ItemRequest;
import io.qameta.allure.Step;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import page.MainPage;

import static com.codeborne.selenide.Condition.visible;
import static io.restassured.RestAssured.given;

public class ApiAction {
    private static final PropertyReader PROPERTIES_READER = PropertyReader.getInstance();
    private static final String TEST_ENV = PROPERTIES_READER.getProperty("test.env");

    MainPage mainPage = new MainPage();

    RequestSpecification requestSpec = given()
            .baseUri(PROPERTIES_READER.getProperty(TEST_ENV + ".test.be.url"))
            .contentType(ContentType.JSON)
            .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    @Step("Create item by api")
    public void createItem(ItemRequest itemBody) {
       requestSpec.body(itemBody).post("/items");

       Selenide.refresh();
       mainPage.formLocator.shouldBe(visible);
    }
}
