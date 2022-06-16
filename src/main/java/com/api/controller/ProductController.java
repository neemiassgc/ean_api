package com.api.controller;

import com.api.entity.Product;
import com.api.error.ErrorTemplate;
import com.api.projection.BarcodeInput;
import com.api.projection.ProductResponseDTO;
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

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final ProductService productService;
    private final DomainMapper domainMapper;

    @PostMapping(path = "/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponseDTO create(@RequestBody @Valid BarcodeInput barcodeInput) {
        final Product fetchedProduct = productService.saveByBarcode(barcodeInput.getBarcode());
        return domainMapper.mapToDto(fetchedProduct);
    }

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductResponseDTO> getAll() {
        return domainMapper.mapToDtoList(productService.findAllByOrderByDescriptionAsc());
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponseDTO getByBarcode(@PathVariable("barcode") String barcode) {
        final Product fetchedProduct = productService.findByBarcode(barcode);
        return domainMapper.mapToDto(fetchedProduct);
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
