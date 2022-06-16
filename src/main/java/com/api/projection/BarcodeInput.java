package com.api.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter(onMethod_ = {@NotBlank, @NotNull, @NotEmpty})
public class BarcodeInput {

    @JsonProperty("barcode")
    private final String barcode;
}
