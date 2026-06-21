package io.astra.api.agentbus;

/**
 * Functional interface for handling incoming {@link AgentMessage}s.
 */
@FunctionalInterface
public interface MessageHandler {
    void onMessage(AgentMessage message);
}
