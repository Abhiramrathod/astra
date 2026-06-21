package io.astra.api;

import io.astra.api.fact.Fact;
import java.util.*;

/** Factory and utility methods for creating {@link WorldState} instances. */
public final class WorldStates {
    private WorldStates() {}

    public static WorldState empty() {
        return new SimpleWorldState(Map.of());
    }

    public static WorldState of(Map<String, String> facts) {
        return new SimpleWorldState(facts);
    }

    public static WorldState of(String key, String value) {
        return new SimpleWorldState(Map.of(key, value));
    }

    public static WorldState of(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) throw new IllegalArgumentException("Must provide pairs of key,value");
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return new SimpleWorldState(map);
    }

    public static WorldState fromSnapshot(Snapshot snapshot) {
        return snapshot.restore();
    }

    private static class SimpleWorldState implements WorldState {
        private final Map<String, String> facts;
        private final Map<String, String> typeHints;

        SimpleWorldState(Map<String, String> facts) {
            this.facts = Map.copyOf(facts);
            this.typeHints = Map.of();
        }

        private SimpleWorldState(Map<String, String> facts, Map<String, String> typeHints) {
            this.facts = facts;
            this.typeHints = typeHints;
        }

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(facts.get(key));
        }

        @Override
        public boolean matches(Map<String, String> conditions) {
            for (var entry : conditions.entrySet()) {
                String actual = facts.get(entry.getKey());
                if (actual == null || !actual.equals(entry.getValue())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Map<String, String> asMap() {
            return facts;
        }

        @Override
        public WorldState set(String key, String value) {
            var copy = new LinkedHashMap<>(facts);
            copy.put(key, value);
            return new SimpleWorldState(Map.copyOf(copy), typeHints);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> getTyped(String key, Class<T> type) {
            String raw = facts.get(key);
            if (raw == null) return Optional.empty();
            String hint = typeHints.get(key);
            if (hint != null && !hint.equals(type.getName())) return Optional.empty();
            Fact<T> fact = Fact.deserialize(key, raw, type);
            return Optional.ofNullable(fact.getValue());
        }

        @Override
        public <T> WorldState setTyped(String key, T value, Class<T> type) {
            var factsCopy = new LinkedHashMap<>(facts);
            var hintsCopy = new LinkedHashMap<>(typeHints);
            factsCopy.put(key, value == null ? "null" : value.toString());
            hintsCopy.put(key, type.getName());
            return new SimpleWorldState(Map.copyOf(factsCopy), Map.copyOf(hintsCopy));
        }

        @Override
        public Snapshot snapshot() {
            Map<String, String> frozenFacts = this.facts;
            Map<String, String> frozenHints = this.typeHints;
            return () -> new SimpleWorldState(frozenFacts, frozenHints);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SimpleWorldState ws && facts.equals(ws.facts);
        }

        @Override
        public int hashCode() {
            return facts.hashCode();
        }

        @Override
        public String toString() {
            return facts.toString();
        }
    }
}
