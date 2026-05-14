package com.example;

import com.example.demo.DuplicateTitleException;
import com.example.demo.TaskNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
class DemoExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    protected ResponseEntity<Map<String, String>> handleNotFoundException(TaskNotFoundException e) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(DuplicateTitleException.class)
    protected ResponseEntity<Map<String, String>> handleDuplicateTitleException(DuplicateTitleException e) {
        return ResponseEntity
                .status(CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(errors);
    }
}