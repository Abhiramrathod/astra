package io.astra.lifecycle;

import io.astra.api.lifecycle.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.*;

/** Manages lifecycle state transitions for agent instances. */
public class LifecycleManager {
    private static final Logger log = LoggerFactory.getLogger(LifecycleManager.class);
    private final Map<Object, AgentState> agentStates = new ConcurrentHashMap<>();

    public void init(Object agentInstance) {
        AgentState previous = agentStates.put(agentInstance, AgentState.INITIALIZED);
        if (previous != null) {
            log.warn("Agent {} re-initialized", agentInstance.getClass().getName());
        }
        if (agentInstance instanceof LifecycleAware aware) {
            try {
                aware.onInit();
            } catch (Exception e) {
                agentStates.put(agentInstance, AgentState.ERROR);
                throw e;
            }
        }
        agentStates.put(agentInstance, AgentState.ACTIVE);
        log.info("Agent {} initialized and active", agentInstance.getClass().getName());
    }

    public void destroy(Object agentInstance) {
        agentStates.put(agentInstance, AgentState.DESTROYED);
        if (agentInstance instanceof LifecycleAware aware) {
            try {
                aware.onDestroy();
            } catch (Exception e) {
                log.error("Error destroying agent {}", agentInstance.getClass().getName(), e);
            }
        }
        agentStates.remove(agentInstance);
    }

    public void onError(Object agentInstance, Throwable error) {
        agentStates.put(agentInstance, AgentState.ERROR);
        if (agentInstance instanceof LifecycleAware aware) {
            try {
                aware.onError(error);
            } catch (Exception e) {
                log.error("Error in agent error handler {}", agentInstance.getClass().getName(), e);
            }
        }
    }

    public AgentState getState(Object agentInstance) {
        return agentStates.getOrDefault(agentInstance, AgentState.CREATED);
    }
}
