package io.astra.api.telemetry;

/**
 * Collector for counters, gauges, and timer metrics.
 */
public interface MetricsCollector {
    void incrementCounter(String name, String... tags);
    void recordGauge(String name, double value, String... tags);
    void recordTimer(String name, long durationMs, String... tags);
}
