package com.xyz.ean.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xyz.ean.entity.Price;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public final class ProductResponseDTO {

    private final String description;
    private final List<PriceInstant> priceInstants;
    private final String eanCode;
    private final Integer sequenceCode;

    @JsonCreator
    @Builder
    public ProductResponseDTO(
        @JsonProperty("description") final String description,
        @JsonProperty("priceInstants") final List<PriceInstant> priceInstants,
        @JsonProperty("eanCode") final String eanCode,
        @JsonProperty("sequenceCode") final Integer sequenceCode
    ) {
        this.description = description;
        this.priceInstants = priceInstants;
        this.eanCode = eanCode;
        this.sequenceCode = sequenceCode;
    }

    @Override
    public String toString() {
        final String template = "ProductResponseDTO{description='%s', prices={%s}, eanCode='%s', sequenceCode='%s'}";
        return String.format(
            template,
            description,
            priceInstants.stream().map(PriceInstant::toString).collect(Collectors.joining(", ")),
            eanCode,
            sequenceCode
        );
    }

    @Getter
    public static class PriceInstant {

        private final Instant instant;
        private final Double priceValue;

        @JsonCreator
        public PriceInstant(
            @JsonProperty("instant") final Instant instant,
            @JsonProperty("price") final Double priceValue
        ) {
            this.instant = instant;
            this.priceValue = priceValue;
        }

        public static PriceInstant from(final Price price) {
            return new PriceInstant(price.getInstant(), price.getPrice());
        }

        @Override
        public String toString() {
            return String.format("{Instant=%s, price=%s}", instant, priceValue);
        }
    }
}
