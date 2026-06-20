export default function Api() {
  return (
    <>
      <h1>API Reference</h1>

      <h2>Annotations</h2>

      <h3>@Agent</h3>
      <p>Marks a class as an agent. Required for <code>AgentScanner</code> discovery.</p>
      <pre><code>{`@Retention(RUNTIME)
@Target(TYPE)
public @interface Agent {}`}</code></pre>

      <h3>@Action</h3>
      <p>Marks a method as an action that a planner can use.</p>
      <pre><code>{`@Repeatable(Actions.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface Action {
    String name() default "";
    String description() default "";
    Fact[] preconditions() default {};
    Fact[] effects() default {};
    float cost() default 1.0f;
    float utility() default 0.5f;
    String utilityMethod() default "";
}`}</code></pre>

      <h3>@Goal</h3>
      <p>Defines a goal condition. The condition is a set of world-state facts that must be <code>true</code>.</p>
      <pre><code>{`@Repeatable(Goals.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface Goal {
    String name() default "";
    String description() default "";
    Fact[] condition() default {};
}`}</code></pre>

      <h3>@CompoundTask / @Decomposition</h3>
      <p>Defines a hierarchical task with one or more decomposition methods. Each method lists subtasks (primitives or other compound tasks) and optional preconditions.</p>
      <pre><code>{`@Repeatable(CompoundTasks.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface CompoundTask {
    String name() default "";
    String description() default "";
}

@Repeatable(Decompositions.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface Decomposition {
    String name() default "";
    String description() default "";
    Fact[] preconditions() default {};
    String[] subtasks();
}`}</code></pre>

      <h3>@Fact</h3>
      <p>A key-value pair representing a single world-state fact.</p>
      <pre><code>{`@Retention(RUNTIME)
@Target({})
public @interface Fact {
    String name();
    String value();
}`}</code></pre>

      <h2>Core Interfaces</h2>

      <h3>Astra</h3>
      <p>The main entry point. Created via <code>DefaultAstra.builder()</code>.</p>
      <pre><code>{`public interface Astra {
    Plan plan(String goalName, WorldState initialState);
    WorldState execute(String goalName, WorldState initialState);
    ExecutionResult executeWithResult(String goalName, WorldState initialState);
    CompletableFuture<ExecutionResult> executeAsync(String goalName, WorldState initialState);
    EventBus getEventBus();
    InterceptorChain getInterceptorChain();
    AstraConfig getConfig();
}`}</code></pre>

      <h3>GoapPlanner</h3>
      <p>Implemented by all planners. Registered via <code>PlannerProvider</code> SPI.</p>
      <pre><code>{`public interface GoapPlanner {
    Plan plan(WorldState currentState, GoalInfo goal, List<ActionInfo> actions);
}`}</code></pre>

      <h3>WorldState</h3>
      <p>Immutable key-value state used throughout the planning and execution pipeline.</p>
      <pre><code>{`public interface WorldState {
    Optional<String> get(String key);
    boolean matches(Map<String, String> conditions);
    Map<String, String> asMap();
    WorldState set(String key, String value);
}`}</code></pre>
      <p>Create instances via <code>WorldStates.of(key, value)</code> or <code>WorldStates.of(key1, val1, key2, val2, ...)</code>.</p>

      <h3>ActionInfo</h3>
      <p>Describes an action to the planner. Created by <code>AgentScanner</code> from <code>@Action</code> annotations.</p>
      <pre><code>{`public interface ActionInfo {
    String getName();
    Map<String, String> getPreconditions();
    Map<String, String> getEffects();
    float getCost();
    Runnable getExecutor();
    float getUtility();
    String getUtilityMethod();
    String getDescription();
    double computeUtility(WorldState state);
}`}</code></pre>

      <h3>Plan</h3>
      <p>Returned by <code>GoapPlanner.plan()</code>. Contains ordered actions and total cost.</p>
      <pre><code>{`public interface Plan {
    List<ActionInfo> getActions();
    double getTotalCost();
    boolean isExecutable();
}`}</code></pre>

      <h3>ExecutionResult</h3>
      <p>Returned by <code>Astra.executeWithResult()</code>. Contains the final state, action records, and success status.</p>
      <pre><code>{`public record ExecutionResult(
    boolean isSuccess,
    Plan plan,
    WorldState finalState,
    List<ActionExecutionRecord> actionRecords,
    long totalDurationMs
) { }`}</code></pre>

      <h2>Config</h2>
      <table>
        <thead>
          <tr><th>Property</th><th>Default</th><th>Description</th></tr>
        </thead>
        <tbody>
          <tr><td><code>astra.planner.maxIterations</code></td><td>10000 (GOAP), 20 (Utility), 100 (Hybrid), 200 (HTN)</td><td>Max search/planning iterations</td></tr>
          <tr><td><code>astra.planner.heuristic</code></td><td><code>unmatched</code></td><td>GOAP heuristic: <code>unmatched</code> or <code>zero</code></td></tr>
        </tbody>
      </table>

      <h2>SPI</h2>
      <p>Register custom planners via <code>PlannerProvider</code>:</p>
      <pre><code>{`public interface PlannerProvider {
    PlannerType type();
    GoapPlanner create(AstraConfig config,
                       Map<String, CompoundTaskDef> compoundTasks,
                       List<ActionInfo> actions);
}`}</code></pre>
      <p>Add a <code>META-INF/services/io.astra.api.PlannerProvider</code> file listing your implementation class.</p>
    </>
  )
}
