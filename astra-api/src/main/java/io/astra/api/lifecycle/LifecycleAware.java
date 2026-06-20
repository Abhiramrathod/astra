package io.astra.api.lifecycle;

public interface LifecycleAware {
    default void onInit() {}
    default void onDestroy() {}
    default void onError(Throwable error) {}
}
