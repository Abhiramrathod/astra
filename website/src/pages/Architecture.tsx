export default function Architecture() {
  return (
    <>
      <h1>Architecture</h1>

      <h2>Module Overview</h2>
      <p>Astra is organized into 14 Maven modules, each with a single responsibility:</p>

      <table>
        <thead>
          <tr><th>Module</th><th>Artifact</th><th>Description</th></tr>
        </thead>
        <tbody>
          <tr><td>Annotations</td><td><code>astra-annotations</code></td><td><code>@Agent</code>, <code>@Action</code>, <code>@Goal</code>, <code>@CompoundTask</code>, <code>@Decomposition</code>, <code>@Fact</code></td></tr>
          <tr><td>API</td><td><code>astra-api</code></td><td>Core interfaces: <code>Astra</code>, <code>GoapPlanner</code>, <code>ActionInfo</code>, <code>WorldState</code>, <code>EventBus</code>, <code>PlannerType</code>, <code>PlannerProvider</code></td></tr>
          <tr><td>Utils</td><td><code>astra-utils</code></td><td><code>ClassPathScanner</code>, <code>WorldStateSerializer</code> (Jackson)</td></tr>
          <tr><td>Scanner</td><td><code>astra-scanner</code></td><td><code>AgentScanner</code> — reflects annotations into API type instances</td></tr>
          <tr><td>Config</td><td><code>astra-config</code></td><td><code>MapConfigProvider</code>, <code>PropertiesFileConfigProvider</code></td></tr>
          <tr><td>Exceptions</td><td><code>astra-exceptions</code></td><td>Typed exception hierarchy: <code>ActionExecutionException</code>, <code>GoalNotFoundException</code>, etc.</td></tr>
          <tr><td>Events</td><td><code>astra-events</code></td><td><code>DefaultEventBus</code> implementation</td></tr>
          <tr><td>Interceptors</td><td><code>astra-interceptors</code></td><td><code>DefaultInterceptorChain</code> implementation</td></tr>
          <tr><td>Lifecycle</td><td><code>astra-lifecycle</code></td><td><code>LifecycleManager</code> — calls <code>onInit</code>/<code>onDestroy</code></td></tr>
          <tr><td>Planners</td><td><code>astra-planners</code></td><td>All four planner implementations + SPI provider registrations</td></tr>
          <tr><td>Core</td><td><code>astra-core</code></td><td><code>DefaultAstra</code> — the main builder and runtime</td></tr>
          <tr><td>Spring</td><td><code>astra-spring</code></td><td>Spring Boot auto-configuration with <code>@EnableAstra</code></td></tr>
          <tr><td>Sample</td><td><code>astra-sample</code></td><td>Demos: <code>CoffeeAgent</code>, <code>CookingAgent</code></td></tr>
          <tr><td>Tests</td><td><code>astra-tests</code></td><td>JUnit 5 test suite (12 tests covering all planners)</td></tr>
        </tbody>
      </table>

      <h2>Build Order</h2>
      <p>The dependency DAG enforces this build order:</p>
      <pre><code>annotations → api → utils + scanner + config + exceptions
    + events + interceptors + lifecycle + planners
    → core → spring → sample → tests</code></pre>

      <h2>How Planning Works</h2>
      <ol>
        <li><strong>Registration</strong> — <code>DefaultAstra.builder().register(agent)</code> calls <code>AgentScanner</code> to reflect annotations into <code>ActionInfo</code>, <code>GoalInfo</code>, and <code>CompoundTaskDef</code> objects.</li>
        <li><strong>Planner selection</strong> — The builder instantiates the chosen planner via <code>ServiceLoader</code> (SPI).</li>
        <li><strong>Planning</strong> — <code>astra.plan(goalName, initialState)</code> delegates to the planner's <code>plan()</code> method, which returns a <code>Plan</code> of ordered actions.</li>
        <li><strong>Execution</strong> — <code>astra.executeWithResult(goalName, initialState)</code> iterates through the plan, invoking each action's executor, applying effects to the world state, and publishing events through the event bus and interceptor chain.</li>
      </ol>

      <h2>Event Bus</h2>
      <p>Events are published at each stage of the planning and execution cycle:</p>
      <ul>
        <li><code>AGENT_REGISTERED</code> — an agent is registered</li>
        <li><code>PLAN_STARTED</code> / <code>PLAN_COMPLETED</code> / <code>PLAN_FAILED</code> — planning lifecycle</li>
        <li><code>ACTION_BEFORE</code> / <code>ACTION_AFTER</code> / <code>ACTION_FAILED</code> — per-action lifecycle</li>
        <li><code>GOAL_SATISFIED</code> / <code>GOAL_UNSATISFIABLE</code> — goal resolution</li>
      </ul>

      <h2>Synchronization</h2>
      <p>All builder maps use <code>ConcurrentHashMap</code> and <code>CopyOnWriteArrayList</code>. The <code>DefaultEventBus</code> uses <code>CopyOnWriteArrayList</code> for thread-safe subscriber management.</p>
    </>
  )
}
