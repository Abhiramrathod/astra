package io.astra.api;

import java.util.*;

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

    private static class SimpleWorldState implements WorldState {
        private final Map<String, String> facts;

        SimpleWorldState(Map<String, String> facts) {
            this.facts = Map.copyOf(facts);
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
            return new SimpleWorldState(copy);
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
