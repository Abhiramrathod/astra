import { GithubIcon, RocketIcon, BookOpenIcon } from '../components/Icons'

export default function Home() {
  return (
    <>
      <div className="hero">
        <h1 className="hero-emblem">Astra</h1>
        <p className="subtitle">
          Multi-Planner Agent Framework for Java — classical AI planning
          with zero LLM dependency. Deterministic, fast, and auditable
          decision-making for real-time systems.
        </p>
        <div className="tags">
          <span className="tag">Java 17+</span>
          <span className="tag">Maven</span>
          <span className="tag">Apache 2.0</span>
          <span className="tag">Multi-Planner</span>
          <span className="tag">Annotation DSL</span>
          <span className="tag">Deterministic</span>
          <span className="tag">SPI Plugins</span>
        </div>
        <div className="links">
          <a href="https://github.com/Abhiramrathod/astra" target="_blank" rel="noopener">
            <GithubIcon />
            GitHub
          </a>
          <a href="#/getting-started" className="primary-link">
            <RocketIcon />
            Get Started
          </a>
          <a href="#/api">
            <BookOpenIcon />
            API Reference
          </a>
        </div>
      </div>

      <div className="gothic-divider">
        <span className="line" />
        <span className="ornament">&#9670;</span>
        <span className="line" />
      </div>

      <h2>What is Astra?</h2>
      <p>
        Astra is a production-grade, pure-Java agent framework for building autonomous
        decision-making systems. Unlike frameworks that depend on large language models,
        Astra uses <strong>classical AI planning</strong> — multiple planning strategies
        including cost-based, utility-based, structural, and hybrid approaches — making it deterministic, fast,
        and suitable for real-time applications like games, robotics, simulation, and automation.
      </p>
      <p>
        Agents are defined declaratively using Java annotations. The framework handles planning,
        execution, event broadcasting, interceptor chains, and lifecycle management out of the box.
      </p>

      <div className="gothic-divider">
        <span className="line" />
        <span className="ornament">&#9670;</span>
        <span className="line" />
      </div>

      <h2>Features</h2>
      <div className="features">
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <h3>Multiple Planners</h3>
          <p>Cost-based, utility-based, structural, and hybrid — all in one framework. Pick the right planner for your use case.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><line x1="16" x2="8" y1="13" y2="13"/><line x1="16" x2="8" y1="17" y2="17"/></svg>
          </div>
          <h3>Annotation DSL</h3>
          <p>Define agents, actions, goals, and tasks entirely with Java annotations. No XML, no YAML, no config files.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" x2="12" y1="2" y2="22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          </div>
          <h3>Dynamic Utility</h3>
          <p>State-aware utility via <code>utilityMethod</code>. Actions compute their own utility from the world state at planning time.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>
          </div>
          <h3>SPI Plugin System</h3>
          <p>Third-party planners discovered at runtime via <code>ServiceLoader</code>. Drop your jar and it works.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H19a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1H6.5a1 1 0 0 1 0-5H20"/><path d="M8 11h8"/><path d="M8 7h6"/></svg>
          </div>
          <h3>Event Bus</h3>
          <p>Publish-subscribe lifecycle and planning events. Track every stage from agent registration to goal satisfaction.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9"/><path d="M16.376 3.622a1 1 0 0 1 3.002 3.002L7.368 18.635a2 2 0 0 1-.855.506l-2.872.838a.5.5 0 0 1-.62-.62l.838-2.872a2 2 0 0 1 .506-.854z"/></svg>
          </div>
          <h3>Interceptor Chain</h3>
          <p>Hook into action execution with before/after/error callbacks for logging, metrics, validation, and auditing.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M6 20h12"/><path d="M6 4h12"/><path d="M6 12h12"/><path d="M6 8h12"/><path d="M6 16h12"/></svg>
          </div>
          <h3>Lifecycle Management</h3>
          <p>Agents receive <code>onInit</code> and <code>onDestroy</code> callbacks. The framework handles initialization and cleanup.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
          </div>
          <h3>Spring Boot</h3>
          <p>First-class Spring Boot support via <code>@EnableAstra</code>. Auto-configures Astra as a Spring bean.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/><path d="M18 12a2 2 0 0 0 0 4h4v-4Z"/></svg>
          </div>
          <h3>JSON Serialization</h3>
          <p>World states to/from JSON via Jackson for persistence, networking, and debugging.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" x2="12" y1="9" y2="13"/><line x1="12" x2="12.01" y1="17" y2="17"/></svg>
          </div>
          <h3>Typed Exceptions</h3>
          <p>Exception hierarchy for every failure mode: goal not found, plan not found, action execution error, and more.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          </div>
          <h3>Async Execution</h3>
          <p><code>CompletableFuture</code>-based async via <code>executeAsync()</code>. Run planning and execution on a separate thread.</p>
        </div>
        <div className="feature-card">
          <div className="icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/></svg>
          </div>
          <h3>Zero LLM Deps</h3>
          <p>No OpenAI, no Anthropic, no HuggingFace. Pure classical AI — deterministic, predictable, and infinitely cheaper.</p>
        </div>
      </div>

      <div className="gothic-divider">
        <span className="line" />
        <span className="ornament">&#9670;</span>
        <span className="line" />
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
            .withPlanner(PlannerType.DEFAULT)
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

      <div className="gothic-divider">
        <span className="line" />
        <span className="ornament">&#9670;</span>
        <span className="line" />
      </div>

      <h2>Modules</h2>
      <table>
        <thead><tr><th>Module</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra-annotations</code></td><td>Annotation definitions: @Agent, @Action, @Goal, @Fact, @CompoundTask, @Decomposition</td></tr>
          <tr><td><code>astra-api</code></td><td>Core interfaces: Astra, Planner, ActionInfo, WorldState, EventBus, PlannerProvider, PlannerType</td></tr>
          <tr><td><code>astra-utils</code></td><td>ClassPathScanner, WorldStateSerializer (Jackson)</td></tr>
          <tr><td><code>astra-scanner</code></td><td>Reflects annotations into API type instances at registration time</td></tr>
          <tr><td><code>astra-config</code></td><td>Map-based and properties-file configuration providers</td></tr>
          <tr><td><code>astra-exceptions</code></td><td>Typed exception hierarchy for action, agent, goal, and state errors</td></tr>
          <tr><td><code>astra-events</code></td><td>DefaultEventBus — thread-safe publish-subscribe event bus</td></tr>
          <tr><td><code>astra-interceptors</code></td><td>DefaultInterceptorChain — before/after/error action hooks</td></tr>
          <tr><td><code>astra-lifecycle</code></td><td>LifecycleManager — agent initialization and shutdown</td></tr>
          <tr><td><code>astra-planners</code></td><td>All planner implementations + SPI providers for extensibility</td></tr>
          <tr><td><code>astra-core</code></td><td>DefaultAstra — the main builder, runtime, and wiring</td></tr>
          <tr><td><code>astra-spring</code></td><td>Spring Boot auto-configuration with @EnableAstra</td></tr>
          <tr><td><code>astra-sample</code></td><td>Demo agents — CoffeeAgent and CookingAgent showcasing various planning strategies</td></tr>
          <tr><td><code>astra-tests</code></td><td>JUnit 5 test suite — 12 tests covering all planners</td></tr>
        </tbody>
      </table>

      <div className="gothic-divider">
        <span className="line" />
        <span className="ornament">&#9670;</span>
        <span className="line" />
      </div>

      <h2>License</h2>
      <p>Astra is open source under the <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank" rel="noopener">Apache License 2.0</a>.</p>
    </>
  )
}
