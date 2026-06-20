package io.astra.api;

import java.util.List;

public interface Planner {
    Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions);
}
