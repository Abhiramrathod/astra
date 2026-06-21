package io.astra.sample.cache;

import io.astra.api.*;
import io.astra.core.DefaultAstra;
import io.astra.core.cache.LruPlanCache;
import java.util.Map;

/** Demonstrates {@link io.astra.api.cache.PlanCache} with LRU eviction for repeated plan queries. */
public class CacheDemo {
    public static void run() {
        System.out.println("\n=== Cache: PlanCache ===");
        LruPlanCache cache = new LruPlanCache(10);
        AgentBase agent = new AgentBase() {{
            addAction("Step1", () -> {}, Map.of(), Map.of("a", "1"));
            addAction("Step2", () -> {}, Map.of("a", "1"), Map.of("b", "2"));
        }};
        DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().register(agent).withCache(cache).build();
        WorldState state = WorldStates.of("x", "0");

        Plan first = astra.plan("_auto_goal", state);
        System.out.println("  First plan: " + first.getActions().size() + " steps, cost=" + first.getTotalCost());

        Plan cached = astra.plan("_auto_goal", state);
        System.out.println("  Cached plan: " + cached.getActions().size() + " steps (same instance? " + (first == cached) + ")");
    }
}
