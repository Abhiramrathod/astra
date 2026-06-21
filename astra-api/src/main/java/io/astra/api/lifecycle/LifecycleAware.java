package io.astra.api.lifecycle;

/**
 * Interface for components with init/destroy/error lifecycle hooks.
 */
public interface LifecycleAware {
    default void onInit() {}
    default void onDestroy() {}
    default void onError(Throwable error) {}
}
