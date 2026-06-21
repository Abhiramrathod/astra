package io.astra.api.scheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service for scheduling one-shot, repeating, and cron tasks.
 */
public interface SchedulerService {
    ScheduledTask schedule(String actionName, Runnable action, Duration delay);
    ScheduledTask scheduleRepeating(String actionName, Runnable action, Duration interval, long times);
    ScheduledTask scheduleCron(String actionName, Runnable action, String cronExpression);
    void cancel(String taskId);
    void shutdown();
}
