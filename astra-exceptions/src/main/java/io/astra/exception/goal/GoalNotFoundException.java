package io.astra.exception.goal;

import io.astra.exception.AstraException;

public class GoalNotFoundException extends AstraException {
    private final String goalName;

    public GoalNotFoundException(String goalName) {
        super("GOAL_NOT_FOUND", "Unknown goal: " + goalName);
        this.goalName = goalName;
    }

    public String getGoalName() { return goalName; }
}
