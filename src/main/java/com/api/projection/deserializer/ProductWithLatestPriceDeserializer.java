package com.api.projection.deserializer;

import com.api.pojo.DomainUtils;
import com.api.projection.Projection;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

public final class ProductWithLatestPriceDeserializer extends StdDeserializer<Projection.ProductWithLatestPrice> {

    public ProductWithLatestPriceDeserializer() {
        this(null);
    }

    public ProductWithLatestPriceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    @Nullable
    public Projection.ProductWithLatestPrice deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        final JsonNode jsonNode = p.getCodec().readTree(p);

        final JsonNode item = jsonNode.get("item");

        if (Objects.isNull(item)) return null;

        if (item.get(5).get("value").asText().isEmpty()) throw new IllegalStateException("Item name is empty");

        return new Projection.ProductWithLatestPrice() {

            @Override
            public String getDescription() {
                return  item.get(1).get("value").asText();
            }

            @Override
            public String getBarcode() {
                return item.get(5).get("value").asText();
            }

            @Override
            public Integer getSequenceCode() {
                return item.get(2).get("value").asInt();
            }

            @Override
            public BigDecimal getLatestPrice() {
                return DomainUtils.parsePrice(item.get(4).get("value").asText());
            }
        };
    }
}
