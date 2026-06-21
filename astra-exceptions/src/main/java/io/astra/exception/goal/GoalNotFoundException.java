package io.astra.exception.goal;

import io.astra.exception.AstraException;

/** Thrown when a requested goal is not found. */
public class GoalNotFoundException extends AstraException {
    private final String goalName;

    public GoalNotFoundException(String goalName) {
        super("GOAL_NOT_FOUND", "Unknown goal: " + goalName);
        this.goalName = goalName;
    }

    public String getGoalName() { return goalName; }
}
