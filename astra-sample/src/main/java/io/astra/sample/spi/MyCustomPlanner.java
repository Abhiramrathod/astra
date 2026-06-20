package io.astra.sample.spi;

import io.astra.api.*;
import io.astra.planner.DefaultPlan;
import java.util.List;

public class MyCustomPlanner implements Planner {
    @Override
    public Plan plan(WorldState currentState,
                     GoalInfo goal,
                     List<ActionInfo> actions) {
        return new DefaultPlan(actions, actions.stream()
            .mapToDouble(ActionInfo::getCost).sum());
    }
}
