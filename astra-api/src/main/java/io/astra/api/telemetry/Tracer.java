package io.astra.api.telemetry;

import io.astra.api.Plan;
import io.astra.api.WorldState;

/**
 * Tracer for plan and action lifecycle events.
 */
public interface Tracer {
    void tracePlanStart(String goalName, WorldState state);
    void tracePlanComplete(String goalName, Plan plan, long durationMs);
    void tracePlanFailed(String goalName, String reason);
    void traceActionStart(String actionName, WorldState state);
    void traceActionComplete(String actionName, WorldState newState, long durationMs);
    void traceActionFailed(String actionName, String error);
}
