package io.astra.api;

import io.astra.api.WorldState;

/** A restore point that returns a previous {@link WorldState}. */
@FunctionalInterface
public interface Snapshot {
    WorldState restore();
}
