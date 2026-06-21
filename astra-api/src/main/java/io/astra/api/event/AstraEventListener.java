package io.astra.api.event;

/**
 * Functional interface for consuming {@link AstraEvent}s.
 */
@FunctionalInterface
public interface AstraEventListener {
    void onEvent(AstraEvent event);
}
