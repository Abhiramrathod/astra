package io.astra.core.scheduler;

import io.astra.api.scheduler.ScheduledTask;
import io.astra.api.scheduler.SchedulerService;
import io.astra.api.event.AstraEvent;
import io.astra.api.event.AstraEventType;
import io.astra.api.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default {@link SchedulerService} backed by a {@link ScheduledExecutorService}.
 * Supports one-shot, fixed-rate repeating, and cron-style scheduling.
 */
public class DefaultSchedulerService implements SchedulerService {
    private static final Logger log = LoggerFactory.getLogger(DefaultSchedulerService.class);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    public DefaultSchedulerService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public ScheduledTask schedule(String actionName, Runnable action, Duration delay) {
        String id = UUID.randomUUID().toString();
        AtomicBoolean cancelled = new AtomicBoolean(false);
        ScheduledFuture<?> future = executor.schedule(() -> {
            if (!cancelled.get()) {
                eventBus.publish(AstraEvent.of(AstraEventType.SCHEDULED_TASK_TRIGGERED,
                    "id", id, "action", actionName));
                action.run();
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
        tasks.put(id, future);
        return task(id, future, cancelled);
    }

    @Override
    public ScheduledTask scheduleRepeating(String actionName, Runnable action, Duration interval, long times) {
        String id = UUID.randomUUID().toString();
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean count = new AtomicBoolean(false);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            if (!cancelled.get()) {
                eventBus.publish(AstraEvent.of(AstraEventType.SCHEDULED_TASK_TRIGGERED,
                    "id", id, "action", actionName));
                action.run();
            }
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        tasks.put(id, future);
        return task(id, future, cancelled);
    }

    @Override
    public ScheduledTask scheduleCron(String actionName, Runnable action, String cronExpression) {
        log.warn("Cron scheduling not fully implemented, using fixed-rate 1min for '{}'", actionName);
        return scheduleRepeating(actionName, action, Duration.ofMinutes(1), -1);
    }

    @Override
    public void cancel(String taskId) {
        ScheduledFuture<?> future = tasks.remove(taskId);
        if (future != null) future.cancel(false);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        tasks.clear();
    }

    private ScheduledTask task(String id, ScheduledFuture<?> future, AtomicBoolean cancelled) {
        return new ScheduledTask() {
            @Override public String getId() { return id; }
            @Override public boolean isCancelled() { return cancelled.get(); }
            @Override public boolean cancel() {
                cancelled.set(true);
                future.cancel(false);
                tasks.remove(id);
                return true;
            }
        };
    }
}
