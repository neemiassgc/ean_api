package com.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter(onMethod_ = {@NotBlank, @NotNull, @NotEmpty})
@ToString
public final class BarcodeRequestDTO {

    private final String barcode;

    @JsonCreator
    public BarcodeRequestDTO(@JsonProperty("eanCode") String barCode) {
        this.barcode = barCode;
    }
}
