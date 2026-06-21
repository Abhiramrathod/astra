package io.astra.exception.goal;

import io.astra.exception.AstraException;

/** Thrown when a goal with the same name already exists. */
public class DuplicateGoalException extends AstraException {
    private final String goalName;

    public DuplicateGoalException(String goalName) {
        super("DUPLICATE_GOAL", "Duplicate goal: " + goalName);
        this.goalName = goalName;
    }

    public String getGoalName() { return goalName; }
}
