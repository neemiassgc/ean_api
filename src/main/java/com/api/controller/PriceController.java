package com.api.controller;

import com.api.annotation.Barcode;
import com.api.entity.Price;
import com.api.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.api.projection.Projection.PriceWithInstant;

@Validated
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@CrossOrigin
public class PriceController {

    private final PriceRepository priceRepository;

    @GetMapping(path = "/prices/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PriceWithInstant searchById(@PathVariable("id") final UUID id) {
        final Price fetchedPrice = priceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));

        return new PriceWithInstant(fetchedPrice.getValue(), fetchedPrice.getInstant());
    }

    @GetMapping(path = "/prices", params = {"barcode", "limit"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PriceWithInstant> searchByProductBarcode(
        @RequestParam("barcode") @Barcode final String barcode,
        @PositiveOrZero @RequestParam(value = "limit", defaultValue = "0") final int limit
    ) {
        List<Price> priceList = null;

        if (limit > 0) priceList = priceRepository.findByProductBarcode(
            barcode, PageRequest.of(0, limit, Sort.by("instant").descending())
        );
        else priceList = priceRepository.findByProductBarcode(barcode, Sort.by("instant").descending());

        if (priceList.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        return priceList.stream()
            .map(price -> new PriceWithInstant(price.getValue(), price.getInstant()))
            .collect(Collectors.toList());
    }

    @GetMapping(path = "/prices", params = "barcode", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PriceWithInstant> searchByProductBarcode(@RequestParam("barcode") @Barcode final String barcode) {
        return searchByProductBarcode(barcode, 0);
    }
}