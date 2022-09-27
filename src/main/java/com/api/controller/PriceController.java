package com.api.controller;

import com.api.annotation.Barcode;
import com.api.entity.Price;
import com.api.projection.PriceWithInstant;
import com.api.service.interfaces.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@CrossOrigin
public class PriceController {

    private final PriceService priceService;

    @GetMapping(path = "/prices/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PriceWithInstant searchById(@PathVariable("id") final UUID id) {
        return priceService.findById(id).toPriceWithInstant();
    }

    @GetMapping(path = "/prices", params = {"barcode", "limit"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PriceWithInstant> searchByProductBarcode(
        @RequestParam("barcode") @Barcode final String barcode,
        @PositiveOrZero @RequestParam(value = "limit", defaultValue = "0") final int limit
    ) {
        return (
            limit > 0
            ? priceService.findByProductBarcode(barcode, PageRequest.of(0, limit, Sort.by("instant").descending()))
            : priceService.findByProductBarcode(barcode, Sort.by("instant").descending())
        )
        .stream()
        .map(Price::toPriceWithInstant)
        .collect(Collectors.toList());
    }

    @GetMapping(path = "/prices", params = "barcode", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PriceWithInstant> searchByProductBarcode(@RequestParam("barcode") @Barcode final String barcode) {
        return searchByProductBarcode(barcode, 0);
    }
}