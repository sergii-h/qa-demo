package com.example;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
class DemoExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<String> handleNotFundException(Exception e, HttpServletRequest request) {

        return ResponseEntity
                .status(NOT_FOUND)
                .body(e.getMessage() + " for request " + request.getRequestURI());
    }
}