package com.api.controller;

import com.api.annotation.Barcode;
import com.api.entity.Price;
import static com.api.projection.Projection.*;
import com.api.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PriceController {

    private final PriceRepository priceRepository;

    @GetMapping(path = "/prices/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PriceWithInstant searchById(@PathVariable("id") final UUID id) {
        final Price fetchedPrice = priceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));

        return new PriceWithInstant(fetchedPrice.getValue(), fetchedPrice.getInstant());
    }

    @GetMapping(path = "/prices", params = "barcode", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PriceWithInstant> searchByProductBarcode(@RequestParam("barcode") @Barcode final String barcode) {
        final List<Price> priceList = priceRepository.findByProductBarcodeOrderByInstantDesc(barcode);

        if (priceList.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        return priceList.stream()
            .map(price -> new PriceWithInstant(price.getValue(), price.getInstant()))
            .collect(Collectors.toList());
    }
}