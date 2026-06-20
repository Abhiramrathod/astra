package io.astra.scanner;

import io.astra.annotation.*;
import io.astra.annotation.action.*;
import io.astra.annotation.fact.*;
import io.astra.annotation.goal.*;
import io.astra.annotation.htn.*;
import io.astra.api.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

public final class AgentScanner {
    private AgentScanner() {}

    public static List<ActionInfo> scanActions(Object agentInstance) {
        List<ActionInfo> actions = new ArrayList<>();
        Class<?> clazz = agentInstance.getClass();
        for (Method method : clazz.getMethods()) {
            Action[] annotations = method.getAnnotationsByType(Action.class);
            for (Action ann : annotations) {
                String name = ann.name().isEmpty() ? method.getName() : ann.name();
                String description = ann.description();
                Map<String, String> preconds = factsToMap(ann.preconditions());
                Map<String, String> effects = factsToMap(ann.effects());
                float cost = ann.cost();
                float utility = ann.utility();
                String utilityMethod = ann.utilityMethod();

                Method utilityFn = null;
                if (!utilityMethod.isEmpty()) {
                    try {
                        utilityFn = agentInstance.getClass().getMethod(utilityMethod, WorldState.class);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalArgumentException(
                            "Utility method '" + utilityMethod + "' not found on " + clazz.getSimpleName()
                            + " — must take a single WorldState parameter", e);
                    }
                }
                final Method utilityFnFinal = utilityFn;

                actions.add(new ActionInfo() {
                    @Override
                    public String getName() { return name; }
                    @Override
                    public String getDescription() { return description; }
                    @Override
                    public Map<String, String> getPreconditions() { return preconds; }
                    @Override
                    public Map<String, String> getEffects() { return effects; }
                    @Override
                    public float getCost() { return cost; }
                    @Override
                    public float getUtility() { return utility; }
                    @Override
                    public String getUtilityMethod() { return utilityMethod; }
                    @Override
                    public double computeUtility(WorldState state) {
                        if (utilityFnFinal != null) {
                            try {
                                return ((Number) utilityFnFinal.invoke(agentInstance, state)).doubleValue();
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to compute utility for '" + name + "'", e);
                            }
                        }
                        return getUtility();
                    }
                    @Override
                    public Runnable getExecutor() {
                        return () -> {
                            try {
                                if (method.getParameterCount() == 0) {
                                    method.invoke(agentInstance);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to execute action: " + name, e);
                            }
                        };
                    }
                    @Override
                    public String toString() {
                        return "ActionInfo{name='" + name + "', description='" + description + "'}";
                    }
                });
            }
        }
        return actions;
    }

    public static List<GoalInfo> scanGoals(Object agentInstance) {
        List<GoalInfo> goals = new ArrayList<>();
        Class<?> clazz = agentInstance.getClass();
        for (Method method : clazz.getMethods()) {
            Goal[] annotations = method.getAnnotationsByType(Goal.class);
            for (Goal ann : annotations) {
                String name = ann.name().isEmpty() ? method.getName() : ann.name();
                String description = ann.description();
                Map<String, String> condition = factsToMap(ann.condition());
                goals.add(new GoalInfo() {
                    @Override
                    public String getName() { return name; }
                    @Override
                    public String getDescription() { return description; }
                    @Override
                    public Map<String, String> getCondition() { return condition; }
                    @Override
                    public String toString() {
                        return "GoalInfo{name='" + name + "', description='" + description + "'}";
                    }
                });
            }
        }
        return goals;
    }

    public static List<CompoundTaskDef> scanCompoundTasks(Object agentInstance) {
        List<CompoundTaskDef> result = new ArrayList<>();
        Class<?> clazz = agentInstance.getClass();
        for (Method method : clazz.getMethods()) {
            CompoundTask[] ctAnnotations = method.getAnnotationsByType(CompoundTask.class);
            for (CompoundTask ct : ctAnnotations) {
                String taskName = ct.name().isEmpty() ? method.getName() : ct.name();
                String taskDescription = ct.description();
                Decomposition[] decompAnnotations = method.getAnnotationsByType(Decomposition.class);
                List<DecompositionDef> decomps = new ArrayList<>();
                for (Decomposition d : decompAnnotations) {
                    String decompName = d.name().isEmpty() ? "default" : d.name();
                    String decompDescription = d.description();
                    decomps.add(new DecompositionDef(
                        decompName,
                        decompDescription,
                        factsToMap(d.preconditions()),
                        List.of(d.subtasks())
                    ));
                }
                result.add(new CompoundTaskDef(taskName, taskDescription, decomps));
            }
        }
        return result;
    }

    private static Map<String, String> factsToMap(Fact[] facts) {
        return Arrays.stream(facts)
            .collect(Collectors.toMap(Fact::name, Fact::value, (a, b) -> b, LinkedHashMap::new));
    }
}
