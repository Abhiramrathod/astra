package io.astra.telemetry;

import io.astra.api.Plan;
import io.astra.api.WorldState;
import io.astra.api.telemetry.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4J-based {@link Tracer} that logs trace events at INFO/WARN levels.
 */
public class Slf4jTracer implements Tracer {
    private static final Logger log = LoggerFactory.getLogger("astra.tracing");

    @Override
    public void tracePlanStart(String goalName, WorldState state) {
        log.info("[TRACE] plan_start goal={}", goalName);
    }

    @Override
    public void tracePlanComplete(String goalName, Plan plan, long durationMs) {
        log.info("[TRACE] plan_complete goal={} durationMs={}", goalName, durationMs);
    }

    @Override
    public void tracePlanFailed(String goalName, String reason) {
        log.warn("[TRACE] plan_failed goal={} reason={}", goalName, reason);
    }

    @Override
    public void traceActionStart(String actionName, WorldState state) {
        log.info("[TRACE] action_start action={}", actionName);
    }

    @Override
    public void traceActionComplete(String actionName, WorldState newState, long durationMs) {
        log.info("[TRACE] action_complete action={} durationMs={}", actionName, durationMs);
    }

    @Override
    public void traceActionFailed(String actionName, String error) {
        log.warn("[TRACE] action_failed action={} error={}", actionName, error);
    }
}
