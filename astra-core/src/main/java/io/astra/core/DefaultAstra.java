package io.astra.core;

import io.astra.api.*;
import io.astra.api.config.*;
import io.astra.api.event.*;
import io.astra.api.interceptor.*;
import io.astra.api.lifecycle.*;
import io.astra.api.result.*;
import io.astra.api.store.*;
import io.astra.api.cache.*;
import io.astra.api.policy.*;
import io.astra.api.validation.*;
import io.astra.api.telemetry.*;
import io.astra.api.scheduler.*;
import io.astra.api.agentbus.*;
import io.astra.api.skill.*;
import io.astra.api.approval.*;
import io.astra.api.composite.*;
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
import io.astra.validation.DefaultValidator;
import io.astra.validation.ValidationInterceptor;
import io.astra.store.InMemoryWorldStateStore;
import io.astra.store.FileWorldStateStore;
import io.astra.agentbus.DefaultAgentBus;
import io.astra.telemetry.Slf4jTracer;
import io.astra.telemetry.SimpleMetricsCollector;
import io.astra.telemetry.TelemetryInterceptor;
import io.astra.core.cache.LruPlanCache;
import io.astra.core.policy.DefaultPolicyChecker;
import io.astra.core.policy.PolicyInterceptor;
import io.astra.core.skill.SkillManager;
import io.astra.core.composite.DefaultCompositeAgent;
import io.astra.core.approval.ConsoleGoalChoiceApprover;
import io.astra.core.scheduler.DefaultSchedulerService;
import io.astra.core.shell.DefaultRepl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.astra.api.event.AstraEventListener;
import java.util.*;
import java.util.concurrent.*;

/**
 * Default implementation of the {@link Astra} interface. Plans and executes
 * goals against a set of registered actions, supporting static and OODA
 * replanning strategies, event publication, and interceptor chains.
 */
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
    private final boolean replanning;
    private final int replanLimit;

    private DefaultAstra(Map<String, GoalInfo> goals, List<ActionInfo> actions,
                         Map<String, CompoundTaskDef> compoundTasks,
                         PlannerType plannerType,
                         AstraConfig config, DefaultEventBus eventBus,
                         DefaultInterceptorChain interceptorChain,
                         LifecycleManager lifecycleManager,
                         boolean replanning, int replanLimit) {
        this.goals = goals;
        this.actions = actions;
        this.compoundTasks = compoundTasks;
        this.plannerType = plannerType;
        this.config = config;
        this.eventBus = eventBus;
        this.interceptorChain = interceptorChain;
        this.lifecycleManager = lifecycleManager;
        this.replanning = replanning;
        this.replanLimit = replanLimit;
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

        if ("_run_all".equals(goalName)) {
            return executeAll(initialState);
        }

        GoalInfo goal = resolveGoal(goalName);

        log.info("Starting execution for goal '{}' (planner: {}, replanning: {})",
            goalName, plannerType, replanning);
        eventBus.publish(AstraEvent.of(AstraEventType.PLAN_STARTED,
            "goal", goalName, "state", initialState, "replanning", replanning));

        List<ActionExecutionRecord> records = new ArrayList<>();
        long planStart = System.currentTimeMillis();
        boolean overallSuccess;

        if (replanning) {
            overallSuccess = executeOODA(goal, initialState, records);
        } else {
            overallSuccess = executeStaticPlan(goal, initialState, records);
        }

        WorldState finalState = deriveCurrentState(initialState, records);
        long totalDuration = System.currentTimeMillis() - planStart;

        if (finalState.matches(goal.getCondition())) {
            eventBus.publish(AstraEvent.of(AstraEventType.GOAL_SATISFIED,
                "goal", goalName, "state", finalState));
        } else {
            eventBus.publish(AstraEvent.of(AstraEventType.GOAL_UNSATISFIABLE,
                "goal", goalName, "state", finalState));
        }

        Plan finalPlan = new Plan() {
            @Override public List<ActionInfo> getActions() {
                return records.stream().map(ActionExecutionRecord::getAction).toList();
            }
            @Override public double getTotalCost() {
                return records.stream().mapToDouble(r -> r.getAction().getCost()).sum();
            }
            @Override public boolean isExecutable() { return !records.isEmpty(); }
            @Override public WorldState execute(WorldState ws) { return finalState; }
        };

        if (overallSuccess) {
            log.info("Goal '{}' achieved in {}ms. Final state: {} ({} steps, replans: {})",
                goalName, totalDuration, finalState, records.size(),
                replanning ? Math.max(0, records.size() - 1) : 0);
            return ExecutionResult.success(finalPlan, finalState, records, totalDuration);
        }
        log.error("Goal '{}' failed after {}ms", goalName, totalDuration);
        return ExecutionResult.failure(finalPlan, finalState, records, totalDuration,
            records.stream().filter(r -> !r.isSuccess()).findFirst()
                .map(r -> "Action failed: " + r.getAction().getName())
                .orElse("Goal not achieved"));
    }

    private ExecutionResult executeAll(WorldState initialState) {
        log.info("Executing all {} actions in sequence", actions.size());
        eventBus.publish(AstraEvent.of(AstraEventType.PLAN_STARTED,
            "goal", "_run_all", "state", initialState));
        List<ActionExecutionRecord> records = new ArrayList<>();
        long start = System.currentTimeMillis();
        boolean success = true;
        for (ActionInfo action : actions) {
            WorldState before = deriveCurrentState(initialState, records);
            if (!executeSingleAction(action, before, records)) {
                success = false;
                break;
            }
        }
        WorldState finalState = deriveCurrentState(initialState, records);
        long duration = System.currentTimeMillis() - start;
        Plan plan = new Plan() {
            @Override public List<ActionInfo> getActions() {
                return records.stream().map(ActionExecutionRecord::getAction).toList();
            }
            @Override public double getTotalCost() {
                return records.stream().mapToDouble(r -> r.getAction().getCost()).sum();
            }
            @Override public boolean isExecutable() { return !records.isEmpty(); }
            @Override public WorldState execute(WorldState ws) { return finalState; }
        };
        if (success) {
            return ExecutionResult.success(plan, finalState, records, duration);
        }
        return ExecutionResult.failure(plan, finalState, records, duration,
            "Action failed: " + records.stream().filter(r -> !r.isSuccess()).findFirst()
                .map(r -> r.getAction().getName()).orElse("unknown"));
    }

    private boolean executeStaticPlan(GoalInfo goal, WorldState initialState,
                                       List<ActionExecutionRecord> records) {
        Plan plan = planner.plan(initialState, goal, actions);
        if (!plan.isExecutable()) {
            eventBus.publish(AstraEvent.of(AstraEventType.PLAN_FAILED,
                "goal", goal.getName(), "reason", "no plan found"));
            throw new PlanNotFoundException(goal.getName(), initialState);
        }
        eventBus.publish(AstraEvent.of(AstraEventType.PLAN_COMPLETED,
            "goal", goal.getName(), "steps", plan.getActions().size()));
        for (ActionInfo action : plan.getActions()) {
            if (!executeSingleAction(action, deriveCurrentState(initialState, records), records))
                return false;
        }
        return true;
    }

    private boolean executeOODA(GoalInfo goal, WorldState initialState,
                                 List<ActionExecutionRecord> records) {
        for (int replan = 0; replan < replanLimit; replan++) {
            WorldState currentState = deriveCurrentState(initialState, records);
            if (currentState.matches(goal.getCondition())) return true;

            Plan plan = planner.plan(currentState, goal, actions);
            if (!plan.isExecutable()) {
                if (currentState.matches(goal.getCondition())) return true;
                log.warn("OODA: no plan after {} replans from state {}", replan, currentState);
                eventBus.publish(AstraEvent.of(AstraEventType.PLAN_FAILED,
                    "goal", goal.getName(), "reason", "no plan at replan " + replan));
                return false;
            }

            eventBus.publish(AstraEvent.of(AstraEventType.PLAN_COMPLETED,
                "goal", goal.getName(), "steps", plan.getActions().size(), "replan", replan));

            ActionInfo nextAction = plan.getActions().get(0);
            if (!executeSingleAction(nextAction, currentState, records)) return false;
        }

        log.warn("OODA: reached replan limit {} without satisfying goal", replanLimit);
        return deriveCurrentState(initialState, records).matches(goal.getCondition());
    }

    private static WorldState deriveCurrentState(WorldState initialState,
                                                  List<ActionExecutionRecord> records) {
        if (records.isEmpty()) return initialState;
        return records.get(records.size() - 1).getStateAfter();
    }

    private boolean executeSingleAction(ActionInfo action, WorldState before,
                                         List<ActionExecutionRecord> records) {
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;

        eventBus.publish(AstraEvent.of(AstraEventType.ACTION_BEFORE,
            "action", action.getName(), "state", before));
        interceptorChain.beforeAction(action, before);

        try {
            log.debug("Executing action: {}", action.getName());
            action.getExecutor().run();
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            log.error("Action '{}' failed: {}", action.getName(), e.getMessage());
            eventBus.publish(AstraEvent.of(AstraEventType.ACTION_FAILED,
                "action", action.getName(), "error", e.getMessage()));
            interceptorChain.onError(action, before, e);
        }

        long elapsed = System.currentTimeMillis() - start;

        WorldState stateAfter = before;
        if (success) {
            for (var entry : action.getEffects().entrySet()) {
                stateAfter = stateAfter.set(entry.getKey(), entry.getValue());
            }
            eventBus.publish(AstraEvent.of(AstraEventType.ACTION_AFTER,
                "action", action.getName(), "state", stateAfter, "durationMs", elapsed));
            interceptorChain.afterAction(action, before, stateAfter);
        }

        records.add(new ActionExecutionRecord(action, before, stateAfter, success, elapsed, errorMsg));
        return success;
    }

    private GoalInfo resolveGoal(String goalName) {
        if (plannerType == PlannerType.STRUCTURAL) {
            if (!compoundTasks.containsKey(goalName)) throw new GoalNotFoundException(goalName);
            CompoundTaskDef ct = compoundTasks.get(goalName);
            return new GoalInfo() {
                @Override public String getName() { return goalName; }
                @Override public String getDescription() { return ct.getDescription(); }
                @Override public Map<String, String> getCondition() { return Map.of(); }
                @Override public String toString() { return "Structural:" + goalName; }
            };
        }
        GoalInfo goal = goals.get(goalName);
        if (goal == null) throw new GoalNotFoundException(goalName);
        return goal;
    }

    @Override
    public ExecutionResult executeQuery(String goalName, WorldState initialState, String query) {
        Objects.requireNonNull(query, "Query must not be null");
        QueryContext.set(query);
        try {
            return executeWithResult(goalName, initialState);
        } finally {
            QueryContext.remove();
        }
    }

    @Override
    public ExecutionResult executeQuery(String query) {
        Objects.requireNonNull(query, "Query must not be null");
        String bestGoal = resolveGoalForQuery(query);
        log.info("Auto-routed query '{}' to goal '{}'", query, bestGoal);
        return executeQuery(bestGoal, WorldStates.of("_query", query), query);
    }

    private String resolveGoalForQuery(String query) {
        Set<String> queryWords = tokenize(query);
        if (queryWords.isEmpty()) {
            if (!goals.isEmpty()) return goals.keySet().iterator().next();
            if (!compoundTasks.isEmpty()) return compoundTasks.keySet().iterator().next();
            throw new GoalNotFoundException("No goals or compound tasks registered");
        }

        String bestName = null;
        int bestScore = 0;

        for (GoalInfo goal : goals.values()) {
            int score = scoreMatch(queryWords, goal);
            if (score > bestScore) { bestScore = score; bestName = goal.getName(); }
        }

        for (Map.Entry<String, CompoundTaskDef> entry : compoundTasks.entrySet()) {
            int score = scoreMatch(queryWords, entry.getValue());
            if (score > bestScore) { bestScore = score; bestName = entry.getKey(); }
        }

        if (bestName == null) {
            if (!goals.isEmpty()) return goals.keySet().iterator().next();
            if (!compoundTasks.isEmpty()) return compoundTasks.keySet().iterator().next();
            throw new GoalNotFoundException("No match for query: " + query);
        }
        return bestName;
    }

    private int scoreMatch(Set<String> queryWords, GoalInfo goal) {
        Set<String> corpus = new HashSet<>();
        corpus.addAll(tokenize(goal.getName()));
        corpus.addAll(tokenize(goal.getDescription()));
        for (ActionInfo a : actions) {
            corpus.addAll(tokenize(a.getName()));
            corpus.addAll(tokenize(a.getDescription()));
        }
        int score = 0;
        for (String qw : queryWords) {
            if (tokenize(goal.getName()).contains(qw)) score += 3;
            else if (tokenize(goal.getDescription()).contains(qw)) score += 2;
            for (ActionInfo a : actions) {
                if (tokenize(a.getName()).contains(qw)) { score += 2; break; }
            }
        }
        for (String qw : queryWords) {
            for (ActionInfo a : actions) {
                if (tokenize(a.getDescription()).contains(qw)) { score += 1; break; }
            }
        }
        return score;
    }

    private int scoreMatch(Set<String> queryWords, CompoundTaskDef ct) {
        Set<String> corpus = new HashSet<>();
        corpus.addAll(tokenize(ct.getName()));
        corpus.addAll(tokenize(ct.getDescription()));
        for (ActionInfo a : actions) {
            corpus.addAll(tokenize(a.getName()));
            corpus.addAll(tokenize(a.getDescription()));
        }
        int score = 0;
        for (String qw : queryWords) {
            if (tokenize(ct.getName()).contains(qw)) score += 3;
            else if (tokenize(ct.getDescription()).contains(qw)) score += 2;
            for (ActionInfo a : actions) {
                if (tokenize(a.getName()).contains(qw)) { score += 2; break; }
            }
        }
        for (String qw : queryWords) {
            for (ActionInfo a : actions) {
                if (tokenize(a.getDescription()).contains(qw)) { score += 1; break; }
            }
        }
        return score;
    }

    private static Set<String> tokenize(String text) {
        if (text == null || text.isEmpty()) return Set.of();
        String[] words = text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .split("\\s+");
        Set<String> result = new HashSet<>();
        for (String w : words) {
            w = w.trim();
            if (w.length() > 1 && !STOP_WORDS.contains(w)) result.add(w);
        }
        return result;
    }

    private static final Set<String> STOP_WORDS = Set.of(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "shall", "can", "need", "dare", "ought",
        "used", "to", "of", "in", "for", "on", "with", "at", "by", "from",
        "as", "into", "through", "during", "before", "after", "above", "below",
        "between", "out", "off", "over", "under", "again", "further", "then",
        "once", "here", "there", "when", "where", "why", "how", "all", "each",
        "every", "both", "few", "more", "most", "other", "some", "such", "no",
        "nor", "not", "only", "own", "same", "so", "than", "too", "very",
        "just", "because", "but", "and", "or", "if", "while", "about"
    );

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
    public List<ActionInfo> getActions() { return actions; }
    public Map<String, GoalInfo> getGoals() { return goals; }
    public Map<String, CompoundTaskDef> getCompoundTasks() { return compoundTasks; }

    public WorldStateStore createStore() { return new InMemoryWorldStateStore(); }
    public PlanCache createCache(int maxSize) { return new LruPlanCache(maxSize); }
    public Tracer createTracer() { return new Slf4jTracer(); }
    public MetricsCollector createMetrics() { return new SimpleMetricsCollector(); }
    public AgentBus createAgentBus() { return new DefaultAgentBus(eventBus); }
    public SkillManager createSkillManager() { return new SkillManager(config); }
    public GoalChoiceApprover createGoalApprover() { return new ConsoleGoalChoiceApprover(); }
    public StateHistory createStateHistory(int maxDepth) { return new StateHistory(maxDepth, eventBus); }
    public DefaultRepl createRepl() { return new DefaultRepl(this); }
    public DefaultCompositeAgent createCompositeAgent() {
        return new DefaultCompositeAgent(eventBus, interceptorChain, config);
    }

    public static Builder builder() { return new Builder(); }

    /**
     * Creates an {@link Astra} instance with the minimal configuration
     * required to run an agent. Equivalent to:
     * <pre>{@code
     * DefaultAstra.builder().register(agentInstance).build()
     * }</pre>
     * <p>
     * The agent instance may be a class annotated with {@code @Agent}
     * or an {@link AgentBase} subclass. Goals are auto-derived when
     * none are defined (see {@link AutoGoals}).
     *
     * @param agentInstance the agent — either {@code @Agent}-annotated or an {@code AgentBase} subclass
     * @return a fully wired Astra instance, ready for {@code execute} / {@code executeQuery}
     * @throws io.astra.exception.agent.AgentRegistrationException if the class has neither {@code @Agent} nor extends {@code AgentBase}
     * @see DefaultAstra.Builder#register(Object)
     */
    public static Astra simple(Object agentInstance) {
        return builder().register(agentInstance).build();
    }

    public static class Builder {
        private final Map<String, GoalInfo> goals = new ConcurrentHashMap<>();
        private final List<ActionInfo> actions = new CopyOnWriteArrayList<>();
        private final Map<String, CompoundTaskDef> compoundTasks = new ConcurrentHashMap<>();
        private final DefaultEventBus eventBus = new DefaultEventBus();
        private final DefaultInterceptorChain interceptorChain = new DefaultInterceptorChain();
        private final LifecycleManager lifecycleManager = new LifecycleManager();
        private final MapConfigProvider config = new MapConfigProvider();
        private PlannerType plannerType = PlannerType.COST_BASED;
        private boolean replanning = false;
        private int replanLimit = 100;

        private WorldStateStore store;
        private Validator validator;
        private PlanCache cache;
        private PolicyChecker policyChecker;
        private Tracer tracer;
        private MetricsCollector metrics;
        private AgentBus agentBus;
        private SkillManager skillManager;
        private GoalChoiceApprover goalApprover;

        public Builder() {
            config.set("astra.planner.heuristic", "unmatched");
            config.set("astra.planner.maxIterations", "10000");
        }

        /**
         * Register an agent instance. The instance must be annotated with
         * {@code @Agent} or extend {@link AgentBase}. All actions and goals
         * are scanned and registered.
         * <p>
         * If the instance extends {@link AgentBase}, public void no-argument
         * methods are auto-detected as convention actions (no {@code @Action}
         * annotation required), and programmatic actions/goals via
         * {@code addAction/addGoal} are included.
         *
         * @param agentInstance the agent to register
         * @return this builder
         * @throws io.astra.exception.agent.AgentRegistrationException if the class has neither {@code @Agent} nor extends {@code AgentBase}
         * @throws io.astra.exception.goal.DuplicateGoalException if a goal with the same name is already registered
         */
        public Builder register(Object agentInstance) {
            Objects.requireNonNull(agentInstance, "Agent instance must not be null");
            if (!agentInstance.getClass().isAnnotationPresent(io.astra.annotation.Agent.class)
                && !(agentInstance instanceof AgentBase)) {
                throw new AgentRegistrationException(
                    "Class " + agentInstance.getClass().getName()
                    + " is not annotated with @Agent and does not extend AgentBase", null);
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

        public Builder withReplanning(boolean enabled) {
            this.replanning = enabled;
            return this;
        }

        public Builder withReplanning(int limit) {
            this.replanning = true;
            this.replanLimit = limit;
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

        public Builder withValidator(Validator validator) {
            this.validator = validator;
            return this;
        }

        public Builder withStore(WorldStateStore store) {
            this.store = store;
            return this;
        }

        public Builder withCache(PlanCache cache) {
            this.cache = cache;
            return this;
        }

        public Builder withPolicyChecker(PolicyChecker policyChecker) {
            this.policyChecker = policyChecker;
            return this;
        }

        public Builder withTracer(Tracer tracer) {
            this.tracer = tracer;
            return this;
        }

        public Builder withMetrics(MetricsCollector metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder withAgentBus(AgentBus agentBus) {
            this.agentBus = agentBus;
            return this;
        }

        public Builder withSkillManager(SkillManager skillManager) {
            this.skillManager = skillManager;
            return this;
        }

        public Builder withGoalApprover(GoalChoiceApprover approver) {
            this.goalApprover = approver;
            return this;
        }

        public DefaultAstra build() {
            log.info("Building Astra (planner: {}) with {} goals, {} compound tasks, {} actions",
                plannerType, goals.size(), compoundTasks.size(), actions.size());

            if (goals.isEmpty() && !actions.isEmpty()) {
                boolean hasEffects = actions.stream()
                    .anyMatch(a -> !a.getEffects().isEmpty());
                if (hasEffects) {
                    GoalInfo autoGoal = AutoGoals.fromActionEffects(actions);
                    goals.put(autoGoal.getName(), autoGoal);
                    log.info("No goals defined — auto-created goal '{}' from {} action effects",
                        autoGoal.getName(), autoGoal.getCondition().size());
                } else {
                    GoalInfo runAll = AutoGoals.runAll(actions);
                    goals.put(runAll.getName(), runAll);
                    log.info("No goals or effects defined — auto-created '{}' goal",
                        runAll.getName());
                }
            }

            if (validator != null) {
                interceptorChain.addInterceptor(new ValidationInterceptor(validator, eventBus));
            }
            if (policyChecker != null) {
                interceptorChain.addInterceptor(new PolicyInterceptor(policyChecker, eventBus));
            }
            if (tracer != null && metrics != null) {
                interceptorChain.addInterceptor(new TelemetryInterceptor(tracer, metrics));
            } else if (tracer != null) {
                interceptorChain.addInterceptor(new TelemetryInterceptor(tracer, new SimpleMetricsCollector()));
            }

            return new DefaultAstra(goals, actions, compoundTasks, plannerType,
                config, eventBus, interceptorChain, lifecycleManager,
                replanning, replanLimit);
        }
    }
}
