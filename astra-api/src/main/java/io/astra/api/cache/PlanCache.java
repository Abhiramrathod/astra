package io.astra.api.cache;

import io.astra.api.Plan;
import io.astra.api.WorldState;

/**
 * Cache for computed plans keyed by goal name and world state.
 */
public interface PlanCache {
    Plan get(String goalName, WorldState state);
    void put(String goalName, WorldState state, Plan plan);
    void invalidate(String goalName);
    void clear();
}
