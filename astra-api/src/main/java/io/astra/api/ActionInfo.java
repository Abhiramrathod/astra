package io.astra.api;

import java.util.Map;

public interface ActionInfo {
    String getName();
    Map<String, String> getPreconditions();
    Map<String, String> getEffects();
    float getCost();
    Runnable getExecutor();
    default float getUtility() { return 0.5f; }
    default String getUtilityMethod() { return ""; }
    default String getDescription() { return ""; }
    default double computeUtility(WorldState state) { return getUtility(); }
}
