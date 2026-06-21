package io.astra.api.agentbus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A message sent between agents over the {@link AgentBus}.
 */
public class AgentMessage {
    private final String id;
    private final String sender;
    private final String recipient;
    private final String topic;
    private final Object payload;
    private final Instant timestamp;

    public AgentMessage(String sender, String recipient, String topic, Object payload) {
        this.id = UUID.randomUUID().toString();
        this.sender = Objects.requireNonNull(sender);
        this.recipient = Objects.requireNonNull(recipient);
        this.topic = Objects.requireNonNull(topic);
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getTopic() { return topic; }
    public Object getPayload() { return payload; }
    public Instant getTimestamp() { return timestamp; }
}
