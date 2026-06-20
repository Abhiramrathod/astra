package io.astra.api;

import io.astra.api.config.AstraConfig;
import io.astra.api.event.EventBus;
import io.astra.api.interceptor.InterceptorChain;
import io.astra.api.result.ExecutionResult;
import java.util.concurrent.CompletableFuture;

public interface Astra {
    Plan plan(String goalName, WorldState initialState);
    WorldState execute(String goalName, WorldState initialState);
    ExecutionResult executeWithResult(String goalName, WorldState initialState);
    CompletableFuture<ExecutionResult> executeAsync(String goalName, WorldState initialState);

    EventBus getEventBus();
    InterceptorChain getInterceptorChain();
    AstraConfig getConfig();
}
