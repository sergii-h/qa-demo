package com.example.demo.integration.test.api;

import com.example.demo.data.Task;
import com.example.demo.integration.ApiIntegrationTestBase;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ListTasksEmptyTest extends ApiIntegrationTestBase {

    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void overrideDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.database", () -> "task_db_empty");
    }

    @BeforeEach
    void beforeEach() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void shouldReturnHttp200WithEmptyArrayWhenNoTasksExist() {
        // when
        Response response = requestSpec.get("/tasks");

        // then
        List<Task> tasks = Arrays.asList(response.getBody().as(Task[].class));

        assertAll(
                () -> assertThat(response.statusCode(), is(200)),
                () -> assertThat(tasks, empty())
        );
    }
}
