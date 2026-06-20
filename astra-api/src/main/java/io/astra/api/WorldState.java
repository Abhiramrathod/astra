package io.astra.api;

import java.util.Map;
import java.util.Optional;

public interface WorldState {
    Optional<String> get(String key);
    boolean matches(Map<String, String> conditions);
    Map<String, String> asMap();
    WorldState set(String key, String value);
}
