package com.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
public final class BarcodeRequestDTO {

    private final String barcode;

    @JsonCreator
    public BarcodeRequestDTO(@JsonProperty("eanCode") String barCode) {
        this.barcode = barCode;
    }
}
