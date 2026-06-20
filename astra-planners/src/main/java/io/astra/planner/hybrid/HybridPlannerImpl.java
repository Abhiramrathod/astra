package io.astra.planner.hybrid;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.DefaultPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.*;

public class HybridPlannerImpl implements GoapPlanner {
    private static final Logger log = LoggerFactory.getLogger(HybridPlannerImpl.class);
    private final AstraConfig config;

    public HybridPlannerImpl(AstraConfig config) {
        this.config = config;
    }

    @Override
    public Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions) {
        int maxSteps = config != null
            ? config.getOrDefault("astra.planner.maxIterations", Integer.class, 100)
            : 100;

        log.debug("Hybrid planning for goal '{}' from {} (max {} steps)",
            goal.getName(), currentState, maxSteps);
        List<ActionInfo> planActions = new ArrayList<>();
        Set<String> usedActionNames = new HashSet<>();
        WorldState state = currentState;

        for (int i = 0; i < maxSteps; i++) {
            if (state.matches(goal.getCondition())) {
                log.info("Goal '{}' satisfied after {} steps", goal.getName(), i);
                break;
            }

            final WorldState current = state;
            List<ActionInfo> applicable = actions.stream()
                .filter(a -> current.matches(a.getPreconditions()))
                .collect(Collectors.toList());

            if (applicable.isEmpty()) {
                log.warn("No applicable actions at step {} before goal reached", i);
                break;
            }

            ActionInfo best = applicable.stream()
                .filter(a -> !usedActionNames.contains(a.getName()))
                .max(Comparator.comparingDouble(a -> getUtility(a, current)))
                .orElse(null);

            if (best == null) {
                log.warn("All applicable actions already used, stopping");
                break;
            }

            log.debug("Step {}: '{}' (utility={})", i, best.getName(), getUtility(best, state));
            usedActionNames.add(best.getName());
            state = applyEffects(state, best);
            planActions.add(best);
        }

        if (!state.matches(goal.getCondition())) {
            log.warn("Hybrid plan did not reach goal '{}'", goal.getName());
            return new DefaultPlan(List.of(), 0);
        }

        double totalCost = planActions.stream().mapToDouble(ActionInfo::getCost).sum();
        log.info("Hybrid plan: {} actions, cost={}", planActions.size(), totalCost);
        return new DefaultPlan(planActions, totalCost);
    }

    private double getUtility(ActionInfo action, WorldState state) {
        double u = action.computeUtility(state);
        return action.getCost() > 0 ? u / action.getCost() : u;
    }

    private WorldState applyEffects(WorldState state, ActionInfo action) {
        WorldState result = state;
        for (var entry : action.getEffects().entrySet()) {
            result = result.set(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
