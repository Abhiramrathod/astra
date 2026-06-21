package io.astra.api;

import io.astra.api.config.AstraConfig;
import io.astra.api.event.EventBus;
import io.astra.api.interceptor.InterceptorChain;
import io.astra.api.result.ExecutionResult;
import java.util.concurrent.CompletableFuture;

/** Central interface for planning and executing goals against a world state. */
public interface Astra {
    Plan plan(String goalName, WorldState initialState);
    WorldState execute(String goalName, WorldState initialState);
    ExecutionResult executeWithResult(String goalName, WorldState initialState);
    CompletableFuture<ExecutionResult> executeAsync(String goalName, WorldState initialState);

    ExecutionResult executeQuery(String goalName, WorldState initialState, String query);
    default WorldState executeQuerySimple(String goalName, WorldState initialState, String query) {
        return executeQuery(goalName, initialState, query).getFinalState();
    }

    ExecutionResult executeQuery(String query);
    default WorldState executeQuerySimple(String query) {
        return executeQuery(query).getFinalState();
    }

    EventBus getEventBus();
    InterceptorChain getInterceptorChain();
    AstraConfig getConfig();
}
