package com.xyz.ean.pojo;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.xyz.ean.dto.InputItemDTO;

import java.io.IOException;

public final class InputItemDTODeserializer extends StdDeserializer<InputItemDTO> {

    public InputItemDTODeserializer() {
        this(null);
    }

    public InputItemDTODeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public InputItemDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        final JsonNode jsonNode = p.getCodec().readTree(p);
        final JsonNode item = DomainUtils.requireNonNull(jsonNode.get("item"), new NullPointerException());

        if (item.get(5).get("value").asText().isEmpty()) throw new IllegalStateException("Item name is empty");

        final String description = jsonNode.get(1).get("value").asText();
        final int sequence = jsonNode.get(2).get("value").asInt();
        final double currentPriceValue = DomainUtils.parsePrice(jsonNode.get(4).get("value").asText());
        final String eanCodeValue = jsonNode.get(5).get("value").asText();

        return InputItemDTO.builder()
            .description(description)
            .sequence(sequence)
            .currentPrice(currentPriceValue)
            .eanCode(eanCodeValue)
            .build();
    }
}
