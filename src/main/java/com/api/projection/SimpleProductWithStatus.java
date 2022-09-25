package com.api.projection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@ToString
public final class SimpleProductWithStatus {

    private final SimpleProduct simpleProduct;
    private final HttpStatus httpStatus;
}