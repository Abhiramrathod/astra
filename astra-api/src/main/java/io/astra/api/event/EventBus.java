package io.astra.api.event;

public interface EventBus {
    void publish(AstraEvent event);
    void subscribe(AstraEventListener listener);
    void unsubscribe(AstraEventListener listener);
}
