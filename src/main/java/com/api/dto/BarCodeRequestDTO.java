package com.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
public final class BarCodeRequestDTO {

    private final String barCode;

    @JsonCreator
    public BarCodeRequestDTO(@JsonProperty("eanCode") String barCode) {
        this.barCode = barCode;
    }
}
