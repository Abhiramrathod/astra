package io.astra.api.result;

import io.astra.api.*;
import java.time.Duration;
import java.util.*;

public class ExecutionResult {
    private final Plan plan;
    private final WorldState finalState;
    private final boolean success;
    private final List<ActionExecutionRecord> actionRecords;
    private final long totalDurationMs;
    private final String errorMessage;

    public ExecutionResult(Plan plan, WorldState finalState, boolean success,
                           List<ActionExecutionRecord> actionRecords,
                           long totalDurationMs, String errorMessage) {
        this.plan = plan;
        this.finalState = finalState;
        this.success = success;
        this.actionRecords = actionRecords;
        this.totalDurationMs = totalDurationMs;
        this.errorMessage = errorMessage;
    }

    public Plan getPlan() { return plan; }
    public WorldState getFinalState() { return finalState; }
    public boolean isSuccess() { return success; }
    public List<ActionExecutionRecord> getActionRecords() { return actionRecords; }
    public long getTotalDurationMs() { return totalDurationMs; }
    public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }

    public static ExecutionResult success(Plan plan, WorldState finalState,
                                           List<ActionExecutionRecord> records, long durationMs) {
        return new ExecutionResult(plan, finalState, true, records, durationMs, null);
    }

    public static ExecutionResult failure(Plan plan, WorldState finalState,
                                           List<ActionExecutionRecord> records,
                                           long durationMs, String errorMessage) {
        return new ExecutionResult(plan, finalState, false, records, durationMs, errorMessage);
    }
}
