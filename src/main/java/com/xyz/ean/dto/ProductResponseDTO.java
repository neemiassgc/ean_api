package com.xyz.ean.dto;

import com.xyz.ean.entity.Price;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public final class ProductResponseDTO {

    private final String description;
    private final List<PriceInstant> prices;
    private final String eanCode;
    private final Integer sequenceCode;

    @Override
    public String toString() {
        final String template = "ProductResponseDTO{description='%s', prices=%s, eanCode='%s', sequenceCode='%s'}";
        return String.format(
            template,
            description,
            String.join(";", prices.stream().map(Object::toString).toArray(String[]::new)),
            eanCode,
            sequenceCode
        );
    }

    @Getter
    @RequiredArgsConstructor
    public static class PriceInstant {

        private final Instant instant;
        private final Double priceValue;

        public static PriceInstant from(final Price price) {
            return new PriceInstant(price.getInstant(), price.getPrice());
        }

        @Override
        public String toString() {
            return String.format("dateTime=%s, price=%s", instant, priceValue);
        }
    }
}
