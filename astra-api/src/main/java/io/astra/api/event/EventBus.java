package io.astra.api.event;

/**
 * Publish/subscribe bus for {@link AstraEvent}s.
 */
public interface EventBus {
    void publish(AstraEvent event);
    void subscribe(AstraEventListener listener);
    void unsubscribe(AstraEventListener listener);
}
