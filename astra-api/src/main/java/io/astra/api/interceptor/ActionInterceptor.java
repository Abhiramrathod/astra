package io.astra.api.interceptor;

import io.astra.api.*;

/**
 * Interceptor hook for actions with before/after/error callbacks.
 */
public interface ActionInterceptor {
    default void beforeAction(ActionInfo action, WorldState state) {}
    default void afterAction(ActionInfo action, WorldState state, WorldState newState) {}
    default void onError(ActionInfo action, WorldState state, Throwable error) {}
}
