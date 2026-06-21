export default function Architecture() {
  return (
    <>
      <h1>Architecture</h1>

      <h2>Design Philosophy</h2>
      <p>
        Astra follows a <strong>modular, interface-driven</strong> architecture. Core abstractions
        live in <code>astra-api</code>; implementations live in separate modules. This design
        enables swapping implementations without changing client code, and allows third-party
        extensions via the SPI plugin system.
      </p>

      <h2>Module Reference</h2>

      <h3>astra-annotations</h3>
      <p>
        Defines all annotations used to declare agents. Divided into sub-packages:
      </p>
      <ul>
        <li><code>io.astra.annotation</code> — <code>@Agent</code></li>
        <li><code>io.astra.annotation.action</code> — <code>@Action</code>, <code>@Actions</code></li>
        <li><code>io.astra.annotation.goal</code> — <code>@Goal</code>, <code>@Goals</code></li>
        <li><code>io.astra.annotation.decomposition</code> — <code>@CompoundTask</code>, <code>@CompoundTasks</code>, <code>@Decomposition</code>, <code>@Decompositions</code></li>
        <li><code>io.astra.annotation.fact</code> — <code>@Fact</code>, <code>@Facts</code></li>
      </ul>

      <h3>astra-api</h3>
      <p>
        The central API module. Contains all interfaces and data classes that both the framework
        and client code depend on:
      </p>
      <ul>
        <li><strong>Core:</strong> <code>Astra</code>, <code>Planner</code>, <code>Plan</code>, <code>ActionInfo</code>, <code>GoalInfo</code></li>
        <li><strong>State:</strong> <code>WorldState</code>, <code>WorldStates</code></li>
        <li><strong>SPI:</strong> <code>PlannerType</code>, <code>PlannerProvider</code></li>
        <li><strong>Decomposition:</strong> <code>CompoundTaskDef</code>, <code>DecompositionDef</code></li>
        <li><strong>Config:</strong> <code>AstraConfig</code>, <code>ConfigProvider</code></li>
        <li><strong>Events:</strong> <code>EventBus</code>, <code>AstraEvent</code>, <code>AstraEventType</code>, <code>AstraEventListener</code></li>
        <li><strong>Interceptors:</strong> <code>InterceptorChain</code>, <code>ActionInterceptor</code></li>
        <li><strong>Lifecycle:</strong> <code>LifecycleAware</code>, <code>AgentState</code></li>
        <li><strong>Results:</strong> <code>ExecutionResult</code>, <code>ActionExecutionRecord</code></li>
      </ul>

      <h3>astra-utils</h3>
      <p>
        Generic utilities used across modules:
      </p>
      <ul>
        <li><code>ClassPathScanner</code> — scans packages for annotated classes (used by <code>Builder.scan()</code>)</li>
        <li><code>WorldStateSerializer</code> — Jackson-based JSON serialization for <code>WorldState</code> (requires <code>jackson-databind</code> on classpath)</li>
      </ul>

      <h3>astra-scanner</h3>
      <p>
        Contains <code>AgentScanner</code>, which reflects on an agent instance's annotations at registration time
        and produces <code>List&lt;ActionInfo&gt;</code>, <code>List&lt;GoalInfo&gt;</code>, and
        <code>List&lt;CompoundTaskDef&gt;</code> used by the planners.
      </p>

      <h3>astra-config</h3>
      <p>Configuration providers for planner settings:</p>
      <ul>
        <li><code>MapConfigProvider</code> — in-memory map-based configuration (used by the builder)</li>
        <li><code>PropertiesFileConfigProvider</code> — loads configuration from a <code>.properties</code> file</li>
      </ul>

      <h3>astra-exceptions</h3>
      <p>Typed exception hierarchy with sub-packages by domain:</p>
      <ul>
        <li><code>io.astra.exception</code> — <code>AstraException</code> (base class, extends <code>RuntimeException</code>)</li>
        <li><code>io.astra.exception.action</code> — <code>ActionExecutionException</code></li>
        <li><code>io.astra.exception.agent</code> — <code>AgentRegistrationException</code></li>
        <li><code>io.astra.exception.goal</code> — <code>GoalNotFoundException</code>, <code>DuplicateGoalException</code>, <code>PlanNotFoundException</code></li>
        <li><code>io.astra.exception.state</code> — <code>InvalidStateException</code></li>
      </ul>

      <h3>astra-events</h3>
      <p>
        <code>DefaultEventBus</code> — a thread-safe publish-subscribe event bus backed by
        <code>CopyOnWriteArrayList</code>. Supports dynamic subscription and unsubscription
        at runtime.
      </p>

      <h3>astra-interceptors</h3>
      <p>
        <code>DefaultInterceptorChain</code> — manages a chain of <code>ActionInterceptor</code>
        instances. Each interceptor receives callbacks before and after every action execution,
        plus error notifications.
      </p>

      <h3>astra-lifecycle</h3>
      <p>
        <code>LifecycleManager</code> — invokes <code>onInit()</code> on all registered
        <code>LifecycleAware</code> agents when registered, and <code>onDestroy()</code>
        when the framework shuts down.
      </p>

      <h3>astra-planners</h3>
      <p>
        Contains all planner implementations plus their SPI provider registrations.
        Each concrete planner resides in its own sub-package:
      </p>
      <ul>
        <li>Cost-based planner implementation</li>
        <li>Utility-based planner implementation</li>
        <li>Hybrid planner implementation</li>
        <li>Structural planner implementation</li>
        <li>SPI provider registrations for <code>ServiceLoader</code> discovery</li>
        <li><code>DefaultPlan</code> — shared plan data class</li>
      </ul>
      <p>The SPI registration file at <code>META-INF/services/io.astra.api.PlannerProvider</code> lists all providers for <code>ServiceLoader</code> discovery.</p>

      <h3>astra-validation</h3>
      <p>
        Pre-action validation framework. <code>DefaultValidator</code> checks world state for null values
        and empty strings before execution. <code>ValidationInterceptor</code> hooks into the interceptor
        chain and throws <code>IllegalArgumentException</code> on validation failure, with an
        accompanying <code>VALIDATION_FAILED</code> event.
      </p>

      <h3>astra-store</h3>
      <p>
        World state persistence. <code>WorldStateStore</code> interface with two implementations:
      </p>
      <ul>
        <li><code>InMemoryWorldStateStore</code> — <code>ConcurrentHashMap</code>-backed, ephemeral storage</li>
        <li><code>FileWorldStateStore</code> — persists states as <code>.properties</code> files on disk</li>
      </ul>

      <h3>astra-agentbus</h3>
      <p>
        Inter-agent communication. <code>DefaultAgentBus</code> provides publish-subscribe messaging
        between named agents. Supports direct <code>send()</code>, <code>broadcast()</code>, and
        topic-based subscriptions. Each message publishes an <code>AGENT_MESSAGE</code> event.
      </p>

      <h3>astra-mcp</h3>
      <p>
        Model Context Protocol (MCP) support. <code>DefaultMcpServer</code> exposes agent actions as
        MCP tools with JSON Schema input definitions. <code>DefaultMcpClient</code> connects to remote
        MCP servers to discover and invoke tools remotely.
      </p>

      <h3>astra-telemetry</h3>
      <p>
        Observability infrastructure:
      </p>
      <ul>
        <li><code>Slf4jTracer</code> — traces planning and action execution via SLF4J</li>
        <li><code>SimpleMetricsCollector</code> — in-memory counters, gauges, and timers</li>
        <li><code>TelemetryInterceptor</code> — automatically wires into the action pipeline</li>
      </ul>

      <h3>astra-core</h3>
      <p>
        The central runtime module. <code>DefaultAstra</code> implements the <code>Astra</code>
        interface and provides:
      </p>
      <ul>
        <li>Inner <code>Builder</code> class with fluent configuration API</li>
        <li>Planner discovery via <code>ServiceLoader</code></li>
        <li>Action execution loop with event publishing and interceptor invocation</li>
        <li>Async execution via <code>CompletableFuture</code></li>
        <li>OODA-loop replanning with configurable replan limit</li>
        <li>Natural-language query routing with keyword scoring</li>
        <li>State rollback via <code>StateHistory</code> with snapshot/restore</li>
        <li>Plan caching via <code>LruPlanCache</code></li>
        <li>Policy enforcement via <code>PolicyInterceptor</code> + <code>DefaultPolicyChecker</code></li>
        <li>Skills framework via <code>SkillManager</code></li>
        <li>Composite agent support via <code>DefaultCompositeAgent</code></li>
        <li>Goal choice approval via <code>ConsoleGoalChoiceApprover</code></li>
        <li>Scheduling via <code>DefaultSchedulerService</code></li>
        <li>Interactive REPL via <code>DefaultRepl</code></li>
        <li>Synchronized data structures for thread safety</li>
      </ul>

      <h3>astra-spring</h3>
      <p>
        Spring Boot auto-configuration module. The <code>@EnableAstra</code> annotation activates
        <code>AstraAutoConfiguration</code>, which creates and configures an <code>Astra</code>
        bean in the Spring application context.
      </p>

      <h3>astra-sample</h3>
      <p>
        Demo applications:
      </p>
      <ul>
        <li><code>CoffeeAgent</code> — demonstrates cost-based, utility-based, and hybrid planning with a coffee-making scenario. Includes a dynamic utility method (<code>getBrewUtility</code>).</li>
        <li><code>CookingAgent</code> — demonstrates structural decomposition planning with a dinner-making scenario. Includes compound tasks with multiple decomposition methods and conditional branching.</li>
        <li><code>Main</code> — orchestrates all planner demos in sequence.</li>
      </ul>

      <h3>astra-tests</h3>
      <p>JUnit 5 test suite with 12 tests covering all planners, including edge cases like no-path scenarios and precondition-based branching.</p>

      <h2>Dependency Graph</h2>
      <p>The diagram below shows the module hierarchy. Arrows indicate dependencies — each module depends on <code>astra-api</code>. New modules are highlighted in red.</p>
      <div style={{textAlign: 'center', margin: '24px 0'}}>
        <img src="architecture.svg" alt="Astra module architecture diagram" style={{maxWidth: '100%', borderRadius: 12, boxShadow: '0 4px 24px rgba(0,0,0,0.3)'}} />
      </div>

      <h2>Planning Pipeline</h2>
      <ol>
        <li><strong>Agent registration</strong> — <code>Builder.register(agent)</code> invokes <code>AgentScanner</code> to scan the agent class for <code>@Action</code>, <code>@Goal</code>, <code>@CompoundTask</code>, and <code>@Decomposition</code> annotations. Results are stored as <code>ActionInfo</code>, <code>GoalInfo</code>, and <code>CompoundTaskDef</code> instances.</li>
        <li><strong>Planner instantiation</strong> — <code>Builder.build()</code> creates the selected planner via <code>ServiceLoader</code>. The planner receives configuration, actions, and compound tasks.</li>
        <li><strong>Planning</strong> — <code>Astra.plan(goalName, initialState)</code> calls the planner's <code>plan()</code> method, which returns a <code>Plan</code> containing ordered actions and total cost.</li>
        <li><strong>Execution</strong> — <code>Astra.executeWithResult(goalName, initialState)</code> iterates through the plan actions, applying effects to the world state after each step.</li>
        <li><strong>Event publishing</strong> — Events are fired at every stage (plan started, plan completed, action before/after, goal satisfied). Interceptors are called for each action.</li>
        <li><strong>Result</strong> — An <code>ExecutionResult</code> is returned with success status, final state, per-action records, and total duration.</li>
      </ol>

      <h2>Thread Safety</h2>
      <ul>
        <li><strong>Builder maps</strong> use <code>ConcurrentHashMap</code> for concurrent agent registration</li>
        <li><strong>Action lists</strong> use <code>CopyOnWriteArrayList</code> for safe iteration during planning</li>
        <li><strong>Event bus subscribers</strong> use <code>CopyOnWriteArrayList</code> for safe dynamic subscription</li>
        <li><strong>Async execution</strong> uses <code>CompletableFuture.supplyAsync()</code> for non-blocking operation</li>
      </ul>

      <h2>Event Types</h2>
      <table>
        <thead><tr><th>Event</th><th>When fired</th></tr></thead>
        <tbody>
          <tr><td><code>AGENT_REGISTERED</code></td><td>After an agent is registered via the builder</td></tr>
          <tr><td><code>AGENT_REMOVED</code></td><td>When an agent is removed</td></tr>
          <tr><td><code>AGENT_MESSAGE</code></td><td>When an agent sends a message via AgentBus</td></tr>
          <tr><td><code>PLAN_STARTED</code></td><td>When <code>executeWithResult</code> begins</td></tr>
          <tr><td><code>PLAN_COMPLETED</code></td><td>When a plan is successfully found</td></tr>
          <tr><td><code>PLAN_FAILED</code></td><td>When no plan could be found</td></tr>
          <tr><td><code>ACTION_BEFORE</code></td><td>Just before an action is executed</td></tr>
          <tr><td><code>ACTION_AFTER</code></td><td>Immediately after a successful action execution</td></tr>
          <tr><td><code>ACTION_FAILED</code></td><td>When an action throws an exception during execution</td></tr>
          <tr><td><code>GOAL_SATISFIED</code></td><td>When the final state matches the goal condition</td></tr>
          <tr><td><code>GOAL_UNSATISFIABLE</code></td><td>When the final state does not match the goal condition</td></tr>
          <tr><td><code>VALIDATION_FAILED</code></td><td>When action preconditions fail validation</td></tr>
          <tr><td><code>POLICY_DENIED</code></td><td>When a policy check blocks an action</td></tr>
          <tr><td><code>SKILL_LOADED</code></td><td>When a skill is loaded via SkillManager</td></tr>
          <tr><td><code>SKILL_UNLOADED</code></td><td>When a skill is unloaded</td></tr>
          <tr><td><code>SCHEDULED_TASK_TRIGGERED</code></td><td>When a scheduled action fires</td></tr>
          <tr><td><code>MCP_TOOL_CALLED</code></td><td>When an MCP tool is invoked</td></tr>
          <tr><td><code>STATE_SNAPSHOT</code></td><td>When a state snapshot is taken</td></tr>
          <tr><td><code>STATE_ROLLBACK</code></td><td>When a state is rolled back</td></tr>
        </tbody>
      </table>
    </>
  )
}
