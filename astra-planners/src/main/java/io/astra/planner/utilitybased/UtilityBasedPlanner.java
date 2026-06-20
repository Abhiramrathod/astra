package io.astra.planner.utilitybased;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.DefaultPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.*;

public class UtilityBasedPlanner implements Planner {
    private static final Logger log = LoggerFactory.getLogger(UtilityBasedPlanner.class);
    private final AstraConfig config;

    public UtilityBasedPlanner(AstraConfig config) {
        this.config = config;
    }

    @Override
    public Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions) {
        int maxSteps = config != null
            ? config.getOrDefault("astra.planner.maxIterations", Integer.class, 20)
            : 20;

        log.debug("Utility planning from {} for up to {} steps", currentState, maxSteps);
        List<ActionInfo> planActions = new ArrayList<>();
        Set<String> usedActionNames = new HashSet<>();
        WorldState state = currentState;

        for (int i = 0; i < maxSteps; i++) {
            final WorldState current = state;
            List<ActionInfo> applicable = actions.stream()
                .filter(a -> current.matches(a.getPreconditions()))
                .filter(a -> !usedActionNames.contains(a.getName()))
                .collect(Collectors.toList());

            if (applicable.isEmpty()) {
                log.debug("No applicable actions at step {}", i);
                break;
            }

            ActionInfo best = applicable.stream()
                .max(Comparator.comparingDouble(a -> getUtility(a, current)))
                .orElse(null);

            if (best == null) break;

            log.debug("Step {}: picked '{}' (utility={})", i, best.getName(), getUtility(best, state));
            usedActionNames.add(best.getName());
            state = applyEffects(state, best);
            planActions.add(best);
        }

        double totalCost = planActions.stream().mapToDouble(ActionInfo::getCost).sum();
        log.info("Utility plan: {} actions, total cost={}", planActions.size(), totalCost);
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
