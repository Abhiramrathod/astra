package io.astra.api.agentbus;

/**
 * Message bus for inter-agent communication.
 */
public interface AgentBus {
    void send(AgentMessage message);
    void subscribe(String agentName, MessageHandler handler);
    void unsubscribe(String agentName);
    void broadcast(String sender, String topic, Object payload);
}
