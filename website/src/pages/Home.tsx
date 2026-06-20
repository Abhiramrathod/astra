export default function Home() {
  return (
    <>
      <div className="hero">
        <h1>Astra</h1>
        <p>Multi-Planner Agent Framework for Java — zero LLM dependency</p>
        <p>
          <span className="tag">Java 17+</span>
          <span className="tag">Maven</span>
          <span className="tag">Apache 2.0</span>
          <span className="tag">A* GOAP</span>
          <span className="tag">Utility AI</span>
          <span className="tag">HTN</span>
        </p>
        <p style={{ fontSize: '1rem', marginTop: '1.5rem' }}>
          <a href="https://github.com/Abhiramrathod/astra" target="_blank">GitHub</a>
          {' · '}
          <a href="https://github.com/Abhiramrathod/astra/releases" target="_blank">Releases</a>
          {' · '}
          <a href="https://github.com/Abhiramrathod/astra/issues" target="_blank">Issues</a>
        </p>
      </div>

      <h2>What is Astra?</h2>
      <p>
        Astra is a production-grade, pure-Java agent framework designed for building autonomous
        decision-making systems. Unlike agent frameworks that depend on large language models (LLMs),
        Astra uses classical AI planning techniques — <strong>A* Goal-Oriented Action Planning</strong>,
        <strong>Utility AI</strong>, <strong>Hybrid</strong>, and <strong>Hierarchical Task Networks</strong> —
        making it deterministic, fast, and suitable for real-time applications like games, robotics,
        simulation, and automation.
      </p>
      <p>
        Agents are defined declaratively using Java annotations. The framework handles planning,
        execution, event broadcasting, interceptor chains, and lifecycle management out of the box.
      </p>

      <h2>Features</h2>
      <div className="features">
        <div className="feature-card">
          <h3>Four Planners</h3>
          <p>A* GOAP for optimal paths, Utility AI for fast greedy decisions, Hybrid for balanced trade-offs, and HTN for structured task hierarchies — all in one framework.</p>
        </div>
        <div className="feature-card">
          <h3>Annotation DSL</h3>
          <p>Define agents, actions, goals, facts, compound tasks, and decompositions entirely with Java annotations. No XML, no YAML, no external configuration files.</p>
        </div>
        <div className="feature-card">
          <h3>Dynamic Utility</h3>
          <p>State-aware utility functions via <code>utilityMethod</code>. Actions can compute their own utility based on the current world state at planning time.</p>
        </div>
        <div className="feature-card">
          <h3>SPI Plugin System</h3>
          <p>Third-party planners discovered at runtime via Java <code>ServiceLoader</code>. Implement <code>PlannerProvider</code>, drop your jar on the classpath, and it works.</p>
        </div>
        <div className="feature-card">
          <h3>Event Bus</h3>
          <p>Publish-subscribe lifecycle and planning events. Track every stage: agent registration, plan start/complete/fail, action before/after/fail, goal satisfied/unsatisfiable.</p>
        </div>
        <div className="feature-card">
          <h3>Interceptor Chain</h3>
          <p>Hook into action execution with <code>beforeAction</code>, <code>afterAction</code>, and <code>onError</code> callbacks. Ideal for logging, metrics, validation, and auditing.</p>
        </div>
        <div className="feature-card">
          <h3>Lifecycle Management</h3>
          <p>Agents implement <code>LifecycleAware</code> to receive <code>onInit</code> and <code>onDestroy</code> callbacks. The framework manages initialization and cleanup automatically.</p>
        </div>
        <div className="feature-card">
          <h3>Spring Boot</h3>
          <p>First-class Spring Boot support via <code>@EnableAstra</code>. Auto-configures the <code>Astra</code> instance and makes it available as a Spring bean.</p>
        </div>
        <div className="feature-card">
          <h3>JSON Serialization</h3>
          <p><code>WorldStateSerializer</code> converts world states to and from JSON using Jackson. Useful for persistence, networking, and debugging.</p>
        </div>
        <div className="feature-card">
          <h3>Typed Exceptions</h3>
          <p>Hierarchical exception types for every failure mode: <code>GoalNotFoundException</code>, <code>PlanNotFoundException</code>, <code>ActionExecutionException</code>, <code>DuplicateGoalException</code>, and more.</p>
        </div>
        <div className="feature-card">
          <h3>Async Execution</h3>
          <p><code>CompletableFuture</code>-based asynchronous execution via <code>executeAsync()</code>. Run planning and action execution on a separate thread without blocking.</p>
        </div>
        <div className="feature-card">
          <h3>Zero LLM Deps</h3>
          <p>No OpenAI, no Anthropic, no HuggingFace. Astra is pure classical AI — deterministic, predictable, auditable, and infinitely cheaper to run.</p>
        </div>
      </div>

      <h2>Quick Start</h2>
      <p>Add the dependency, write an agent, and run it in minutes.</p>
      <h3>1. Add dependency</h3>
      <pre><code>{`<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-core</artifactId>
    <version>0.1.0</version>
</dependency>`}</code></pre>

      <h3>2. Define an agent</h3>
      <pre><code>{`import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;

@Agent
public class CoffeeAgent {
    @Action(
        name = "GrindBeans",
        preconditions = @Fact(name = "hasBeans", value = "true"),
        effects = @Fact(name = "beansGround", value = "true")
    )
    public void grindBeans() {
        System.out.println("Grinding coffee beans...");
    }

    @Action(
        name = "BrewCoffee",
        preconditions = {
            @Fact(name = "beansGround", value = "true"),
            @Fact(name = "waterBoiled", value = "true")
        },
        effects = @Fact(name = "coffeeBrewed", value = "true")
    )
    public void brewCoffee() {
        System.out.println("Brewing coffee...");
    }

    @Action(
        name = "ServeCoffee",
        preconditions = @Fact(name = "coffeeBrewed", value = "true"),
        effects = @Fact(name = "coffeeServed", value = "true")
    )
    public void serveCoffee() {
        System.out.println("Serving coffee!");
    }

    @Goal(
        name = "MakeCoffee",
        condition = @Fact(name = "coffeeServed", value = "true")
    )
    public void coffeeGoal() {}
}`}</code></pre>

      <h3>3. Run with Astra</h3>
      <pre><code>{`import io.astra.api.*;
import io.astra.core.DefaultAstra;

public class Main {
    public static void main(String[] args) {
        Astra astra = DefaultAstra.builder()
            .register(new CoffeeAgent())
            .withPlanner(PlannerType.GOAP)
            .build();

        ExecutionResult result = astra.executeWithResult(
            "MakeCoffee",
            WorldStates.of("hasBeans", "true"));

        System.out.println("Success: " + result.isSuccess());
        System.out.println("Steps: " + result.getActions().size());
        System.out.println("Final state: " + result.getFinalState());
    }
}`}</code></pre>

      <h3>4. Output</h3>
      <pre><code>{`Goal 'MakeCoffee' achieved in 3ms.
Final state: {hasBeans=true, waterBoiled=true,
    coffeeServed=true, coffeeBrewed=true, beansGround=true}
Success: true, Steps: 3`}</code></pre>

      <h2>Modules</h2>
      <table>
        <thead><tr><th>Module</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra-annotations</code></td><td>Annotation definitions: @Agent, @Action, @Goal, @Fact, @CompoundTask, @Decomposition</td></tr>
          <tr><td><code>astra-api</code></td><td>Core interfaces: Astra, GoapPlanner, ActionInfo, WorldState, EventBus, PlannerProvider, PlannerType</td></tr>
          <tr><td><code>astra-utils</code></td><td>ClassPathScanner, WorldStateSerializer (Jackson)</td></tr>
          <tr><td><code>astra-scanner</code></td><td>Reflects annotations into API type instances at registration time</td></tr>
          <tr><td><code>astra-config</code></td><td>Map-based and properties-file configuration providers</td></tr>
          <tr><td><code>astra-exceptions</code></td><td>Typed exception hierarchy for action, agent, goal, and state errors</td></tr>
          <tr><td><code>astra-events</code></td><td>DefaultEventBus — thread-safe publish-subscribe event bus</td></tr>
          <tr><td><code>astra-interceptors</code></td><td>DefaultInterceptorChain — before/after/error action hooks</td></tr>
          <tr><td><code>astra-lifecycle</code></td><td>LifecycleManager — agent initialization and shutdown</td></tr>
          <tr><td><code>astra-planners</code></td><td>All four planner implementations (GOAP, Utility, Hybrid, HTN) + SPI providers</td></tr>
          <tr><td><code>astra-core</code></td><td>DefaultAstra — the main builder, runtime, and wiring</td></tr>
          <tr><td><code>astra-spring</code></td><td>Spring Boot auto-configuration with @EnableAstra</td></tr>
          <tr><td><code>astra-sample</code></td><td>Demo agents: CoffeeAgent (GOAP/Utility/Hybrid) and CookingAgent (HTN)</td></tr>
          <tr><td><code>astra-tests</code></td><td>JUnit 5 test suite — 12 tests covering all planners</td></tr>
        </tbody>
      </table>

      <h2>License</h2>
      <p>Astra is open source under the <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License 2.0</a>.</p>
    </>
  )
}
