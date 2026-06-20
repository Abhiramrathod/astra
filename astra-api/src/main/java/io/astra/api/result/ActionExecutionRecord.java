package io.astra.api.result;

import io.astra.api.*;
import java.time.Duration;
import java.util.*;

public class ActionExecutionRecord {
    private final ActionInfo action;
    private final WorldState stateBefore;
    private final WorldState stateAfter;
    private final boolean success;
    private final long durationMs;
    private final String errorMessage;

    public ActionExecutionRecord(ActionInfo action, WorldState stateBefore,
                                  WorldState stateAfter, boolean success,
                                  long durationMs, String errorMessage) {
        this.action = action;
        this.stateBefore = stateBefore;
        this.stateAfter = stateAfter;
        this.success = success;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
    }

    public ActionInfo getAction() { return action; }
    public WorldState getStateBefore() { return stateBefore; }
    public WorldState getStateAfter() { return stateAfter; }
    public boolean isSuccess() { return success; }
    public long getDurationMs() { return durationMs; }
    public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }
}
