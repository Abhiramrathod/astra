package io.astra.api.store;

import io.astra.api.WorldState;
import java.util.Optional;

/**
 * Persistence store for {@link io.astra.api.WorldState} snapshots keyed by session.
 */
public interface WorldStateStore {
    void save(String sessionId, WorldState state);
    Optional<WorldState> load(String sessionId);
    void delete(String sessionId);
    boolean exists(String sessionId);
}
