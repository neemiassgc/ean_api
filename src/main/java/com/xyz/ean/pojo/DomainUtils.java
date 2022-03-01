package com.xyz.ean.pojo;

public final class DomainUtils {

    public static double parsePrice(final String priceInput) {
        return Double.parseDouble(priceInput.replace(",", "."));
    }
}
