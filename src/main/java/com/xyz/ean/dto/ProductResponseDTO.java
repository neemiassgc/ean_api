package com.xyz.ean.dto;

import com.xyz.ean.entity.Price;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class ProductResponseDTO {

    private String description;
    private List<PriceInstant> prices;
    private String eanCode;
    private Integer sequenceCode;

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

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PriceInstant {

        private Instant instant;
        private Double price;

        public PriceInstant(final Price price) {
            this.instant = price.getInstant();
            this.price = price.getPrice();
        }

        @Override
        public String toString() {
            return String.format("dateTime=%s, price=%s", instant, price);
        }
    }
}
