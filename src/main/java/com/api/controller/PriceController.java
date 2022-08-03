package com.api.controller;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.api.projection.Projection.PriceWithInstant;

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
}