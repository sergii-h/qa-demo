package com.example.demo.integration;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class TestBase {
    @LocalServerPort
    int port;

    public RequestSpecification requestSpec;

    @BeforeAll
    public static void baseBeforeAll() {
        Awaitility.setDefaultTimeout(Duration.ofMinutes(1));
    }

    @BeforeEach
    public void baseBeforeEach() {
        requestSpec = given()
                .baseUri("http://localhost:" + port + "/v1")
                .contentType(ContentType.JSON)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }
}





