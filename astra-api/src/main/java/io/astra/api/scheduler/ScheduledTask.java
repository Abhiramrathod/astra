package io.astra.api.scheduler;

/**
 * Handle for a scheduled task that can be cancelled.
 */
public interface ScheduledTask {
    String getId();
    boolean isCancelled();
    boolean cancel();
}
