package io.astra.scanner;

import io.astra.annotation.*;
import io.astra.annotation.action.*;
import io.astra.annotation.fact.*;
import io.astra.annotation.goal.*;
import io.astra.annotation.decomposition.*;
import io.astra.api.*;
import io.astra.api.QueryContext;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * Scans agent instances for actions, goals, and compound tasks by
 * inspecting annotations ({@code @Action}, {@code @Goal},
 * {@code @CompoundTask}) and convention-based public void methods.
 */
public final class AgentScanner {
    private AgentScanner() {}

    /**
     * Scans an agent instance for actions. Supports three sources:
     * <ol>
     *   <li>Programmatic actions declared via {@link AgentBase#addAction(String, Runnable)}
     *   <li>Methods annotated with {@code @Action}
     *   <li>Convention actions — public void no-argument methods on
     *       {@link AgentBase} subclasses (no annotation required)
     * </ol>
     * Convention actions are detected for methods declared on the class
     * hierarchy (excluding {@code Object} and {@code AgentBase} itself).
     * If a programmatic or annotated action already exists with the same
     * name, the convention scan skips it.
     *
     * @param agentInstance the agent instance (may be {@code @Agent}-annotated or an {@code AgentBase})
     * @return list of all discovered actions (never null)
     */
    public static List<ActionInfo> scanActions(Object agentInstance) {
        Set<String> seenNames = new HashSet<>();
        List<ActionInfo> actions = new ArrayList<>();

        if (agentInstance instanceof AgentBase) {
            AgentBase base = (AgentBase) agentInstance;
            actions.addAll(base.getDeclaredActions());
            base.getDeclaredActions().forEach(a -> seenNames.add(a.getName()));
        }

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
                Class<?>[] paramTypes = method.getParameterTypes();

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
                                } else {
                                    Object[] args = resolveParameters(method, paramTypes);
                                    method.invoke(agentInstance, args);
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
                seenNames.add(name);
            }
        }

        if (agentInstance instanceof AgentBase) {
            Class<?> cls = clazz;
            while (cls != null) {
                for (Method method : cls.getDeclaredMethods()) {
                    if (isConventionAction(method) && !seenNames.contains(method.getName())) {
                        String name = method.getName();
                        int mod = method.getModifiers();
                        if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
                            actions.add(new ActionInfo() {
                                @Override public String getName() { return name; }
                                @Override public String getDescription() { return "Convention-based: " + name; }
                                @Override public Map<String, String> getPreconditions() { return Map.of(); }
                                @Override public Map<String, String> getEffects() { return Map.of(); }
                                @Override public float getCost() { return 1.0f; }
                                @Override public float getUtility() { return 0.5f; }
                                @Override public Runnable getExecutor() {
                                    return () -> {
                                        try {
                                            method.setAccessible(true);
                                            method.invoke(agentInstance);
                                        } catch (Exception e) {
                                            throw new RuntimeException("Failed to execute action: " + name, e);
                                        }
                                    };
                                }
                                @Override public String toString() { return "Action{name='" + name + "'}"; }
                            });
                            seenNames.add(name);
                        }
                    }
                }
                cls = cls.getSuperclass();
            }
        }
        return actions;
    }

    /**
     * Determines whether a method qualifies as a convention action.
     * A method is a convention action iff all of the following hold:
     * <ul>
     *   <li>declared on the user's class (not {@code Object} or {@code AgentBase})
     *   <li>public
     *   <li>not static
     *   <li>takes zero parameters
     *   <li>returns void
     *   <li>not annotated with {@code @Action}, {@code @Goal}, or {@code @CompoundTask}
     * </ul>
     */
    private static boolean isConventionAction(Method method) {
        if (method.isBridge() || method.isSynthetic()) return false;
        if (method.getDeclaringClass() == Object.class) return false;
        if (method.getDeclaringClass() == AgentBase.class) return false;
        int mod = method.getModifiers();
        if (!Modifier.isPublic(mod)) return false;
        if (Modifier.isStatic(mod)) return false;
        if (method.getParameterCount() != 0) return false;
        if (method.getReturnType() != void.class) return false;
        if (method.getAnnotation(Action.class) != null) return false;
        if (method.getAnnotation(Goal.class) != null) return false;
        if (method.getAnnotation(CompoundTask.class) != null) return false;
        return true;
    }

    /**
     * Scans an agent instance for goals. Supports two sources:
     * <ol>
     *   <li>Programmatic goals declared via {@link AgentBase#addGoal(String, Map)}
     *   <li>Methods annotated with {@code @Goal}
     * </ol>
     *
     * @param agentInstance the agent instance
     * @return list of all discovered goals (never null)
     */
    public static List<GoalInfo> scanGoals(Object agentInstance) {
        Set<String> seenNames = new HashSet<>();
        List<GoalInfo> goals = new ArrayList<>();

        if (agentInstance instanceof AgentBase) {
            AgentBase base = (AgentBase) agentInstance;
            goals.addAll(base.getDeclaredGoals());
            base.getDeclaredGoals().forEach(g -> seenNames.add(g.getName()));
        }

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
                seenNames.add(name);
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

    private static Object[] resolveParameters(Method method, Class<?>[] paramTypes) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == String.class) {
                args[i] = QueryContext.get();
            } else {
                throw new IllegalArgumentException(
                    "Unsupported parameter type '" + paramTypes[i].getSimpleName()
                    + "' for action method '" + method.getName()
                    + "'. Only String parameters are supported.");
            }
        }
        return args;
    }
}
