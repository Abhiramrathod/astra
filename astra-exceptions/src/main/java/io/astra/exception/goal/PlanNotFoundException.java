package io.astra.exception.goal;

import io.astra.exception.AstraException;

/** Thrown when no plan can be found for a given goal and state. */
public class PlanNotFoundException extends AstraException {
    public PlanNotFoundException(String goalName, Object state) {
        super("PLAN_NOT_FOUND", "No plan found for goal '" + goalName + "' from state: " + state);
    }
}
