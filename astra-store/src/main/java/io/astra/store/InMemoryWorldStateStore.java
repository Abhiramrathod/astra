package io.astra.store;

import io.astra.api.WorldState;
import io.astra.api.WorldStates;
import io.astra.api.store.WorldStateStore;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** A {@link WorldStateStore} backed by a {@link ConcurrentHashMap} for in-memory storage. */
public class InMemoryWorldStateStore implements WorldStateStore {
    private final Map<String, WorldState> store = new ConcurrentHashMap<>();

    @Override
    public void save(String sessionId, WorldState state) {
        store.put(sessionId, state);
    }

    @Override
    public Optional<WorldState> load(String sessionId) {
        return Optional.ofNullable(store.get(sessionId));
    }

    @Override
    public void delete(String sessionId) {
        store.remove(sessionId);
    }

    @Override
    public boolean exists(String sessionId) {
        return store.containsKey(sessionId);
    }
}
