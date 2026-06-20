package io.astra.api;

import java.util.List;

public interface GoapPlanner {
    Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions);
}
