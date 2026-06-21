package io.astra.agentbus;

import io.astra.api.agentbus.AgentBus;
import io.astra.api.agentbus.AgentMessage;
import io.astra.api.agentbus.MessageHandler;
import io.astra.api.event.AstraEvent;
import io.astra.api.event.AstraEventType;
import io.astra.api.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link AgentBus} with in-memory subscriber management.
 */
public class DefaultAgentBus implements AgentBus {
    private static final Logger log = LoggerFactory.getLogger(DefaultAgentBus.class);
    private final Map<String, CopyOnWriteArrayList<MessageHandler>> subscribers = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    public DefaultAgentBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void send(AgentMessage message) {
        log.debug("AgentBus: {} -> {} [{}]", message.getSender(), message.getRecipient(), message.getTopic());
        var handlers = subscribers.get(message.getRecipient());
        if (handlers != null) {
            for (MessageHandler h : handlers) {
                try {
                    h.onMessage(message);
                } catch (Exception e) {
                    log.error("Handler error delivering message {}: {}", message.getId(), e.getMessage());
                }
            }
        }
        eventBus.publish(AstraEvent.of(AstraEventType.AGENT_MESSAGE,
            "id", message.getId(), "sender", message.getSender(),
            "recipient", message.getRecipient(), "topic", message.getTopic()));
    }

    @Override
    public void subscribe(String agentName, MessageHandler handler) {
        subscribers.computeIfAbsent(agentName, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public void unsubscribe(String agentName) {
        subscribers.remove(agentName);
    }

    @Override
    public void broadcast(String sender, String topic, Object payload) {
        for (String recipient : subscribers.keySet()) {
            send(new AgentMessage(sender, recipient, topic, payload));
        }
    }
}
