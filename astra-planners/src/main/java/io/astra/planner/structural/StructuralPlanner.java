package io.astra.planner.structural;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.DefaultPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.*;

public class StructuralPlanner implements Planner {
    private static final Logger log = LoggerFactory.getLogger(StructuralPlanner.class);
    private final AstraConfig config;
    private final Map<String, CompoundTaskDef> compoundTasks;
    private final Map<String, ActionInfo> primitives;

    public StructuralPlanner(AstraConfig config,
                             Map<String, CompoundTaskDef> compoundTasks,
                             List<ActionInfo> allActions) {
        this.config = config;
        this.compoundTasks = compoundTasks;
        this.primitives = new LinkedHashMap<>();
        for (ActionInfo a : allActions) {
            primitives.put(a.getName(), a);
        }
    }

    @Override
    public Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions) {
        String rootTaskName = goal.getName();

        if (!compoundTasks.containsKey(rootTaskName)) {
            log.warn("No compound task named '{}' — falling back to empty plan", rootTaskName);
            return new DefaultPlan(List.of(), 0);
        }

        int maxDecomp = config != null
            ? config.getOrDefault("astra.planner.maxIterations", Integer.class, 200)
            : 200;

        log.debug("Structural planning: decompose '{}' from {}", rootTaskName, currentState);
        List<ActionInfo> planActions = decompose(rootTaskName, currentState, 0, maxDecomp);

        if (planActions == null) {
            log.warn("Structural decomposition failed for '{}'", rootTaskName);
            return new DefaultPlan(List.of(), 0);
        }

        double totalCost = planActions.stream().mapToDouble(ActionInfo::getCost).sum();
        log.info("Structural plan for '{}': {} primitive actions, cost={}",
            rootTaskName, planActions.size(), totalCost);
        return new DefaultPlan(planActions, totalCost);
    }

    private List<ActionInfo> decompose(String taskName, WorldState state,
                                        int depth, int maxDepth) {
        if (depth > maxDepth) return null;

        ActionInfo primitive = primitives.get(taskName);
        if (primitive != null) {
            if (!state.matches(primitive.getPreconditions())) {
                log.debug("Primitive '{}' preconditions not met: {}", taskName, state);
                return null;
            }
            log.debug("  Primitive: {}", taskName);
            return new ArrayList<>(List.of(primitive));
        }

        CompoundTaskDef compound = compoundTasks.get(taskName);
        if (compound == null) {
            log.warn("Unknown task: {}", taskName);
            return null;
        }

        for (DecompositionDef decomp : compound.getDecompositions()) {
            log.debug("  Trying decomposition '{}' preconditions={} state={}", decomp.getName(), decomp.getPreconditions(), state);
            if (!state.matches(decomp.getPreconditions())) {
                log.debug("  Decomposition '{}' preconditions not met, trying next", decomp.getName());
                continue;
            }

            log.debug("  Decompose '{}' via '{}' -> {}", taskName, decomp.getName(), decomp.getSubtasks());
            List<ActionInfo> result = new ArrayList<>();
            WorldState currentState = state;
            boolean success = true;

            for (String subtask : decomp.getSubtasks()) {
                List<ActionInfo> subPlan = decompose(subtask.trim(), currentState, depth + 1, maxDepth);
                if (subPlan == null) {
                    log.debug("  Subtask '{}' failed, backtracking", subtask);
                    success = false;
                    break;
                }
                result.addAll(subPlan);
                for (ActionInfo a : subPlan) {
                    for (var entry : a.getEffects().entrySet()) {
                        currentState = currentState.set(entry.getKey(), entry.getValue());
                    }
                }
            }

            if (success) {
                return result;
            }
        }

        return null;
    }
}
