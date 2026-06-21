package io.astra.core.composite;

import io.astra.api.*;
import io.astra.api.composite.CompositeAgent;
import io.astra.api.config.AstraConfig;
import io.astra.api.event.EventBus;
import io.astra.api.interceptor.InterceptorChain;
import io.astra.api.result.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link CompositeAgent} that manages named sub-agents and
 * delegates goal execution to the first capable sub-agent.
 */
public class DefaultCompositeAgent implements CompositeAgent {
    private static final Logger log = LoggerFactory.getLogger(DefaultCompositeAgent.class);
    private final Map<String, Astra> subAgents = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    private final InterceptorChain interceptorChain;
    private final AstraConfig config;

    public DefaultCompositeAgent(EventBus eventBus, InterceptorChain interceptorChain, AstraConfig config) {
        this.eventBus = eventBus;
        this.interceptorChain = interceptorChain;
        this.config = config;
    }

    @Override
    public void registerSubAgent(String name, Astra agent) {
        subAgents.put(name, agent);
        log.info("Sub-agent '{}' registered in composite", name);
    }

    @Override
    public void removeSubAgent(String name) {
        subAgents.remove(name);
    }

    @Override
    public List<String> getSubAgentNames() {
        return List.copyOf(subAgents.keySet());
    }

    @Override
    public ExecutionResult executeQuery(String query, String preferredAgent) {
        Astra agent = subAgents.get(preferredAgent);
        if (agent == null) throw new IllegalArgumentException("No sub-agent named: " + preferredAgent);
        return agent.executeQuery(query);
    }

    @Override
    public Plan plan(String goalName, WorldState initialState) {
        for (Astra agent : subAgents.values()) {
            try { return agent.plan(goalName, initialState); } catch (Exception ignored) {}
        }
        throw new IllegalStateException("No sub-agent can plan for: " + goalName);
    }

    @Override
    public WorldState execute(String goalName, WorldState initialState) {
        return executeWithResult(goalName, initialState).getFinalState();
    }

    @Override
    public ExecutionResult executeWithResult(String goalName, WorldState initialState) {
        for (Astra agent : subAgents.values()) {
            try { return agent.executeWithResult(goalName, initialState); } catch (Exception ignored) {}
        }
        throw new IllegalStateException("No sub-agent can execute: " + goalName);
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(String goalName, WorldState initialState) {
        return CompletableFuture.supplyAsync(() -> executeWithResult(goalName, initialState));
    }

    @Override
    public ExecutionResult executeQuery(String goalName, WorldState initialState, String query) {
        for (Astra agent : subAgents.values()) {
            try { return agent.executeQuery(goalName, initialState, query); } catch (Exception ignored) {}
        }
        throw new IllegalStateException("No sub-agent handles query: " + goalName);
    }

    @Override
    public ExecutionResult executeQuery(String query) {
        for (Astra agent : subAgents.values()) {
            try { return agent.executeQuery(query); } catch (Exception ignored) {}
        }
        throw new IllegalStateException("No sub-agent can handle query: " + query);
    }

    @Override
    public EventBus getEventBus() { return eventBus; }

    @Override
    public InterceptorChain getInterceptorChain() { return interceptorChain; }

    @Override
    public AstraConfig getConfig() { return config; }
}
