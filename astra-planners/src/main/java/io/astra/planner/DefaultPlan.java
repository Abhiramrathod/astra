package io.astra.planner;

import io.astra.api.*;
import io.astra.api.result.ActionExecutionRecord;
import io.astra.api.result.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class DefaultPlan implements Plan {
    private static final Logger log = LoggerFactory.getLogger(DefaultPlan.class);
    private final List<ActionInfo> actions;
    private final double totalCost;

    public DefaultPlan(List<ActionInfo> actions, double totalCost) {
        this.actions = actions;
        this.totalCost = totalCost;
    }

    @Override
    public List<ActionInfo> getActions() { return actions; }

    @Override
    public double getTotalCost() { return totalCost; }

    @Override
    public boolean isExecutable() { return !actions.isEmpty(); }

    @Override
    public WorldState execute(WorldState initialState) {
        log.debug("Executing plan with {} actions", actions.size());
        WorldState state = initialState;
        for (ActionInfo action : actions) {
            long start = System.currentTimeMillis();
            log.debug("Executing action: {}", action.getName());
            action.getExecutor().run();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("Action '{}' completed in {}ms", action.getName(), elapsed);
            for (var entry : action.getEffects().entrySet()) {
                state = state.set(entry.getKey(), entry.getValue());
            }
        }
        log.info("Plan execution completed. Final state: {}", state);
        return state;
    }

    public ExecutionResult executeWithTracking(WorldState initialState) {
        log.debug("Executing plan with tracking, {} actions", actions.size());
        List<ActionExecutionRecord> records = new ArrayList<>();
        WorldState state = initialState;
        boolean overallSuccess = true;
        long planStart = System.currentTimeMillis();

        for (ActionInfo action : actions) {
            WorldState before = state;
            long start = System.currentTimeMillis();
            boolean success = true;
            String errorMsg = null;

            try {
                log.debug("Executing action: {}", action.getName());
                action.getExecutor().run();
            } catch (Exception e) {
                success = false;
                errorMsg = e.getMessage();
                overallSuccess = false;
                log.error("Action '{}' failed: {}", action.getName(), e.getMessage());
            }

            long elapsed = System.currentTimeMillis() - start;

            if (success) {
                for (var entry : action.getEffects().entrySet()) {
                    state = state.set(entry.getKey(), entry.getValue());
                }
            }

            records.add(new ActionExecutionRecord(action, before, state, success, elapsed, errorMsg));

            if (!success) break;
        }

        long totalDuration = System.currentTimeMillis() - planStart;

        if (overallSuccess) {
            log.info("Plan executed successfully in {}ms. Final state: {}", totalDuration, state);
            return ExecutionResult.success(this, state, records, totalDuration);
        }
        log.error("Plan execution failed after {}ms", totalDuration);
        return ExecutionResult.failure(this, state, records, totalDuration,
            "Action failed: " + records.stream()
                .filter(r -> !r.isSuccess())
                .findFirst()
                .map(r -> r.getAction().getName())
                .orElse("unknown"));
    }

    @Override
    public String toString() {
        return "Plan{actions=" + actions + ", totalCost=" + totalCost + "}";
    }
}
