package com.api.controller;

import com.api.annotation.Barcode;
import com.api.annotation.Number;
import com.api.pojo.DomainUtils;
import com.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.Objects;

import static com.api.projection.Projection.ProductBase;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(
        @RequestParam(name = "pag", required = false)
        @Pattern(regexp = "\\d{1,3}-\\d{1,3}", message = "pag must match digit-digit") String pag
    ) {
        if (Objects.isNull(pag))
            return ResponseEntity.ok(productRepository.findAllProducts());
        return ResponseEntity.ok(productRepository.findAllPagedProducts(DomainUtils.parsePage(pag)));
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductBase getByBarcode(
        @PathVariable("barcode") @Barcode String barcode,
        @RequestParam(name = "lop", required = false, defaultValue = "0")
        @Number(message = "lop must be a positive number or zero") String limitOfPrices
    ) {
        return productRepository.findProductByBarcode(barcode, Integer.parseInt(limitOfPrices));
    }
}
