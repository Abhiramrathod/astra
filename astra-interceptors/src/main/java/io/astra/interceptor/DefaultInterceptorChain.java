package io.astra.interceptor;

import io.astra.api.*;
import io.astra.api.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;

public class DefaultInterceptorChain implements InterceptorChain {
    private static final Logger log = LoggerFactory.getLogger(DefaultInterceptorChain.class);
    private final List<ActionInterceptor> interceptors = new CopyOnWriteArrayList<>();

    @Override
    public void addInterceptor(ActionInterceptor interceptor) {
        interceptors.add(interceptor);
        log.debug("Interceptor registered: {}", interceptor.getClass().getName());
    }

    @Override
    public void removeInterceptor(ActionInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    @Override
    public List<ActionInterceptor> getInterceptors() {
        return List.copyOf(interceptors);
    }

    public void beforeAction(ActionInfo action, WorldState state) {
        for (ActionInterceptor interceptor : interceptors) {
            try {
                interceptor.beforeAction(action, state);
            } catch (Exception e) {
                log.error("Interceptor beforeAction failed: {}", interceptor.getClass().getName(), e);
            }
        }
    }

    public void afterAction(ActionInfo action, WorldState state, WorldState newState) {
        for (ActionInterceptor interceptor : interceptors) {
            try {
                interceptor.afterAction(action, state, newState);
            } catch (Exception e) {
                log.error("Interceptor afterAction failed: {}", interceptor.getClass().getName(), e);
            }
        }
    }

    public void onError(ActionInfo action, WorldState state, Throwable error) {
        for (ActionInterceptor interceptor : interceptors) {
            try {
                interceptor.onError(action, state, error);
            } catch (Exception e) {
                log.error("Interceptor onError failed: {}", interceptor.getClass().getName(), e);
            }
        }
    }
}
