package com.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@ToString
public final class BarcodeRequestDTO {

    @NotBlank
    @NotEmpty
    @NotNull
    private final String barcode;

    @JsonCreator
    public BarcodeRequestDTO(@JsonProperty("eanCode") String barCode) {
        this.barcode = barCode;
    }
}
