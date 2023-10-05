package com.api.controller;

import com.api.annotation.Barcode;
import com.api.entity.Price;
import com.api.projection.PriceWithInstant;
import com.api.service.interfaces.PriceService;
import com.api.utility.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.PositiveOrZero;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PriceController {

    private final PriceService priceService;

    @GetMapping(path = "/prices/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriceWithInstant> searchById(@PathVariable("id") final UUID id) {
        return buildResponse(priceService.findById(id).toPriceWithInstant());
    }

    @GetMapping(path = "/prices", params = {"barcode", "limit"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PriceWithInstant>> searchByProductBarcode(
        @RequestParam("barcode") @Barcode final String barcode,
        @PositiveOrZero @RequestParam(value = "limit", defaultValue = "0") final int limit
    ) {
        final List<PriceWithInstant> listOfPrices = (
            limit > 0
            ? priceService.findByProductBarcode(barcode, PageRequest.of(0, limit, Sort.by("instant").descending()))
            : priceService.findByProductBarcode(barcode, Sort.by("instant").descending())
        )
        .stream()
        .map(Price::toPriceWithInstant)
        .collect(Collectors.toList());

        return buildResponse(listOfPrices);
    }

    @GetMapping(path = "/prices", params = "barcode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PriceWithInstant>> searchByProductBarcode(@RequestParam("barcode") @Barcode final String barcode) {
        return searchByProductBarcode(barcode, 0);
    }

    private <B> ResponseEntity<B> buildResponse(final B body) {
        final CacheControl cacheControl = CacheControl.maxAge(Duration.ofSeconds(calculateCacheControl()));
        return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .body(body);
    }

    private long calculateCacheControl() {
        final ZoneId timezone = ZoneId.of(Constants.TIMEZONE);
        final LocalDate tomorrow = LocalDate.now(timezone).plusDays(1);
        final LocalTime fiveAm = LocalTime.of(5, 0);
        final ZonedDateTime tomorrowAtFiveAm = ZonedDateTime.of( LocalDateTime.of(tomorrow, fiveAm), timezone);
        return ChronoUnit.SECONDS.between(ZonedDateTime.now(timezone), tomorrowAtFiveAm);
    }
}