package io.astra.event;

import io.astra.api.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;

/** Default {@link EventBus} implementation using a {@link CopyOnWriteArrayList} of listeners. */
public class DefaultEventBus implements EventBus {
    private static final Logger log = LoggerFactory.getLogger(DefaultEventBus.class);
    private final List<AstraEventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(AstraEvent event) {
        log.debug("Event: {} at {}", event.type(), event.timestamp());
        for (AstraEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("AstraEventListener threw exception for event {}", event.type(), e);
            }
        }
    }

    @Override
    public void subscribe(AstraEventListener listener) {
        listeners.add(listener);
        log.debug("AstraEventListener subscribed: {}", listener.getClass().getName());
    }

    @Override
    public void unsubscribe(AstraEventListener listener) {
        listeners.remove(listener);
    }
}
