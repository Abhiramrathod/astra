package io.astra.api.policy;

import io.astra.api.WorldState;

/**
 * Checks whether an action is allowed under a given policy.
 */
public interface PolicyChecker {
    boolean check(String actionName, String policy, WorldState state);
}
