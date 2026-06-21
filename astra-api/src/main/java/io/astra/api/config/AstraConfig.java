package io.astra.api.config;

import java.util.Optional;

/**
 * Configuration interface for typed key-value access.
 */
public interface AstraConfig {
    <T> T get(String key, Class<T> type);
    <T> T getOrDefault(String key, Class<T> type, T defaultValue);
    Optional<String> getString(String key);
    Optional<Integer> getInt(String key);
    Optional<Boolean> getBoolean(String key);
    Optional<Double> getDouble(String key);
    boolean containsKey(String key);
}
