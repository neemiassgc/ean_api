package com.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
public final class EanCodeRequestDTO {

    private final String eanCode;

    @JsonCreator
    public EanCodeRequestDTO(@JsonProperty("eanCode") String eanCode) {
        this.eanCode = eanCode;
    }
}
