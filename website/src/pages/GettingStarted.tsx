export default function GettingStarted() {
  return (
    <>
      <h1>Getting Started</h1>

      <h2>Prerequisites</h2>
      <ul>
        <li><strong>Java 17+</strong> — Astra uses records, sealed classes, pattern matching, and other modern features</li>
        <li><strong>Maven 3.8+</strong> — or use the bundled <code>./mvnw</code> wrapper</li>
      </ul>

      <h2>Installation</h2>

      <h3>Maven</h3>
      <pre><code>{`<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-core</artifactId>
    <version>0.1.0</version>
</dependency>`}</code></pre>
        <p>This single dependency pulls in everything you need: <code>astra-api</code> (interfaces), <code>astra-planners</code> (all planner implementations), <code>astra-scanner</code> (annotation processing), <code>astra-utils</code>, <code>astra-config</code>, <code>astra-exceptions</code>, <code>astra-events</code>, <code>astra-interceptors</code>, <code>astra-lifecycle</code>, <code>astra-validation</code>, <code>astra-store</code>, <code>astra-agentbus</code>, <code>astra-mcp</code>, and <code>astra-telemetry</code>.</p>

      <h3>Gradle</h3>
      <pre><code>{`implementation 'io.astra:astra-core:0.1.0'`}</code></pre>

      <h3>Optional modules</h3>
      <table>
        <thead><tr><th>Artifact</th><th>When to add</th></tr></thead>
        <tbody>
          <tr><td><code>astra-spring</code></td><td>Spring Boot applications — enables <code>@EnableAstra</code></td></tr>
          <tr><td><code>jackson-databind</code> (via <code>astra-utils</code>)</td><td>JSON serialization of world states</td></tr>
        </tbody>
      </table>
      <p>Other modules like <code>astra-validation</code>, <code>astra-store</code>, <code>astra-agentbus</code>, <code>astra-mcp</code>, and <code>astra-telemetry</code> are included transitively by <code>astra-core</code> and need no extra dependency declaration.</p>

      <h2>Quick Start — Simplified DX</h2>
      <p>No annotations, no goals to define, no builder chain. Extend <code>AgentBase</code> and write plain public void methods:</p>
      <pre><code>{`import io.astra.api.AgentBase;
import io.astra.core.DefaultAstra;

public class SimpleAgent extends AgentBase {
    public void fetchData() {
        System.out.println("Fetching data...");
    }
    public void analyze() {
        System.out.println("Analyzing...");
    }
}

// One-liner:
Astra astra = DefaultAstra.simple(new SimpleAgent());
astra.execute("_run_all", WorldStates.empty());`}</code></pre>
      <p>Public void no-arg methods become convention actions automatically. Goals are auto-derived. Or use the programmatic API on <code>AgentBase</code>:</p>
      <pre><code>{`AgentBase agent = new AgentBase() {{
    addAction("fetch", () -> System.out.println("Fetching..."));
    addAction("process", () -> System.out.println("Processing..."),
        Map.of("raw", "true"), Map.of("processed", "true"), 2.0f);
    addGoal("done", Map.of("processed", "true"));
}};
Astra astra = DefaultAstra.builder()
    .register(agent)
    .withPlanner(PlannerType.COST_BASED)
    .build();
ExecutionResult result = astra.executeWithResult("done",
    WorldStates.of("raw", "true"));`}</code></pre>

      <h2>Core Concepts</h2>

      <p>Astra models planning as a state-transition problem. An <strong>agent</strong> operates in a <strong>world state</strong> and tries to reach a <strong>goal</strong> by executing <strong>actions</strong>. Each action declares what it needs (preconditions) and what it changes (effects). A <strong>planner</strong> searches for the cheapest or best sequence of actions to get from the current state to a state that satisfies the goal.</p>

      <h3>Fact</h3>
      <p>A <code>@Fact</code> is the smallest unit of state — a key-value pair. Facts are used everywhere: as preconditions ("this action requires <code>hasBeans=true</code>"), as effects ("after grinding, <code>beansGround=true</code>"), and as goal conditions ("goal achieved when <code>coffeeServed=true</code>").</p>
      <pre><code>{`@Fact(name = "hasBeans", value = "true")`}</code></pre>
      <p>Multiple facts are composed using the array syntax:</p>
      <pre><code>{`preconditions = {
    @Fact(name = "hasBeans", value = "true"),
    @Fact(name = "waterBoiled", value = "true")
}`}</code></pre>

      <h3>WorldState</h3>
      <p>The <code>WorldState</code> is an immutable snapshot of all currently known facts. It is passed through the planning pipeline — the planner checks preconditions against it, and actions update it with their effects. Create initial states with the <code>WorldStates</code> factory:</p>
      <pre><code>{`WorldState state = WorldStates.of(
    "hasBeans", "true",
    "waterBoiled", "false"
);
state.get("hasBeans");          // Optional["true"]
state.matches(conditions);      // boolean — are all conditions satisfied?
state.set("waterBoiled", "true"); // returns a NEW state (immutable)`}</code></pre>

      <h3>Action</h3>
      <p>An <code>@Action</code> is a capability — something the agent can do. Every action declares:</p>
      <table>
        <thead><tr><th>Property</th><th>Role</th></tr></thead>
        <tbody>
          <tr><td><code>preconditions</code></td><td>Facts that <strong>must be true</strong> in the current state before this action can be selected by the planner</td></tr>
          <tr><td><code>effects</code></td><td>Facts that <strong>become true</strong> in the state after the action runs</td></tr>
          <tr><td><code>cost</code></td><td>Weight used by cost-based planners to find the optimal (cheapest) plan — higher cost = less preferred</td></tr>
          <tr><td><code>utility</code></td><td>Static desirability score used by utility-based planners — higher utility = more likely to be chosen</td></tr>
          <tr><td><code>utilityMethod</code></td><td>Name of a method that computes utility dynamically from the current <code>WorldState</code>, overriding the static <code>utility</code> value</td></tr>
        </tbody>
      </table>
      <p>The planner chains actions by matching effects of one action to preconditions of the next. For example, if action A has effect <code>beansGround=true</code> and action B has precondition <code>beansGround=true</code>, then A must come before B.</p>

      <h3>Goal</h3>
      <p>A <code>@Goal</code> defines what success looks like — a condition on the world state that, when satisfied, means the agent has accomplished its objective. The planner works backward from this condition to find a sequence of actions that achieves it.</p>
      <pre><code>{`@Goal(
    name = "MakeCoffee",
    condition = @Fact(name = "coffeeServed", value = "true")
)
public void coffeeGoal() {}`}</code></pre>
      <p>A method annotated with <code>@Goal</code> is a marker only — the method body is never executed. Its <code>condition</code> is what matters to the planner.</p>

      <h3>Plan</h3>
      <p>A <code>Plan</code> is the output of planning: an ordered list of actions that, executed in sequence, transforms the initial state into a goal-satisfying state. Plans have a <code>totalCost</code> (sum of action costs) so you can compare different strategies.</p>

      <h3>Compound Task &amp; Decomposition (Structural Planner only)</h3>
      <p>A <code>@CompoundTask</code> is a high-level task that can be broken down in multiple ways via <code>@Decomposition</code> annotations. Each decomposition defines <code>subtasks</code> (referring to other actions or compound tasks by name) and optional <code>preconditions</code> to choose the right decomposition at planning time. Decompositions are tried in source-code order; if one fails due to unmet preconditions, the next is attempted (backtracking).</p>
      <pre><code>{`@CompoundTask(name = "MakeCoffee")
@Decomposition(
    name = "espresso",
    preconditions = @Fact(name = "hasEspressoMachine", value = "true"),
    subtasks = {"GrindBeans", "BrewEspresso", "Serve"}
)
@Decomposition(
    name = "drip",
    subtasks = {"BoilWater", "AddGrounds", "BrewDrip", "Serve"}
)
public void makeCoffee() {}`}</code></pre>

      <h3>Planner</h3>
      <p>A <code>Planner</code> is the algorithm that searches for a plan. Astra ships with four:</p>
      <table>
        <thead><tr><th>Planner</th><th>Strategy</th><th>Best for</th></tr></thead>
        <tbody>
          <tr><td><strong>Cost-based</strong> (<code>COST_BASED</code>)</td><td>A*-informed search; finds the cheapest plan by summing action costs</td><td>Most applications — optimal plans, explicit cost model</td></tr>
          <tr><td><strong>Utility-based</strong> (<code>UTILITY_BASED</code>)</td><td>Greedy forward chaining; picks the highest-utility applicable action at each step</td><td>Real-time / reactive agents where speed matters more than optimality</td></tr>
          <tr><td><strong>Hybrid</strong> (<code>HYBRID</code>)</td><td>Utility selection with goal-awareness and backtracking</td><td>Scenarios that need both responsiveness and goal-directed behavior</td></tr>
          <tr><td><strong>Structural</strong> (<code>STRUCTURAL</code>)</td><td>Recursive task decomposition; breaks compound tasks into subtasks</td><td>Hierarchical domains with well-defined procedures (recipes, workflows)</td></tr>
        </tbody>
      </table>

      <h3>Planning flow summary</h3>
      <pre><code>{`Initial state            Planner                   Plan
{hasBeans: true}    ──>  searches actions   ──>  [GrindBeans, Brew, Serve]
                      with preconditions/         (ordered sequence
                       effects matching            that achieves goal)
                       forward/backward

Goal condition: coffeeServed = true`}</code></pre>

      <p>At execution time, the plan actions run in order. Each action reads the current world state, executes its method body, and writes its effects back to the state. The event bus and interceptors fire at each phase for observability.</p>

      <h3>Event Bus &amp; Interceptors (cross-cutting)</h3>
      <p>The <strong>event bus</strong> publishes lifecycle events (<code>PLAN_STARTED</code>, <code>ACTION_BEFORE</code>, <code>ACTION_FAILED</code>, <code>GOAL_SATISFIED</code>, etc.) that listeners can subscribe to for logging, metrics, or debugging.</p>
      <p><strong>Interceptors</strong> hook into action execution (<code>beforeAction</code>, <code>afterAction</code>, <code>onError</code>) to add cross-cutting concerns like validation, authentication, or performance tracking — without modifying the agent code.</p>

      <h2>Writing Your First Agent</h2>
      <p>Two styles are available. Extend <code>AgentBase</code> for the quickest start (no annotations), or use the full annotation DSL for fine-grained control.</p>

      <h3>Style A: Convention-based (AgentBase)</h3>
      <p>Extend <code>AgentBase</code> — public void no-arg methods become actions automatically. No annotations needed.</p>
      <pre><code>{`import io.astra.api.AgentBase;
import io.astra.api.Astra;
import io.astra.api.WorldStates;
import io.astra.core.DefaultAstra;

public class SimpleAgent extends AgentBase {
    public void myAction() {
        System.out.println("Action executed!");
    }
}

// One-liner:
Astra astra = DefaultAstra.simple(new SimpleAgent());
astra.execute("_run_all", WorldStates.empty());`}</code></pre>
      <p>Goals are auto-derived from action effects. If no effects are declared, all actions run in sequence via the <code>_run_all</code> goal.</p>

      <h3>Style B: Annotation-based</h3>
      <p>An agent is a plain Java class annotated with <code>@Agent</code>. The class defines <strong>actions</strong> (what the agent can do), <strong>goals</strong> (what the agent wants to achieve), and optionally <strong>compound tasks</strong> (hierarchical task networks).</p>

      <h3>1. Create the agent class</h3>
      <pre><code>{`import io.astra.annotation.Agent;

@Agent
public class CoffeeAgent {
    // Actions and goals go here
}`}</code></pre>

      <h3>2. Define actions</h3>
      <p>Each action is a method annotated with <code>@Action</code>. Declare its <strong>preconditions</strong> (facts that must be true before execution) and <strong>effects</strong> (facts that become true after execution).</p>
      <pre><code>{`import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;

@Action(
    name = "GrindBeans",
    description = "Grind whole coffee beans into fine grounds",
    preconditions = @Fact(name = "hasBeans", value = "true"),
    effects = @Fact(name = "beansGround", value = "true"),
    cost = 1.0f,
    utility = 2.0f
)
public void grindBeans() {
    System.out.println("Grinding coffee beans...");
}`}</code></pre>

      <p><strong>Action fields:</strong></p>
      <table>
        <thead><tr><th>Field</th><th>Required</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>name</code></td><td>No</td><td>Method name</td><td>Unique action identifier used by planners</td></tr>
          <tr><td><code>description</code></td><td>No</td><td><code>""</code></td><td>Human-readable description</td></tr>
          <tr><td><code>preconditions</code></td><td>No</td><td><code>{}</code></td><td>Facts that must be true in the world state for this action to be applicable</td></tr>
          <tr><td><code>effects</code></td><td>No</td><td><code>{}</code></td><td>Facts that become true after execution (updated in the world state)</td></tr>
          <tr><td><code>cost</code></td><td>No</td><td><code>1.0</code></td><td>Resource cost — used by cost-based planners to find the cheapest plan</td></tr>
          <tr><td><code>utility</code></td><td>No</td><td><code>0.5</code></td><td>Static utility value — used by utility-based planners</td></tr>
          <tr><td><code>utilityMethod</code></td><td>No</td><td><code>""</code></td><td>Name of a method on the agent for dynamic utility computation</td></tr>
        </tbody>
      </table>

      <h3>3. Define goals</h3>
      <p>A goal declares a desired world state. The planner works backward (or forward, depending on the strategy) from this condition to find a sequence of actions that achieves it.</p>
      <pre><code>{`import io.astra.annotation.goal.Goal;

@Goal(
    name = "MakeCoffee",
    description = "Brew and serve a fresh cup of coffee",
    condition = @Fact(name = "coffeeServed", value = "true")
)
public void coffeeGoal() {}`}</code></pre>

      <p><strong>Goal fields:</strong></p>
      <table>
        <thead><tr><th>Field</th><th>Required</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>name</code></td><td>No</td><td>Method name</td><td>Goal identifier used when calling <code>astra.plan("GoalName", state)</code></td></tr>
          <tr><td><code>description</code></td><td>No</td><td><code>""</code></td><td>Human-readable description</td></tr>
          <tr><td><code>condition</code></td><td>No</td><td><code>{}</code></td><td>Facts that must be true for the goal to be considered satisfied</td></tr>
        </tbody>
      </table>

      <h3>4. Build and run</h3>
      <pre><code>{`import io.astra.api.*;
import io.astra.core.DefaultAstra;

public class Main {
    public static void main(String[] args) {
        Astra astra = DefaultAstra.builder()
            .register(new CoffeeAgent())
            .withPlanner(PlannerType.DEFAULT)
            .build();

        ExecutionResult result = astra.executeWithResult(
            "MakeCoffee",
            WorldStates.of("hasBeans", "true"));

        System.out.println("Success: " + result.isSuccess());
        System.out.println("Steps: " + result.getActions().size());
        for (ActionInfo action : result.getPlan().getActions()) {
            System.out.println(" - " + action.getName() + ": " + action.getDescription());
        }
        System.out.println("Final state: " + result.getFinalState());
    }
}`}</code></pre>

      <h2>Switching Planners</h2>
      <p>Change the planner with a single line in the builder:</p>
      <pre><code>{`// Cost-based planner — optimal cost-based planning (default)
.withPlanner(PlannerType.COST_BASED)

// Utility-based planner — fast greedy utility-based selection
.withPlanner(PlannerType.UTILITY_BASED)

// Hybrid planner — utility selection with goal-awareness
.withPlanner(PlannerType.HYBRID)

// Structural planner — hierarchical task decomposition
.withPlanner(PlannerType.STRUCTURAL)`}</code></pre>

      <h2>Building from Source</h2>
      <pre><code>{`# Clone the repository
git clone https://github.com/Abhiramrathod/astra.git
cd astra

# Build all modules (skip tests for speed)
./mvnw clean install -DskipTests

# Run the full test suite
./mvnw test -pl astra-tests

# Run the sample application
./mvnw exec:java -pl astra-sample \\
    -Dexec.mainClass="io.astra.sample.Main"`}</code></pre>
      <p>The Maven wrapper (<code>mvnw</code>) handles dependency resolution automatically. No global Maven installation is required.</p>

      <h2>Troubleshooting</h2>

      <h3>Build fails with missing dependencies</h3>
      <p>Ensure you run <code>./mvnw install -DskipTests</code> from the project root first. This installs all Astra modules to your local Maven repository.</p>

      <h3>SLF4J warning at runtime</h3>
      <p>Astra uses SLF4J for logging. Add a binding like <code>slf4j-simple</code> or <code>logback-classic</code> to your runtime classpath to see log output.</p>
      <pre><code>{`<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.9</version>
    <scope>runtime</scope>
</dependency>`}</code></pre>

      <h3>Agent class not found by scanner</h3>
      <p>If using annotations, ensure the class is annotated with <code>@Agent</code> and is public. The <code>AgentScanner</code> uses <code>Class.getMethods()</code> which only returns public methods. Alternatively, extend <code>AgentBase</code> instead — no <code>@Agent</code> annotation required.</p>
    </>
  )
}
