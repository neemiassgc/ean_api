package com.api.projection;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Builder
@ToString
public final class SimpleProduct {

    private final String description;
    private final int sequenceCode;
    private final String barcode;
}