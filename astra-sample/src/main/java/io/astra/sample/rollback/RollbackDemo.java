package io.astra.sample.rollback;

import io.astra.api.*;
import io.astra.core.StateHistory;
import io.astra.event.DefaultEventBus;

/** Demonstrates {@link StateHistory} for snapshot-based state rollback. */
public class RollbackDemo {
    public static void run() {
        System.out.println("\n=== Rollback: StateHistory Snapshots ===");
        StateHistory history = new StateHistory(5, new DefaultEventBus());
        WorldState s1 = WorldStates.of("step", "1");
        WorldState s2 = s1.set("step", "2");
        WorldState s3 = s2.set("step", "3");

        history.snapshot(s1);
        history.snapshot(s2);
        history.snapshot(s3);
        System.out.println("  Current: " + s3);

        WorldState restored = history.rollback();
        System.out.println("  After rollback: " + restored);
        System.out.println("  Rollback demo complete");
    }
}
