package com.xyz.ean.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private String description;
    private List<Double> prices;
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
}
