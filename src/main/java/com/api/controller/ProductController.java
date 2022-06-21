package com.api.controller;

import com.api.error.ErrorTemplate;
import com.api.service.PersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.api.projection.Projection.ProductBase;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final PersistenceService persistenceService;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductBase> getAll() {
        return persistenceService.findAllProducts();
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductBase getByBarcode(
        @PathVariable("barcode") String barcode,
        @RequestParam(name = "lop", required = false, defaultValue = "0") int limitOfPrices
    ) {
        return persistenceService.findProductByBarcode(barcode, limitOfPrices);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorTemplate handleMethodArgumentNotValid(final MethodArgumentNotValidException mnv) {
        final List<String> reasons = mnv.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> "'"+fe.getField()+"' "+fe.getDefaultMessage())
            .collect(Collectors.toList());

        return new ErrorTemplate(HttpStatus.BAD_REQUEST, reasons);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorTemplate> handleResponseStatusException(final ResponseStatusException rse) {
        return ResponseEntity
            .status(rse.getStatus())
            .body(new ErrorTemplate(rse.getStatus(), List.of(Objects.requireNonNullElseGet(rse.getReason(), () -> "No reasons"))));
    }
}
