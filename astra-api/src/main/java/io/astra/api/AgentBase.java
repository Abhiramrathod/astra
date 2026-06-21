package io.astra.api;

import io.astra.api.lifecycle.LifecycleAware;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for convention-driven agents. Extend this class and either
 * add actions programmatically via {@link #addAction(String, Runnable)} or
 * define public void no-argument methods — they are auto-detected as actions
 * at registration time.
 * <p>
 * No {@code @Agent}, {@code @Action}, or {@code @Goal} annotations are
 * required. Goals are derived automatically from action effects (see
 * {@link AutoGoals}). Lifeycle hooks ({@link #onInit()}, {@link #onDestroy()},
 * {@link #onError(Throwable)}) are inherited from {@link LifecycleAware}.
 * <p>
 * Combine with {@link DefaultAstra#simple(Object)} for a one-line setup:
 * <pre>{@code
 * AgentBase agent = new AgentBase() {
 *     public void fetchData() { ... }
 * };
 * Astra astra = DefaultAstra.simple(agent);
 * }</pre>
 *
 * @see AutoGoals
 * @see DefaultAstra#simple(Object)
 * @see io.astra.scanner.AgentScanner#scanActions(Object)
 */
public abstract class AgentBase implements LifecycleAware {
    private final List<ActionInfo> declaredActions = new CopyOnWriteArrayList<>();
    private final List<GoalInfo> declaredGoals = new CopyOnWriteArrayList<>();
    private boolean initialized;

    /**
     * Register an action with the given name and executor.
     * Description defaults to the action name, preconditions and effects
     * are empty, and cost is 1.0.
     *
     * @param name   unique action name
     * @param executor the code to run when the action executes
     */
    protected void addAction(String name, Runnable executor) {
        addAction(name, name, executor, Map.of(), Map.of(), 1.0f);
    }

    /**
     * Register an action with name, description, and executor.
     *
     * @param name        unique action name
     * @param description human-readable description
     * @param executor    the code to run when the action executes
     */
    protected void addAction(String name, String description, Runnable executor) {
        addAction(name, description, executor, Map.of(), Map.of(), 1.0f);
    }

    /**
     * Register an action with name, executor, preconditions, and effects.
     *
     * @param name         unique action name
     * @param executor     the code to run when the action executes
     * @param preconditions facts that must be true for the action to be applicable
     * @param effects      facts that become true after execution
     */
    protected void addAction(String name, Runnable executor,
                              Map<String, String> preconditions,
                              Map<String, String> effects) {
        addAction(name, name, executor, preconditions, effects, 1.0f);
    }

    /**
     * Register a fully specified action.
     *
     * @param name         unique action name
     * @param description  human-readable description
     * @param executor     the code to run when the action executes
     * @param preconditions facts that must be true for the action to be applicable
     * @param effects      facts that become true after execution
     * @param cost         weight used by cost-based planners (higher = less preferred)
     */
    protected void addAction(String name, String description, Runnable executor,
                              Map<String, String> preconditions,
                              Map<String, String> effects, float cost) {
        declaredActions.add(new ActionInfo() {
            @Override public String getName() { return name; }
            @Override public String getDescription() { return description; }
            @Override public Map<String, String> getPreconditions() { return preconditions; }
            @Override public Map<String, String> getEffects() { return effects; }
            @Override public float getCost() { return cost; }
            @Override public float getUtility() { return 0.5f; }
            @Override public Runnable getExecutor() { return executor; }
            @Override public String toString() { return "Action{name='" + name + "'}"; }
        });
    }

    /**
     * Register a goal with the given name and condition.
     * Description defaults to the goal name.
     *
     * @param name      unique goal name
     * @param condition facts that must be true for the goal to be satisfied
     */
    protected void addGoal(String name, Map<String, String> condition) {
        addGoal(name, name, condition);
    }

    /**
     * Register a fully specified goal.
     *
     * @param name        unique goal name
     * @param description human-readable description
     * @param condition   facts that must be true for the goal to be satisfied
     */
    protected void addGoal(String name, String description, Map<String, String> condition) {
        declaredGoals.add(new GoalInfo() {
            @Override public String getName() { return name; }
            @Override public String getDescription() { return description; }
            @Override public Map<String, String> getCondition() { return condition; }
            @Override public String toString() { return "Goal{name='" + name + "'}"; }
        });
    }

    /**
     * Returns actions registered programmatically via {@code addAction(...)}.
     * Does <b>not</b> include convention actions (public void methods).
     *
     * @return list of declared actions (never null)
     */
    public List<ActionInfo> getDeclaredActions() { return declaredActions; }

    /**
     * Returns goals registered programmatically via {@code addGoal(...)}.
     *
     * @return list of declared goals (never null)
     */
    public List<GoalInfo> getDeclaredGoals() { return declaredGoals; }

    /** Lifecycle hook — called once when the agent is registered. Override to implement custom initialization. */
    public void onInit() {}
    /** Lifecycle hook — called once when the agent is shut down. Override to implement custom cleanup. */
    public void onDestroy() {}
    /** Lifecycle hook — called when an exception occurs during agent operation. */
    public void onError(Throwable error) {}

    boolean isInitialized() { return initialized; }
    void setInitialized(boolean v) { initialized = v; }
}
