package com.xyz.ean.dto;

import lombok.*;

@Getter
@ToString
@Builder
public final class StandardProductDTO {

    private final double currentPrice;
    private final String description;
    private final int sequence;
    private final String eanCode;
}
