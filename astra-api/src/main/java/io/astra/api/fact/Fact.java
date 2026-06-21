package io.astra.api.fact;

import java.util.Objects;

/**
 * A typed, serializable named fact.
 *
 * @param <T> the value type
 */
public class Fact<T> {
    private final String name;
    private final T value;
    private final Class<T> type;

    public Fact(String name, T value, Class<T> type) {
        this.name = Objects.requireNonNull(name);
        this.value = value;
        this.type = Objects.requireNonNull(type);
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public Class<T> getType() { return type; }

    public String serialize() {
        if (value == null) return "null";
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> Fact<T> deserialize(String name, String raw, Class<T> type) {
        if ("null".equals(raw)) return new Fact<>(name, null, type);
        Object val;
        if (type == String.class) val = raw;
        else if (type == Integer.class || type == int.class) val = Integer.parseInt(raw);
        else if (type == Long.class || type == long.class) val = Long.parseLong(raw);
        else if (type == Double.class || type == double.class) val = Double.parseDouble(raw);
        else if (type == Boolean.class || type == boolean.class) val = Boolean.parseBoolean(raw);
        else if (type == Float.class || type == float.class) val = Float.parseFloat(raw);
        else val = raw;
        return new Fact<>(name, (T) val, type);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Fact<?> f && name.equals(f.name) && Objects.equals(value, f.value);
    }

    @Override
    public int hashCode() { return Objects.hash(name, value); }

    @Override
    public String toString() { return name + "=" + value; }
}
