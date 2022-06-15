package com.api.projection;

import java.util.UUID;

public final class Projection {

    private Projection() {}

    public interface ProductWithLatestPrice {
        String getBarcode();
        String getDesc();
        String getSeqCode();
        UUID getId();
        double getLatestPrice();
    }
}
