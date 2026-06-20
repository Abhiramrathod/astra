export default function Api() {
  return (
    <>
      <h1>API Reference</h1>

      <h2>Annotations</h2>

      <h3>@Agent</h3>
      <p>Marks a class as an agent. Required for scanner-based discovery.</p>
      <pre><code>{`@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Agent {}`}</code></pre>

      <h3>@Action</h3>
      <p>Marks a method as a planner action. Repeatable.</p>
      <pre><code>{`@Repeatable(Actions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {
    String name() default "";
    String description() default "";
    Fact[] preconditions() default {};
    Fact[] effects() default {};
    float cost() default 1.0f;
    float utility() default 0.5f;
    String utilityMethod() default "";
}`}</code></pre>

      <h3>@Actions</h3>
      <p>Container for repeatable <code>@Action</code> annotations.</p>
      <pre><code>{`@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Actions {
    Action[] value();
}`}</code></pre>

      <h3>@Goal</h3>
      <p>Defines a goal with a name and a target condition. Repeatable.</p>
      <pre><code>{`@Repeatable(Goals.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Goal {
    String name() default "";
    String description() default "";
    Fact[] condition() default {};
}`}</code></pre>

      <h3>@CompoundTask</h3>
      <p>Defines a compound task for structural decomposition. Repeatable.</p>
      <pre><code>{`@Repeatable(CompoundTasks.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompoundTask {
    String name() default "";
    String description() default "";
}`}</code></pre>

      <h3>@Decomposition</h3>
      <p>Defines one way to decompose a compound task. Repeatable.</p>
      <pre><code>{`@Repeatable(Decompositions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Decomposition {
    String name() default "";
    String description() default "";
    Fact[] preconditions() default {};
    String[] subtasks();
}`}</code></pre>

      <h3>@Fact</h3>
      <p>A key-value pair representing a world state fact.</p>
      <pre><code>{`@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Fact {
    String name();
    String value();
}`}</code></pre>

      <h2>Core Interfaces</h2>

      <h3>Astra</h3>
      <p>Main entry point for planning and execution.</p>
      <pre><code>{`public interface Astra {
    Plan plan(String goalName, WorldState initialState);
    WorldState execute(String goalName, WorldState initialState);
    ExecutionResult executeWithResult(String goalName, WorldState initialState);
    CompletableFuture<ExecutionResult> executeAsync(String goalName, WorldState initialState);
    EventBus getEventBus();
    InterceptorChain getInterceptorChain();
    AstraConfig getConfig();
}`}</code></pre>

      <table>
        <thead><tr><th>Method</th><th>Returns</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>plan(goalName, initialState)</code></td><td><code>Plan</code></td><td>Generate a plan without executing it</td></tr>
          <tr><td><code>execute(goalName, initialState)</code></td><td><code>WorldState</code></td><td>Generate and execute a plan, return final state</td></tr>
          <tr><td><code>executeWithResult(goalName, initialState)</code></td><td><code>ExecutionResult</code></td><td>Execute and return detailed result with records</td></tr>
          <tr><td><code>executeAsync(goalName, initialState)</code></td><td><code>CompletableFuture&lt;ExecutionResult&gt;</code></td><td>Async execution on a separate thread</td></tr>
        </tbody>
      </table>

      <h3>Planner</h3>
      <p>Implemented by all planners. The core planning contract.</p>
      <pre><code>{`public interface Planner {
    Plan plan(WorldState currentState,
              GoalInfo goal,
              List<ActionInfo> actions);
}`}</code></pre>

      <h3>ActionInfo</h3>
      <p>Describes an action to the planner.</p>
      <pre><code>{`public interface ActionInfo {
    String getName();
    String getDescription();
    Map<String, String> getPreconditions();
    Map<String, String> getEffects();
    float getCost();
    float getUtility();
    String getUtilityMethod();
    Runnable getExecutor();
    double computeUtility(WorldState state);
}`}</code></pre>

      <h3>GoalInfo</h3>
      <p>Describes a goal to the planner.</p>
      <pre><code>{`public interface GoalInfo {
    String getName();
    String getDescription();
    Map<String, String> getCondition();
}`}</code></pre>

      <h3>Plan</h3>
      <p>The result of planning. Contains ordered actions and total cost.</p>
      <pre><code>{`public interface Plan {
    List<ActionInfo> getActions();
    double getTotalCost();
    boolean isExecutable();
}`}</code></pre>

      <h3>WorldState</h3>
      <p>Immutable key-value state representation. Used throughout planning and execution.</p>
      <pre><code>{`public interface WorldState {
    Optional<String> get(String key);
    boolean matches(Map<String, String> conditions);
    Map<String, String> asMap();
    WorldState set(String key, String value);
}`}</code></pre>
      <p>Create instances with <code>WorldStates</code> factory methods:</p>
      <pre><code>{`WorldStates.empty()
WorldStates.of(Map<String, String>)
WorldStates.of(String key, String value)
WorldStates.of(String k1, String v1, String k2, String v2, ...)`}</code></pre>

      <h3>WorldStates</h3>
      <p>Factory for creating <code>WorldState</code> instances. Uses <code>SimpleWorldState</code>
      (a private inner class) that provides consistent <code>equals</code> and <code>hashCode</code>
      based on the underlying map.</p>

      <h2>Result Types</h2>

      <h3>ExecutionResult</h3>
      <p>Contains the full outcome of an execution cycle.</p>
      <pre><code>{`public record ExecutionResult(
    boolean isSuccess,
    Plan plan,
    WorldState finalState,
    List<ActionExecutionRecord> actionRecords,
    long totalDurationMs
) {
    static ExecutionResult success(Plan plan, WorldState finalState,
                                   List<ActionExecutionRecord> records,
                                   long duration);
    static ExecutionResult failure(Plan plan, WorldState finalState,
                                   List<ActionExecutionRecord> records,
                                   long duration, String errorMessage);
}`}</code></pre>

      <h3>ActionExecutionRecord</h3>
      <p>Per-action execution details.</p>
      <pre><code>{`public record ActionExecutionRecord(
    ActionInfo action,
    WorldState stateBefore,
    WorldState stateAfter,
    boolean isSuccess,
    long durationMs,
    String errorMessage
) {}`}</code></pre>

      <h2>Config</h2>

      <h3>AstraConfig</h3>
      <p>Configuration interface for planner settings.</p>
      <pre><code>{`public interface AstraConfig {
    <T> T getOrDefault(String key, Class<T> type, T defaultValue);
}`}</code></pre>

      <h3>ConfigProvider</h3>
      <pre><code>{`public interface ConfigProvider extends AstraConfig {
    void set(String key, String value);
    String get(String key);
}`}</code></pre>

      <h2>Event System</h2>

      <h3>EventBus</h3>
      <pre><code>{`public interface EventBus {
    void publish(AstraEvent event);
    void subscribe(AstraEventListener listener);
    void unsubscribe(AstraEventListener listener);
}`}</code></pre>

      <h3>AstraEvent</h3>
      <pre><code>{`public record AstraEvent(
    AstraEventType type,
    Map<String, Object> data,
    long timestamp
) {
    static AstraEvent of(AstraEventType type, Object... keyValues);
}`}</code></pre>

      <h3>AstraEventType</h3>
      <pre><code>{`public enum AstraEventType {
    AGENT_REGISTERED,
    PLAN_STARTED, PLAN_COMPLETED, PLAN_FAILED,
    ACTION_BEFORE, ACTION_AFTER, ACTION_FAILED,
    GOAL_SATISFIED, GOAL_UNSATISFIABLE
}`}</code></pre>

      <h2>Interceptor System</h2>

      <h3>InterceptorChain</h3>
      <pre><code>{`public interface InterceptorChain {
    void addInterceptor(ActionInterceptor interceptor);
    void beforeAction(ActionInfo action, WorldState state);
    void afterAction(ActionInfo action, WorldState before, WorldState after);
    void onError(ActionInfo action, WorldState state, Exception error);
}`}</code></pre>

      <h3>ActionInterceptor</h3>
      <pre><code>{`public interface ActionInterceptor {
    default void beforeAction(ActionInfo action, WorldState state) {}
    default void afterAction(ActionInfo action, WorldState before, WorldState after) {}
    default void onError(ActionInfo action, WorldState state, Exception error) {}
}`}</code></pre>

      <h2>Lifecycle</h2>

      <h3>LifecycleAware</h3>
      <pre><code>{`public interface LifecycleAware {
    default void onInit() {}
    default void onDestroy() {}
}`}</code></pre>

      <h3>AgentState</h3>
      <pre><code>{`public enum AgentState {
    CREATED, INITIALIZED, ACTIVE, DESTROYED
}`}</code></pre>

      <h2>SPI Types</h2>

      <h3>PlannerType</h3>
      <pre><code>{`public enum PlannerType {
    COST_BASED, UTILITY_BASED, HYBRID, STRUCTURAL
}`}</code></pre>

      <h3>PlannerProvider</h3>
      <pre><code>{`public interface PlannerProvider {
    PlannerType type();
    Planner create(AstraConfig config,
                       Map<String, CompoundTaskDef> compoundTasks,
                       List<ActionInfo> actions);
}`}</code></pre>

      <h2>Decomposition Types</h2>

      <h3>CompoundTaskDef</h3>
      <pre><code>{`public class CompoundTaskDef {
    String getName();
    String getDescription();
    List<DecompositionDef> getDecompositions();
}`}</code></pre>

      <h3>DecompositionDef</h3>
      <pre><code>{`public class DecompositionDef {
    String getName();
    String getDescription();
    Map<String, String> getPreconditions();
    List<String> getSubtasks();
}`}</code></pre>

      <h2>Exception Hierarchy</h2>
      <pre><code>{`AstraException (RuntimeException)
├── ActionExecutionException    (action execution failure)
├── AgentRegistrationException  (agent registration failure)
├── DuplicateGoalException      (duplicate goal name)
├── GoalNotFoundException       (unknown goal name)
├── PlanNotFoundException       (no plan for goal+state)
└── InvalidStateException       (invalid world state)`}</code></pre>

      <h2>Builder API</h2>
      <pre><code>{`DefaultAstra.builder()
    .register(agent)                            // Register a single agent
    .register(agent1, agent2)                   // Register multiple agents
    .scan("com.myapp.agents")                   // Scan package for @Agent classes
    .withPlanner(PlannerType.COST_BASED)        // Select planner (default: cost-based)
    .withConfig("key", "value")                 // Set a config value
    .withConfig(Map.of("k1", "v1"))             // Set multiple config values
    .withConfig(configProvider)                 // Use a custom config provider
    .withEventListener(listener)                // Subscribe to events
    .withInterceptor(interceptor)               // Add an action interceptor
    .build()                                    // Create the Astra instance`}</code></pre>
    </>
  )
}
