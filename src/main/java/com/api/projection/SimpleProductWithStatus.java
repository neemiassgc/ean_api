package com.api.projection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import static com.api.projection.Projection.SimpleProduct;

@RequiredArgsConstructor
@Getter
public final class SimpleProductWithStatus {

    private final SimpleProduct simpleProduct;
    private final HttpStatus httpStatus;
}
