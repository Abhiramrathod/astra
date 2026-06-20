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
        <p>This single dependency pulls in everything you need: <code>astra-api</code> (interfaces), <code>astra-planners</code> (all planner implementations), <code>astra-scanner</code> (annotation processing), <code>astra-utils</code>, <code>astra-config</code>, <code>astra-exceptions</code>, <code>astra-events</code>, <code>astra-interceptors</code>, and <code>astra-lifecycle</code>.</p>

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

      <h2>Writing Your First Agent</h2>
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
      <p>Ensure the class is annotated with <code>@Agent</code> and is public. The <code>AgentScanner</code> uses <code>Class.getMethods()</code> which only returns public methods.</p>
    </>
  )
}
