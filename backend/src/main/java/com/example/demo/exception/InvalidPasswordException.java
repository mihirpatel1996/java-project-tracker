package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordException extends RuntimeException {

    private final List<String> errors;

    public InvalidPasswordException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public InvalidPasswordException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}