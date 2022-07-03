package com.api.controller;

import com.api.annotation.Barcode;
import com.api.annotation.Number;
import com.api.error.ErrorTemplate;
import com.api.error.Violation;
import com.api.pojo.DomainUtils;
import com.api.service.PersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.api.projection.Projection.*;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class ProductController {

    private final PersistenceService persistenceService;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(
        @RequestParam(name = "pag", required = false)
        @Pattern(regexp = "\\d{1,3}-\\d{1,3}", message = "pag must match digit-digit") String pag
    ) {
        if (Objects.isNull(pag))
            return ResponseEntity.ok(persistenceService.findAllProducts());
        return ResponseEntity.ok(persistenceService.findAllPagedProducts(DomainUtils.parsePage(pag)));
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductBase getByBarcode(
        @PathVariable("barcode") @Barcode String barcode,
        @RequestParam(name = "lop", required = false, defaultValue = "0")
        @Number(message = "lop must be a positive number or zero") String limitOfPrices
    ) {
        return persistenceService.findProductByBarcode(barcode, Integer.parseInt(limitOfPrices));
    }

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
            .body(Objects.requireNonNullElseGet(rse.getReason(), () -> "No reasons"));
    }
}
