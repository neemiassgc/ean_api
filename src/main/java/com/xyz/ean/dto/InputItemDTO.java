package com.xyz.ean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
public final class InputItemDTO {

    private final double currentPrice;
    private final String description;
    private final int sequence;
    private final String eanCode;

    @Builder
    public InputItemDTO(
        @JsonProperty("currentPrice") final double currentPrice,
        @JsonProperty("description") final String description,
        @JsonProperty("sequence") final int sequence,
        @JsonProperty("eanCode") final String eanCode
    ) {
        this.currentPrice = currentPrice;
        this.description = description;
        this.sequence = sequence;
        this.eanCode = eanCode;
    }
}
