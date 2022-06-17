package com.api.controller;

import com.api.error.ErrorTemplate;
import com.api.projection.BarcodeInput;
import com.api.repository.PriceRepository;
import com.api.service.DomainMapper;
import com.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.api.projection.Projection.*;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final ProductService productService;
    private final DomainMapper domainMapper;
    private final PriceRepository priceRepository;

    @PostMapping(path = "/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductWithLatestPrice create(@RequestBody @Valid BarcodeInput barcodeInput) {
        return domainMapper.toProductWithLatestPrice(priceRepository.findLatestPriceByProductBarcode(barcodeInput.getBarcode()).get());
    }

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductWithAllPrices> getAll() {
        return domainMapper.toProductListWithAllPrices(priceRepository.findAll());
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductWithAllPrices getByBarcode(@PathVariable("barcode") String barcode) {
        return domainMapper.toProductWithAllPrices(priceRepository.findAllByProductBarcode(barcode));
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
