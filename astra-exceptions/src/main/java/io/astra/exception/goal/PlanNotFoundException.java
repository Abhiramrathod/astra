package io.astra.exception.goal;

import io.astra.exception.AstraException;

public class PlanNotFoundException extends AstraException {
    public PlanNotFoundException(String goalName, Object state) {
        super("PLAN_NOT_FOUND", "No plan found for goal '" + goalName + "' from state: " + state);
    }
}
