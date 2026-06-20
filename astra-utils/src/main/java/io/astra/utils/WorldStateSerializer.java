package io.astra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.astra.api.WorldState;
import io.astra.api.WorldStates;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WorldStateSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WorldStateSerializer() {}

    public static String toJson(WorldState state) {
        try {
            return MAPPER.writeValueAsString(state.asMap());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize WorldState to JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static WorldState fromJson(String json) {
        try {
            Map<String, String> map = MAPPER.readValue(json, LinkedHashMap.class);
            return WorldStates.of(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize WorldState from JSON", e);
        }
    }
}
