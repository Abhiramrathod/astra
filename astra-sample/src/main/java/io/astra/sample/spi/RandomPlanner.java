package io.astra.sample.spi;

import io.astra.api.*;
import io.astra.planner.DefaultPlan;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * {@link Planner} that randomly selects applicable actions until a goal is reached.
 */
public class RandomPlanner implements Planner {
    private final Random rand = new Random();

    @Override
    public Plan plan(WorldState state, GoalInfo goal,
                     List<ActionInfo> actions) {
        List<ActionInfo> plan = new ArrayList<>();
        WorldState current = state;
        for (int i = 0; i < 100; i++) {
            final WorldState stepState = current;
            List<ActionInfo> applicable = actions.stream()
                .filter(a -> stepState.matches(a.getPreconditions()))
                .toList();
            if (applicable.isEmpty()) break;
            ActionInfo chosen = applicable.get(rand.nextInt(applicable.size()));
            plan.add(chosen);
            for (var e : chosen.getEffects().entrySet())
                current = current.set(e.getKey(), e.getValue());
            if (current.matches(goal.getCondition())) break;
        }
        double cost = plan.stream().mapToDouble(ActionInfo::getCost).sum();
        return new DefaultPlan(plan, cost);
    }
}
