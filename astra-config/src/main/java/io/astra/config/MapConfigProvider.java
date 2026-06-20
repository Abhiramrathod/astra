package io.astra.config;

import io.astra.api.config.AstraConfig;
import java.util.*;

public class MapConfigProvider implements AstraConfig {
    private final Map<String, String> values;

    public MapConfigProvider(Map<String, String> values) {
        this.values = new LinkedHashMap<>(values);
    }

    public MapConfigProvider() {
        this.values = new LinkedHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        String value = values.get(key);
        if (value == null) throw new NoSuchElementException("Missing config: " + key);
        return convert(value, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        String value = values.get(key);
        if (value == null) return defaultValue;
        return convert(value, type);
    }

    @Override
    public Optional<String> getString(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public Optional<Integer> getInt(String key) {
        String v = values.get(key);
        return v == null ? Optional.empty() : Optional.of(Integer.parseInt(v));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        String v = values.get(key);
        return v == null ? Optional.empty() : Optional.of(Boolean.parseBoolean(v));
    }

    @Override
    public Optional<Double> getDouble(String key) {
        String v = values.get(key);
        return v == null ? Optional.empty() : Optional.of(Double.parseDouble(v));
    }

    @Override
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public void set(String key, String value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(String value, Class<T> type) {
        if (type == String.class) return (T) value;
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf(value);
        if (type == Boolean.class || type == boolean.class) return (T) Boolean.valueOf(value);
        if (type == Double.class || type == double.class) return (T) Double.valueOf(value);
        if (type == Long.class || type == long.class) return (T) Long.valueOf(value);
        if (type == Float.class || type == float.class) return (T) Float.valueOf(value);
        throw new IllegalArgumentException("Unsupported config type: " + type);
    }
}
