package io.astra.planner.costbased;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.DefaultPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/** A* search planner that finds a minimal-cost action sequence to reach a goal. */
public class CostBasedPlanner implements Planner {
    private static final Logger log = LoggerFactory.getLogger(CostBasedPlanner.class);
    private final AstraConfig config;

    public CostBasedPlanner(AstraConfig config) {
        this.config = config;
    }

    public CostBasedPlanner() {
        this.config = null;
    }

    @Override
    public Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions) {
        log.debug("Planning for goal '{}' from state {}", goal.getName(), currentState);
        validateState(currentState);
        validateActions(actions);

        PriorityQueue<SearchNode> open = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.costSoFar + heuristic(n.state, goal))
        );
        Set<WorldState> closed = new HashSet<>();
        int maxIterations = config != null
            ? config.getOrDefault("astra.planner.maxIterations", Integer.class, 10000)
            : 10000;
        int iterations = 0;

        open.add(new SearchNode(currentState, null, null, 0));

        while (!open.isEmpty() && iterations++ < maxIterations) {
            SearchNode current = open.poll();
            if (current == null) break;
            if (closed.contains(current.state)) continue;
            closed.add(current.state);

            if (current.state.matches(goal.getCondition())) {
                Plan plan = buildPlan(current);
                log.info("Plan found for goal '{}': {} steps, cost={}",
                    goal.getName(), plan.getActions().size(), plan.getTotalCost());
                return plan;
            }

            for (ActionInfo action : actions) {
                if (!current.state.matches(action.getPreconditions())) continue;
                WorldState nextState = applyEffects(current.state, action);
                if (closed.contains(nextState)) continue;
                double newCost = current.costSoFar + action.getCost();
                open.add(new SearchNode(nextState, current, action, newCost));
            }
        }

        log.warn("No plan found for goal '{}' from state {} (iterations={})",
            goal.getName(), currentState, iterations);
        return new DefaultPlan(List.of(), 0);
    }

    private void validateState(WorldState state) {
        Objects.requireNonNull(state, "WorldState must not be null");
    }

    private void validateActions(List<ActionInfo> actions) {
        Objects.requireNonNull(actions, "Actions list must not be null");
        if (actions.isEmpty()) {
            log.warn("Planning with empty actions list");
        }
    }

    private WorldState applyEffects(WorldState state, ActionInfo action) {
        WorldState result = state;
        for (var entry : action.getEffects().entrySet()) {
            result = result.set(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private double heuristic(WorldState state, GoalInfo goal) {
        String mode = config != null
            ? config.getOrDefault("astra.planner.heuristic", String.class, "unmatched")
            : "unmatched";
        return switch (mode) {
            case "zero" -> 0;
            case "unmatched" -> goal.getCondition().entrySet().stream()
                .filter(e -> !state.get(e.getKey()).map(v -> v.equals(e.getValue())).orElse(false))
                .count();
            default -> 0;
        };
    }

    private Plan buildPlan(SearchNode node) {
        List<ActionInfo> actions = new ArrayList<>();
        SearchNode current = node;
        while (current.action != null) {
            actions.add(current.action);
            current = current.parent;
        }
        Collections.reverse(actions);
        double totalCost = actions.stream().mapToDouble(a -> a.getCost()).sum();
        return new DefaultPlan(actions, totalCost);
    }
}
