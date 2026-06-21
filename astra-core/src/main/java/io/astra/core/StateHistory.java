package io.astra.core;

import io.astra.api.Snapshot;
import io.astra.api.WorldState;
import io.astra.api.event.AstraEvent;
import io.astra.api.event.AstraEventType;
import io.astra.api.event.EventBus;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Bounded-depth snapshot history for {@link WorldState} rollback support.
 * Publishes events on snapshot and rollback operations.
 */
public class StateHistory {
    private final Deque<Snapshot> snapshots = new ArrayDeque<>();
    private final int maxDepth;
    private final EventBus eventBus;

    public StateHistory(int maxDepth, EventBus eventBus) {
        this.maxDepth = maxDepth;
        this.eventBus = eventBus;
    }

    public void snapshot(WorldState state) {
        snapshots.push(state.snapshot());
        if (snapshots.size() > maxDepth) {
            snapshots.removeLast();
        }
        eventBus.publish(AstraEvent.of(AstraEventType.STATE_SNAPSHOT,
            "depth", String.valueOf(snapshots.size())));
    }

    public WorldState rollback() {
        if (snapshots.isEmpty()) {
            throw new IllegalStateException("No snapshots to rollback to");
        }
        Snapshot snap = snapshots.pop();
        WorldState restored = snap.restore();
        eventBus.publish(AstraEvent.of(AstraEventType.STATE_ROLLBACK,
            "depth", String.valueOf(snapshots.size())));
        return restored;
    }

    public int depth() { return snapshots.size(); }

    public void clear() { snapshots.clear(); }
}
