package io.astra.telemetry;

import io.astra.api.*;
import io.astra.api.interceptor.ActionInterceptor;
import io.astra.api.telemetry.Tracer;
import io.astra.api.telemetry.MetricsCollector;

/**
 * {@link ActionInterceptor} that records tracing and metrics for every action lifecycle event.
 */
public class TelemetryInterceptor implements ActionInterceptor {
    private final Tracer tracer;
    private final MetricsCollector metrics;

    public TelemetryInterceptor(Tracer tracer, MetricsCollector metrics) {
        this.tracer = tracer;
        this.metrics = metrics;
    }

    @Override
    public void beforeAction(ActionInfo action, WorldState state) {
        tracer.traceActionStart(action.getName(), state);
        metrics.incrementCounter("action.started", "action=" + action.getName());
    }

    @Override
    public void afterAction(ActionInfo action, WorldState state, WorldState newState) {
        tracer.traceActionComplete(action.getName(), newState, 0);
        metrics.incrementCounter("action.completed", "action=" + action.getName());
    }

    @Override
    public void onError(ActionInfo action, WorldState state, Throwable error) {
        tracer.traceActionFailed(action.getName(), error.getMessage());
        metrics.incrementCounter("action.failed", "action=" + action.getName());
    }
}
