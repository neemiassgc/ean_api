package com.api.component;

import com.api.entity.Product;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

public final class DomainUtils {

    private DomainUtils() {}

    public static Pageable parsePage(@NonNull final String value, @NonNull Sort sort) {
        final String[] split = value.split("-");
        return PageRequest.of(
            Integer.parseInt(split[0]),
            Integer.parseInt(split[1]),
            sort
        );
    }

    public static String calculateNextPage(@NonNull final Page<Product> productPage) {
        return (productPage.getNumber() + 1) + "-" + productPage.getSize();
    }

    public static BigDecimal parsePrice(final String priceInput) {
        return new BigDecimal((priceInput.replace(",", ".")));
    }

    public static <T> T requireIntegrity(final T object, final String errorMessage) {
        return requireNonNull(object, new IllegalStateException(errorMessage));
    }

    public static <O> O requireNonNull(final O object, final RuntimeException runtimeException) {
        if (object == null) {
            throw runtimeException;
        }
        return object;
    }
}
