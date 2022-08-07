package com.api.controller;

import com.api.error.ErrorTemplate;
import com.api.error.Violation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrorHandlingController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorTemplate handleMethodArgumentNotValid(final MethodArgumentNotValidException mnv) {
        final Set<Violation> violations = mnv.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> new Violation(fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.toSet());

        return new ErrorTemplate(violations);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorTemplate handleConstraintViolationException(final ConstraintViolationException cve) {
        final Set<Violation> violations = cve.getConstraintViolations()
            .stream()
            .map(constraint -> new Violation(constraint.getInvalidValue().toString(), constraint.getMessage()))
            .collect(Collectors.toSet());

        return new ErrorTemplate(violations);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(final ResponseStatusException rse) {
        return ResponseEntity
            .status(rse.getStatus())
            .contentType(MediaType.TEXT_PLAIN)
            .body(Objects.requireNonNullElseGet(rse.getReason(), () -> "No reasons"));
    }
}
