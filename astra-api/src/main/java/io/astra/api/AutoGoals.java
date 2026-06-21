package io.astra.api;

import java.util.*;
import java.util.stream.*;

/**
 * Utility for deriving goals automatically when the user does not define any.
 * Used internally by {@link DefaultAstra.Builder#build()} when no goals are
 * registered and at least one action exists.
 * <p>
 * Two strategies:
 * <ul>
 *   <li>{@link #fromActionEffects(List)} — creates a goal whose condition is
 *       the union of all action effects. Works well when actions declare
 *       meaningful effects.
 *   <li>{@link #runAll(List)} — creates a sentinel goal that triggers
 *       sequential execution of all actions (no planning). Used when no
 *       effects are declared (e.g. convention-based actions).
 * </ul>
 *
 * @see AgentBase
 * @see DefaultAstra.Builder#build()
 */
public final class AutoGoals {
    private AutoGoals() {}

    /**
     * Creates a goal whose condition is the union of all action effects.
     * The goal name is {@code "_auto_goal"}. If multiple actions produce
     * the same fact key, the last one wins.
     *
     * @param actions the registered actions (must not be null)
     * @return a goal that is satisfied when all effects are present in the world state
     */
    public static GoalInfo fromActionEffects(List<ActionInfo> actions) {
        Map<String, String> allEffects = new LinkedHashMap<>();
        for (ActionInfo a : actions) {
            allEffects.putAll(a.getEffects());
        }
        return new GoalInfo() {
            @Override public String getName() { return "_auto_goal"; }
            @Override public String getDescription() { return "Auto-derived goal from " + actions.size() + " actions"; }
            @Override public Map<String, String> getCondition() { return allEffects; }
            @Override public String toString() { return "AutoGoal{actions=" + actions.size() + "}"; }
        };
    }

    /**
     * Creates a sentinel goal named {@code "_run_all"} that causes
     * {@link DefaultAstra} to execute all actions sequentially without
     * planning. The condition {@code {_run_all: done}} is never matched
     * by any action effect, so execution is driven by the special-case
     * handling in {@code executeWithResult}.
     *
     * @param actions the registered actions (must not be null)
     * @return a sentinel goal that triggers sequential execution
     */
    public static GoalInfo runAll(List<ActionInfo> actions) {
        return new GoalInfo() {
            @Override public String getName() { return "_run_all"; }
            @Override public String getDescription() { return "Execute all " + actions.size() + " actions"; }
            @Override public Map<String, String> getCondition() { return Map.of("_run_all", "done"); }
            @Override public String toString() { return "RunAllGoal{actions=" + actions.size() + "}"; }
        };
    }
}
