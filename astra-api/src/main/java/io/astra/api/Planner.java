package io.astra.api;

import java.util.List;

/** Strategy interface for generating a plan from a goal and available actions. */
public interface Planner {
    Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions);
}
