package com.xyz.ean.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public final class EanCodeRequestDTO {

    private final String eanCode;
}
