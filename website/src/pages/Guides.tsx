export default function Guides() {
  return (
    <>
      <h1>Guides</h1>

      <h2 id="dynamic-utility">Dynamic Utility</h2>
      <p>
        Actions can compute their utility dynamically based on the current world state,
        rather than using a fixed value. This is useful when an action's desirability
        depends on context.
      </p>

      <h3>Declaring a dynamic utility method</h3>
      <p>Use the <code>utilityMethod</code> field on <code>@Action</code> to name a method on your agent class:</p>
      <pre><code>{`@Action(
    name = "Repair",
    utility = 5,
    utilityMethod = "repairUtility"
)
public void repair() {
    System.out.println("Repairing equipment...");
}

public double repairUtility(WorldState state) {
    int damageLevel = Integer.parseInt(
        state.get("damage").orElse("0"));
    // Higher damage = higher priority to repair
    return damageLevel * 2.0;
}`}</code></pre>

      <h3>Requirements</h3>
      <ul>
        <li>The method must be <code>public</code></li>
        <li>It must accept a single <code>WorldState</code> parameter</li>
        <li>It must return a numeric type (<code>int</code>, <code>long</code>, <code>float</code>, <code>double</code>)</li>
      </ul>

      <h3>How it works</h3>
      <p>
        At planning time, the <code>AgentScanner</code> looks up the named method via reflection
        and stores it in the <code>ActionInfo</code>. When utility-based planners (Utility AI,
        Hybrid) call <code>action.computeUtility(state)</code>, the method is invoked with the
        current world state. The return value overrides the static <code>utility</code> value.
      </p>

      <h3>Performance note</h3>
      <p>
        The method is looked up once at registration time and cached. The per-planning invocation
        cost is a single method call — negligible in practice.
      </p>

      <hr />

      <h2 id="htn">HTN Decompositions</h2>
      <p>
        Hierarchical Task Networks decompose compound tasks into smaller subtasks recursively.
        This guide covers advanced HTN usage patterns.
      </p>

      <h3>Conditional branching</h3>
      <p>
        Multiple <code>@Decomposition</code> annotations on the same compound task define
        alternative ways to accomplish it. Decompositions are tried in source-code order;
        the first whose preconditions match is used.
      </p>
      <pre><code>{`@CompoundTask(name = "HandleOrder")
@Decomposition(name = "express",
    preconditions = @Fact(name = "priority", value = "high"),
    subtasks = {"ExpediteShipping", "NotifyCustomer"})
@Decomposition(name = "standard",
    subtasks = {"PackOrder", "PrintLabel", "ShipOrder"})
public void handleOrder() {}`}</code></pre>
      <p>
        Here, if <code>priority=high</code> in the world state, the "express" path is taken.
        Otherwise, the "standard" three-step path is used.
      </p>

      <h3>Nested compound tasks</h3>
      <p>Compound tasks can reference other compound tasks as subtasks, creating nested hierarchies:</p>
      <pre><code>{`@CompoundTask(name = "CookDinner")
@Decomposition(subtasks = {"PrepareIngredients", "CookMeal", "CleanUp"})
public void cookDinner() {}

@CompoundTask(name = "PrepareIngredients")
@Decomposition(subtasks = {"Chop", "Measure", "Marinate"})
public void prepareIngredients() {}`}</code></pre>

      <h3>Backtracking</h3>
      <p>
        If a decomposition method's subtask fails (because its preconditions aren't met),
        the planner backtracks and tries the next decomposition method. This provides
        graceful fallback behavior.
      </p>

      <hr />

      <h2 id="spi">Custom Planners (SPI)</h2>
      <p>
        Add a custom planner without modifying Astra's source code by implementing the SPI.
      </p>

      <h3>Step 1: Implement the planner</h3>
      <pre><code>{`package com.myapp.planner;

import io.astra.api.*;

public class MyCustomPlanner implements GoapPlanner {
    @Override
    public Plan plan(WorldState currentState,
                     GoalInfo goal,
                     List<ActionInfo> actions) {
        // Your planning logic here
        return new DefaultPlan(actions, actions.stream()
            .mapToDouble(ActionInfo::getCost).sum());
    }
}`}</code></pre>

      <h3>Step 2: Implement the provider</h3>
      <pre><code>{`package com.myapp.planner;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import java.util.List;
import java.util.Map;

public class MyPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() {
        // Extend PlannerType isn't possible — use an existing type
        // or create your own mechanism
        return PlannerType.GOAP;
    }

    @Override
    public GoapPlanner create(AstraConfig config,
                              Map<String, CompoundTaskDef> compoundTasks,
                              List<ActionInfo> actions) {
        return new MyCustomPlanner();
    }
}`}</code></pre>

      <h3>Step 3: Register via ServiceLoader</h3>
      <p>Create a file at <code>META-INF/services/io.astra.api.PlannerProvider</code> containing the fully qualified class name:</p>
      <pre><code>{`com.myapp.planner.MyPlannerProvider`}</code></pre>

      <h3>Step 4: Add to classpath</h3>
      <p>When your jar is on the classpath, <code>DefaultAstra</code> discovers it automatically.</p>

      <blockquote>
        <strong>Note:</strong> Your custom planner replaces the built-in planner for the matching
        <code>PlannerType</code>. For a new planner type that doesn't conflict, consider extending
        the <code>PlannerType</code> enum pattern or using the planner's constructor directly.
      </blockquote>

      <hr />

      <h2 id="spring">Spring Boot Integration</h2>
      <p>
        Astra provides first-class Spring Boot support via the <code>astra-spring</code> module.
      </p>

      <h3>1. Add the dependency</h3>
      <pre><code>{`<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-spring</artifactId>
    <version>0.1.0</version>
</dependency>`}</code></pre>

      <h3>2. Enable Astra</h3>
      <pre><code>{`import io.astra.spring.EnableAstra;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAstra
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}`}</code></pre>

      <h3>3. Inject the Astra bean</h3>
      <pre><code>{`import io.astra.api.*;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final Astra astra;

    public OrderService(Astra astra) {
        this.astra = astra;
    }

    public ExecutionResult processOrder(String orderId) {
        return astra.executeWithResult("ProcessOrder",
            WorldStates.of("orderId", orderId, "inventoryOk", "true"));
    }
}`}</code></pre>

      <h3>4. Configuration</h3>
      <p>Spring Boot application properties:</p>
      <pre><code>{`# application.properties
astra.planner.maxIterations=5000
astra.planner.heuristic=unmatched`}</code></pre>

      <hr />

      <h2 id="json">JSON Serialization</h2>
      <p>
        Astra provides <code>WorldStateSerializer</code> for converting world states to and from
        JSON, using Jackson.
      </p>

      <h3>1. Add Jackson</h3>
      <pre><code>{`<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.0</version>
</dependency>`}</code></pre>

      <h3>2. Serialize</h3>
      <pre><code>{`import io.astra.utils.WorldStateSerializer;

WorldState state = WorldStates.of("hasBeans", "true", "waterBoiled", "true");
String json = WorldStateSerializer.toJson(state);
// {"hasBeans":"true","waterBoiled":"true"}

WorldState restored = WorldStateSerializer.fromJson(json);
restored.get("hasBeans"); // Optional["true"]`}</code></pre>

      <hr />

      <h2 id="config">Configuration Reference</h2>

      <table>
        <thead><tr><th>Property</th><th>Type</th><th>Default</th><th>Applies to</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra.planner.maxIterations</code></td><td><code>Integer</code></td><td><code>10000</code> (GOAP)<br /><code>20</code> (Utility)<br /><code>100</code> (Hybrid)<br /><code>200</code> (HTN)</td><td>All</td><td>Maximum search/planning iterations</td></tr>
          <tr><td><code>astra.planner.heuristic</code></td><td><code>String</code></td><td><code>unmatched</code></td><td>GOAP</td><td>Heuristic mode: <code>unmatched</code> or <code>zero</code></td></tr>
        </tbody>
      </table>

      <p>Set configuration via builder:</p>
      <pre><code>{`DefaultAstra.builder()
    .withConfig("astra.planner.maxIterations", "5000")
    .withConfig("astra.planner.heuristic", "unmatched")
    .build();`}</code></pre>

      <p>Or load from a properties file:</p>
      <pre><code>{`// astra.properties
astra.planner.maxIterations=5000
astra.planner.heuristic=unmatched`}</code></pre>
      <pre><code>{`import io.astra.config.PropertiesFileConfigProvider;
import io.astra.core.DefaultAstra;

AstraConfig fileConfig =
    new PropertiesFileConfigProvider("astra.properties");

DefaultAstra.builder()
    .withConfig(fileConfig)
    .build();`}</code></pre>

      <hr />

      <h2 id="events">Events &amp; Interceptors</h2>

      <h3>Listening to events</h3>
      <pre><code>{`import io.astra.api.event.*;

Astra astra = DefaultAstra.builder()
    .register(new CoffeeAgent())
    .withEventListener(event -> {
        System.out.println("Event: " + event.getType());
        event.getData().forEach((k, v) ->
            System.out.println("  " + k + " = " + v));
    })
    .build();`}</code></pre>

      <h3>Writing an interceptor</h3>
      <pre><code>{`import io.astra.api.interceptor.ActionInterceptor;
import io.astra.api.*;

public class LoggingInterceptor implements ActionInterceptor {
    @Override
    public void beforeAction(ActionInfo action, WorldState state) {
        System.out.println("About to execute: " + action.getName());
    }

    @Override
    public void afterAction(ActionInfo action,
                            WorldState before, WorldState after) {
        System.out.println("Completed: " + action.getName());
    }

    @Override
    public void onError(ActionInfo action,
                        WorldState state, Exception error) {
        System.err.println("Failed: " + action.getName()
            + " - " + error.getMessage());
    }
}`}</code></pre>

      <p>Register the interceptor:</p>
      <pre><code>{`DefaultAstra.builder()
    .register(new CoffeeAgent())
    .withInterceptor(new LoggingInterceptor())
    .build();`}</code></pre>

      <hr />

      <h2 id="lifecycle">Lifecycle Management</h2>
      <p>
        Agents can implement the <code>LifecycleAware</code> interface to receive
        initialization and shutdown callbacks.
      </p>
      <pre><code>{`import io.astra.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent
public class MyAgent implements LifecycleAware {
    private static final Logger log =
        LoggerFactory.getLogger(MyAgent.class);

    @Override
    public void onInit() {
        log.info("MyAgent initializing — allocating resources");
    }

    @Override
    public void onDestroy() {
        log.info("MyAgent shutting down — cleaning up");
    }
}`}</code></pre>
      <p>
        The <code>LifecycleManager</code> calls <code>onInit()</code> when the agent
        is registered via the builder, and <code>onDestroy()</code> when the
        <code>Astra</code> instance is garbage collected or explicitly shut down.
      </p>
    </>
  )
}
