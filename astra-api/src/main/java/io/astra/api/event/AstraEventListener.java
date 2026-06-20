package io.astra.api.event;

@FunctionalInterface
public interface AstraEventListener {
    void onEvent(AstraEvent event);
}
