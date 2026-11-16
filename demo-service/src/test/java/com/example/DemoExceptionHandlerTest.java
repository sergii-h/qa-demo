package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.CONFLICT;

class DemoExceptionHandlerTest {
    private DemoExceptionHandler exceptionHandler;

    @BeforeEach
    void beforeEach() {
        exceptionHandler = new DemoExceptionHandler();
    }

    @Test
    void shouldHandleDuplicateKeyExceptionWithConflictStatus() {
        // given
        DuplicateKeyException exception = new DuplicateKeyException("E11000 duplicate key error");

        // when
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDuplicateKeyException(exception);

        // then
        assertThat(response.getStatusCode(), is(CONFLICT));
        assertThat(response.getBody().get("message"), is("Task with this title already exists"));
    }
}

