package io.astra.core.cache;

import io.astra.api.Plan;
import io.astra.api.WorldState;
import io.astra.api.cache.PlanCache;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU (least-recently-used) {@link PlanCache} backed by a
 * {@link LinkedHashMap} with automatic eviction of the eldest entry.
 */
public class LruPlanCache implements PlanCache {
    private final int maxSize;
    private final LinkedHashMap<String, Plan> cache;

    public LruPlanCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Plan> eldest) {
                return size() > maxSize;
            }
        };
    }

    private String key(String goalName, WorldState state) {
        return goalName + "::" + state.asMap();
    }

    @Override
    public synchronized Plan get(String goalName, WorldState state) {
        return cache.get(key(goalName, state));
    }

    @Override
    public synchronized void put(String goalName, WorldState state, Plan plan) {
        cache.put(key(goalName, state), plan);
    }

    @Override
    public synchronized void invalidate(String goalName) {
        cache.keySet().removeIf(k -> k.startsWith(goalName + "::"));
    }

    @Override
    public synchronized void clear() {
        cache.clear();
    }
}
