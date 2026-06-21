package io.astra.api;

import io.astra.api.fact.Fact;
import java.util.Map;
import java.util.Optional;

/** Immutable key-value state used for planning and execution. */
public interface WorldState {
    Optional<String> get(String key);
    boolean matches(Map<String, String> conditions);
    Map<String, String> asMap();
    WorldState set(String key, String value);

    <T> Optional<T> getTyped(String key, Class<T> type);
    <T> WorldState setTyped(String key, T value, Class<T> type);
    default Snapshot snapshot() { return () -> this; }
}
