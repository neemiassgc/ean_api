package com.api.pojo;

import com.api.projection.InputItemDTO;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Objects;

public final class InputItemDTODeserializer extends StdDeserializer<InputItemDTO> {

    public InputItemDTODeserializer() {
        this(null);
    }

    public InputItemDTODeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    @Nullable
    public InputItemDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        final JsonNode jsonNode = p.getCodec().readTree(p);

        final JsonNode item = jsonNode.get("item");

        if (Objects.isNull(item)) return null;

        if (item.get(5).get("value").asText().isEmpty()) throw new IllegalStateException("Item name is empty");

        final String description = item.get(1).get("value").asText();
        final int sequence = item.get(2).get("value").asInt();
        final double currentPriceValue = DomainUtils.parsePrice(item.get(4).get("value").asText());
        final String eanCodeValue = item.get(5).get("value").asText();

        return InputItemDTO.builder()
            .description(description)
            .sequence(sequence)
            .currentPrice(currentPriceValue)
            .barcode(eanCodeValue)
            .build();
    }
}
