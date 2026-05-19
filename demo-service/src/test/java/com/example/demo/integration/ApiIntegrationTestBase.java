package com.example.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class ApiIntegrationTestBase {
    @LocalServerPort
    public int port;

    @Autowired
    private ObjectMapper objectMapper;

    public RequestSpecification requestSpec;

    static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.0.3")
            .withCommand("--replSet rs0 --bind_ip_all");

    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:3.7.1");

    static {
        mongo.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongo.getConnectionString() + "/task_db");
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    public void baseBeforeEach() {
        requestSpec = given()
                .config(RestAssured.config()
                        .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                                .jackson2ObjectMapperFactory((type, charset) -> objectMapper)))
                .baseUri("http://localhost:" + port + "/v1")
                .contentType(ContentType.JSON)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }
}
