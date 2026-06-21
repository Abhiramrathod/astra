package io.astra.telemetry;

import io.astra.api.telemetry.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory implementation of {@link MetricsCollector} backed by {@link ConcurrentHashMap}.
 */
public class SimpleMetricsCollector implements MetricsCollector {
    private static final Logger log = LoggerFactory.getLogger("astra.metrics");
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> gauges = new ConcurrentHashMap<>();

    @Override
    public void incrementCounter(String name, String... tags) {
        String key = tags.length > 0 ? name + ":" + String.join(",", tags) : name;
        counters.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
        log.debug("[METRIC] counter {} = {}", key, counters.get(key).get());
    }

    @Override
    public void recordGauge(String name, double value, String... tags) {
        String key = tags.length > 0 ? name + ":" + String.join(",", tags) : name;
        gauges.put(key, new AtomicLong((long) value));
        log.debug("[METRIC] gauge {} = {}", key, value);
    }

    @Override
    public void recordTimer(String name, long durationMs, String... tags) {
        String key = tags.length > 0 ? name + ":" + String.join(",", tags) : name;
        log.info("[METRIC] timer {} = {}ms", key, durationMs);
    }

    public long getCounter(String name, String... tags) {
        String key = tags.length > 0 ? name + ":" + String.join(",", tags) : name;
        AtomicLong val = counters.get(key);
        return val == null ? 0 : val.get();
    }
}
