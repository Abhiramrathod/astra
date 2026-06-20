package io.astra.core;

import io.astra.api.*;
import io.astra.api.config.*;
import io.astra.api.event.*;
import io.astra.api.interceptor.*;
import io.astra.api.lifecycle.*;
import io.astra.api.result.*;
import io.astra.config.MapConfigProvider;
import java.util.ServiceLoader;
import io.astra.event.DefaultEventBus;
import io.astra.exception.agent.AgentRegistrationException;
import io.astra.exception.goal.DuplicateGoalException;
import io.astra.exception.goal.GoalNotFoundException;
import io.astra.exception.goal.PlanNotFoundException;
import io.astra.interceptor.DefaultInterceptorChain;
import io.astra.lifecycle.LifecycleManager;
import io.astra.scanner.AgentScanner;
import io.astra.utils.ClassPathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.astra.api.event.AstraEventListener;
import java.util.*;
import java.util.concurrent.*;

public class DefaultAstra implements Astra {
    private static final Logger log = LoggerFactory.getLogger(DefaultAstra.class);

    private final Map<String, GoalInfo> goals;
    private final List<ActionInfo> actions;
    private final Map<String, CompoundTaskDef> compoundTasks;
    private final Planner planner;
    private final PlannerType plannerType;
    private final DefaultEventBus eventBus;
    private final DefaultInterceptorChain interceptorChain;
    private final AstraConfig config;
    private final LifecycleManager lifecycleManager;

    private DefaultAstra(Map<String, GoalInfo> goals, List<ActionInfo> actions,
                         Map<String, CompoundTaskDef> compoundTasks,
                         PlannerType plannerType,
                         AstraConfig config, DefaultEventBus eventBus,
                         DefaultInterceptorChain interceptorChain,
                         LifecycleManager lifecycleManager) {
        this.goals = goals;
        this.actions = actions;
        this.compoundTasks = compoundTasks;
        this.plannerType = plannerType;
        this.config = config;
        this.eventBus = eventBus;
        this.interceptorChain = interceptorChain;
        this.lifecycleManager = lifecycleManager;
        this.planner = createPlanner(plannerType);
    }

    private Planner createPlanner(PlannerType type) {
        for (PlannerProvider provider : ServiceLoader.load(PlannerProvider.class)) {
            if (provider.type() == type) {
                return provider.create(config, compoundTasks, actions);
            }
        }
        throw new IllegalArgumentException("No PlannerProvider found for type: " + type);
    }

    @Override
    public Plan plan(String goalName, WorldState initialState) {
        Objects.requireNonNull(goalName, "Goal name must not be null");
        Objects.requireNonNull(initialState, "Initial state must not be null");

        if (plannerType == PlannerType.STRUCTURAL) {
            if (!compoundTasks.containsKey(goalName)) {
                throw new GoalNotFoundException(goalName);
            }
            CompoundTaskDef ct = compoundTasks.get(goalName);
            GoalInfo structGoal = new GoalInfo() {
                @Override public String getName() { return goalName; }
                @Override public String getDescription() { return ct.getDescription(); }
                @Override public Map<String, String> getCondition() { return Map.of(); }
                @Override public String toString() { return "Structural:" + goalName; }
            };
            return planner.plan(initialState, structGoal, actions);
        }

        GoalInfo goal = goals.get(goalName);
        if (goal == null) throw new GoalNotFoundException(goalName);
        return planner.plan(initialState, goal, actions);
    }

    @Override
    public WorldState execute(String goalName, WorldState initialState) {
        return executeWithResult(goalName, initialState).getFinalState();
    }

    @Override
    public ExecutionResult executeWithResult(String goalName, WorldState initialState) {
        Objects.requireNonNull(goalName, "Goal name must not be null");
        Objects.requireNonNull(initialState, "Initial state must not be null");

        GoalInfo goal;
        if (plannerType == PlannerType.STRUCTURAL) {
            if (!compoundTasks.containsKey(goalName)) {
                throw new GoalNotFoundException(goalName);
            }
            CompoundTaskDef ct = compoundTasks.get(goalName);
            goal = new GoalInfo() {
                @Override public String getName() { return goalName; }
                @Override public String getDescription() { return ct.getDescription(); }
                @Override public Map<String, String> getCondition() { return Map.of(); }
                @Override public String toString() { return "Structural:" + goalName; }
            };
        } else {
            goal = goals.get(goalName);
            if (goal == null) throw new GoalNotFoundException(goalName);
        }

        log.info("Starting execution for goal '{}' (planner: {})", goalName, plannerType);
        eventBus.publish(AstraEvent.of(AstraEventType.PLAN_STARTED, "goal", goalName, "state", initialState));

        Plan plan = planner.plan(initialState, goal, actions);
        if (!plan.isExecutable()) {
            eventBus.publish(AstraEvent.of(AstraEventType.PLAN_FAILED, "goal", goalName, "reason", "no plan found"));
            throw new PlanNotFoundException(goalName, initialState);
        }

        eventBus.publish(AstraEvent.of(AstraEventType.PLAN_COMPLETED, "goal", goalName, "steps", plan.getActions().size()));

        List<ActionExecutionRecord> records = new ArrayList<>();
        WorldState state = initialState;
        boolean overallSuccess = true;
        long planStart = System.currentTimeMillis();

        for (ActionInfo action : plan.getActions()) {
            WorldState before = state;
            long start = System.currentTimeMillis();
            boolean success = true;
            String errorMsg = null;

            eventBus.publish(AstraEvent.of(AstraEventType.ACTION_BEFORE,
                "action", action.getName(), "state", state));
            interceptorChain.beforeAction(action, state);

            try {
                log.debug("Executing action: {}", action.getName());
                action.getExecutor().run();
            } catch (Exception e) {
                success = false;
                overallSuccess = false;
                errorMsg = e.getMessage();
                log.error("Action '{}' failed: {}", action.getName(), e.getMessage());
                eventBus.publish(AstraEvent.of(AstraEventType.ACTION_FAILED,
                    "action", action.getName(), "error", e.getMessage()));
                interceptorChain.onError(action, state, e);
            }

            long elapsed = System.currentTimeMillis() - start;

            if (success) {
                for (var entry : action.getEffects().entrySet()) {
                    state = state.set(entry.getKey(), entry.getValue());
                }
                eventBus.publish(AstraEvent.of(AstraEventType.ACTION_AFTER,
                    "action", action.getName(), "state", state, "durationMs", elapsed));
                interceptorChain.afterAction(action, before, state);
            }

            records.add(new ActionExecutionRecord(action, before, state, success, elapsed, errorMsg));
            if (!success) break;
        }

        long totalDuration = System.currentTimeMillis() - planStart;

        if (state.matches(goal.getCondition())) {
            eventBus.publish(AstraEvent.of(AstraEventType.GOAL_SATISFIED,
                "goal", goalName, "state", state));
        } else {
            eventBus.publish(AstraEvent.of(AstraEventType.GOAL_UNSATISFIABLE,
                "goal", goalName, "state", state));
        }

        if (overallSuccess) {
            log.info("Goal '{}' achieved in {}ms. Final state: {}", goalName, totalDuration, state);
            return ExecutionResult.success(plan, state, records, totalDuration);
        }
        log.error("Goal '{}' failed after {}ms", goalName, totalDuration);
        return ExecutionResult.failure(plan, state, records, totalDuration,
            records.stream().filter(r -> !r.isSuccess()).findFirst()
                .map(r -> "Action failed: " + r.getAction().getName())
                .orElse("Unknown error"));
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(String goalName, WorldState initialState) {
        return CompletableFuture.supplyAsync(() -> executeWithResult(goalName, initialState));
    }

    @Override
    public EventBus getEventBus() { return eventBus; }
    @Override
    public InterceptorChain getInterceptorChain() { return interceptorChain; }
    @Override
    public AstraConfig getConfig() { return config; }
    public LifecycleManager getLifecycleManager() { return lifecycleManager; }
    public PlannerType getPlannerType() { return plannerType; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Map<String, GoalInfo> goals = new ConcurrentHashMap<>();
        private final List<ActionInfo> actions = new CopyOnWriteArrayList<>();
        private final Map<String, CompoundTaskDef> compoundTasks = new ConcurrentHashMap<>();
        private final DefaultEventBus eventBus = new DefaultEventBus();
        private final DefaultInterceptorChain interceptorChain = new DefaultInterceptorChain();
        private final LifecycleManager lifecycleManager = new LifecycleManager();
        private final MapConfigProvider config = new MapConfigProvider();
        private PlannerType plannerType = PlannerType.COST_BASED;

        public Builder() {
            config.set("astra.planner.heuristic", "unmatched");
            config.set("astra.planner.maxIterations", "10000");
        }

        public Builder register(Object agentInstance) {
            Objects.requireNonNull(agentInstance, "Agent instance must not be null");
            if (!agentInstance.getClass().isAnnotationPresent(io.astra.annotation.Agent.class)) {
                throw new AgentRegistrationException(
                    "Class " + agentInstance.getClass().getName() + " is not annotated with @Agent", null);
            }
            actions.addAll(AgentScanner.scanActions(agentInstance));
            for (GoalInfo goal : AgentScanner.scanGoals(agentInstance)) {
                if (goals.containsKey(goal.getName())) {
                    throw new DuplicateGoalException(goal.getName());
                }
                goals.put(goal.getName(), goal);
            }
            for (CompoundTaskDef ct : AgentScanner.scanCompoundTasks(agentInstance)) {
                if (compoundTasks.containsKey(ct.getName())) {
                    throw new DuplicateGoalException("CompoundTask:" + ct.getName());
                }
                compoundTasks.put(ct.getName(), ct);
            }
            lifecycleManager.init(agentInstance);
            eventBus.publish(AstraEvent.of(AstraEventType.AGENT_REGISTERED,
                "agent", agentInstance.getClass().getName()));
            int actionCount = AgentScanner.scanActions(agentInstance).size();
            int goalCount = AgentScanner.scanGoals(agentInstance).size();
            int structCount = AgentScanner.scanCompoundTasks(agentInstance).size();
            log.info("Agent '{}' registered ({} actions, {} goals, {} compound tasks)",
                agentInstance.getClass().getSimpleName(), actionCount, goalCount, structCount);
            return this;
        }

        public Builder register(Object... agentInstances) {
            for (Object instance : agentInstances) register(instance);
            return this;
        }

        public Builder scan(String packageName) {
            Objects.requireNonNull(packageName, "Package name must not be null");
            log.info("Scanning package '{}' for @Agent classes", packageName);
            List<Class<?>> classes = ClassPathScanner.findClasses(packageName);
            int found = 0;
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(io.astra.annotation.Agent.class)) {
                    try {
                        register(clazz.getDeclaredConstructor().newInstance());
                        found++;
                    } catch (Exception e) {
                        throw new AgentRegistrationException("Failed to instantiate " + clazz.getName(), e);
                    }
                }
            }
            log.info("Found and registered {} @Agent classes in package '{}'", found, packageName);
            return this;
        }

        public Builder withPlanner(PlannerType type) {
            this.plannerType = type;
            log.info("Planner set to: {}", type);
            return this;
        }

        public Builder withConfig(String key, String value) {
            config.set(key, value);
            return this;
        }

        public Builder withConfig(Map<String, String> configMap) {
            configMap.forEach(config::set);
            return this;
        }

        public Builder withConfig(AstraConfig configProvider) {
            return this;
        }

        public Builder withInterceptor(ActionInterceptor interceptor) {
            interceptorChain.addInterceptor(interceptor);
            return this;
        }

        public Builder withEventListener(AstraEventListener listener) {
            eventBus.subscribe(listener);
            return this;
        }

        public DefaultAstra build() {
            log.info("Building Astra (planner: {}) with {} goals, {} compound tasks, {} actions",
                plannerType, goals.size(), compoundTasks.size(), actions.size());
            return new DefaultAstra(goals, actions, compoundTasks, plannerType,
                config, eventBus, interceptorChain, lifecycleManager);
        }
    }
}
