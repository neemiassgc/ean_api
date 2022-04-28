package com.api.controller;

import com.api.service.DomainMapper;
import com.api.dto.BarcodeRequestDTO;
import com.api.dto.ProductResponseDTO;
import com.api.entity.Product;
import com.api.error.ErrorTemplate;
import com.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final ProductService productService;
    private final DomainMapper domainMapper;

    @PostMapping(path = "/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponseDTO create(@RequestBody BarcodeRequestDTO barcodeRequestDTO) {
        final Product fetchedProduct = productService.saveByBarcode(barcodeRequestDTO.getBarcode());
        return domainMapper.mapToDto(fetchedProduct);
    }

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductResponseDTO> getAll() {
        return domainMapper.mapToDtoList(productService.findAll());
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponseDTO getByBarcode(@PathVariable("barcode") String barcode) {
        final Product fetchedProduct = productService.findByBarcode(barcode);
        return domainMapper.mapToDto(fetchedProduct);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorTemplate> handleException(final ResponseStatusException rse) {
        return ResponseEntity
            .status(rse.getStatus())
            .body(new ErrorTemplate(rse.getStatus(), List.of(Objects.requireNonNullElseGet(rse.getReason(), () -> "No reasons"))));
    }
}
