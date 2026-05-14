package com.example;

import com.example.demo.DuplicateTitleException;
import com.example.demo.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class DemoExceptionHandlerTest {
    private DemoExceptionHandler exceptionHandler;

    @BeforeEach
    void beforeEach() {
        exceptionHandler = new DemoExceptionHandler();
    }

    @Test
    void shouldHandleTaskNotFoundExceptionWithNotFoundStatus() {
        // given
        TaskNotFoundException exception = new TaskNotFoundException("Task not found with id: abc123");

        // when
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNotFoundException(exception);

        // then
        assertThat(response.getStatusCode(), is(NOT_FOUND));
        assertThat(response.getBody().get("message"), is("Task not found with id: abc123"));
    }

    @Test
    void shouldHandleDuplicateTitleExceptionWithConflictStatus() {
        // given
        DuplicateTitleException exception = new DuplicateTitleException("Task with title 'My Task' already exists");

        // when
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDuplicateTitleException(exception);

        // then
        assertThat(response.getStatusCode(), is(CONFLICT));
        assertThat(response.getBody().get("message"), is("Task with title 'My Task' already exists"));
    }

    @Test
    void shouldHandleValidationExceptionsWithBadRequestStatus() {
        // given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("task", "title", "must not be blank");
        FieldError fieldError2 = new FieldError("task", "priority", "must not be null");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // when
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // then
        assertThat(response.getStatusCode(), is(BAD_REQUEST));
        assertThat(response.getBody().size(), is(2));
        assertEquals("must not be blank", response.getBody().get("title"));
        assertEquals("must not be null", response.getBody().get("priority"));
    }

    @Test
    void shouldReturnAllValidationErrorsInMap() {
        // given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError("task", "title", "size must be between 1 and 100");
        FieldError error2 = new FieldError("task", "status", "must not be null");
        FieldError error3 = new FieldError("task", "priority", "must not be null");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2, error3));

        // when
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // then
        Map<String, String> errors = response.getBody();
        assertThat(errors.size(), is(3));
        assertEquals("size must be between 1 and 100", errors.get("title"));
        assertEquals("must not be null", errors.get("status"));
        assertEquals("must not be null", errors.get("priority"));
    }
}

