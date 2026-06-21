package io.astra.api.lifecycle;

/**
 * Lifecycle states an agent can transition through.
 */
public enum AgentState {
    CREATED,
    INITIALIZED,
    ACTIVE,
    DESTROYED,
    ERROR
}
