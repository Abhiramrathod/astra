export default function FAQ() {
  return (
    <>
      <h1>FAQ</h1>

      <h3>What makes Astra different from other agent frameworks?</h3>
      <p>
        Astra uses <strong>classical AI planning</strong> (cost-based, utility-based,
        structural approaches) rather than large language models. This makes it deterministic, fast, auditable, and
        significantly cheaper to run. It's designed for real-time systems like games,
        robotics, and automation where decisions must be made in milliseconds.
      </p>

      <h3>Does Astra require a GPU or cloud API?</h3>
      <p>
        No. Astra is pure Java with zero external AI dependencies. No GPU, no OpenAI,
        no Anthropic, no HuggingFace endpoints. It runs entirely in-process.
      </p>

      <h3>Which planner should I use?</h3>
      <p>
        It depends on your use case:
      </p>
      <ul>
        <li><strong>Cost-based</strong> — you need the <em>optimal</em> (cheapest) plan and can wait for the search to complete</li>
        <li><strong>Utility-based</strong> — you need <em>fast</em> decisions and can accept near-optimal results</li>
        <li><strong>Hybrid</strong> — you want fast decisions <em>and</em> goal-awareness</li>
        <li><strong>Structural</strong> — your domain has a <em>natural task hierarchy</em> with known decomposition patterns</li>
      </ul>

      <h3>Can I use multiple planners in one application?</h3>
      <p>
        Yes. Create separate <code>Astra</code> instances for different planning needs.
        Each instance has its own planner and action set.
      </p>
      <pre><code>{`Astra costAstra = DefaultAstra.builder()
    .register(new CombatAgent())
    .withPlanner(PlannerType.COST_BASED)
    .build();

Astra structAstra = DefaultAstra.builder()
    .register(new CraftingAgent())
    .withPlanner(PlannerType.STRUCTURAL)
    .build();`}</code></pre>

      <h3>How do I add a custom planner?</h3>
      <p>
        Implement <code>Planner</code> and <code>PlannerProvider</code>, register the
        provider via <code>META-INF/services/io.astra.api.PlannerProvider</code>, and
        put your jar on the classpath. See the <a href="#/guides">Guides → Custom Planners</a>
        section for a step-by-step walkthrough.
      </p>

      <h3>Is Astra thread-safe?</h3>
      <p>
        Yes. The builder uses <code>ConcurrentHashMap</code> and <code>CopyOnWriteArrayList</code>
        internally. The event bus supports concurrent publishing and dynamic subscription.
        Async execution via <code>CompletableFuture</code> is supported natively.
      </p>

      <h3>Does Astra work with Spring Boot?</h3>
      <p>
        Yes. Add the <code>astra-spring</code> dependency and annotate your application
        with <code>@EnableAstra</code>. An <code>Astra</code> bean is automatically
        configured in the application context.
      </p>

      <h3>How do I configure planner parameters?</h3>
      <p>
        Use <code>Builder.withConfig("key", "value")</code> for individual settings,
        or <code>Builder.withConfig(Map)</code> / <code>Builder.withConfig(AstraConfig)</code>
        for bulk configuration. See <a href="#/architecture">Architecture → Configuration</a>
        for all available properties.
      </p>

      <h3>Can I observe what the planner is doing?</h3>
      <p>
        Yes. Subscribe to events via <code>Builder.withEventListener(listener)</code>
        or add an <code>ActionInterceptor</code> via <code>Builder.withInterceptor(interceptor)</code>.
        You can monitor every step of planning and execution.
      </p>

      <h3>How do I serialize world state for persistence or networking?</h3>
      <p>
        Add Jackson to your classpath and use <code>WorldStateSerializer.toJson(state)</code>
        and <code>WorldStateSerializer.fromJson(json)</code>.
      </p>

      <h3>Why is the planner not finding a plan?</h3>
      <p>
        Common causes:
      </p>
      <ul>
        <li><strong>Missing precondition chain:</strong> Ensure preconditions form a connected graph from the initial state to the goal</li>
        <li><strong>No applicable actions:</strong> The initial state may not satisfy any action's preconditions</li>
        <li><strong>Iteration limit:</strong> The search space may be too large; increase <code>astra.planner.maxIterations</code></li>
        <li><strong>Unreachable goal:</strong> No sequence of actions can produce the goal condition from the initial state</li>
      </ul>

      <h3>What Java version is required?</h3>
      <p>Java 17 or later. Astra uses records, sealed classes, pattern matching, and text blocks.</p>

      <h3>What build tools are supported?</h3>
      <p>
        Maven is the primary build system, with a bundled Maven wrapper (<code>mvnw</code>)
        for zero-install builds. Gradle is supported via the standard Maven Central coordinates.
      </p>

      <h3>Is Astra production-ready?</h3>
      <p>
        Astra is in active development (v0.1.0). The core architecture and all planners
        are functional and tested. APIs may evolve as the framework matures.
      </p>

      <h3>How can I contribute?</h3>
      <p>
        Open issues, submit pull requests, or start discussions on the
        <a href="https://github.com/Abhiramrathod/astra" target="_blank">GitHub repository</a>.
        All contributions are welcome.
      </p>

      <h3>What license does Astra use?</h3>
      <p>
        Apache License 2.0. See the
        <a href="https://github.com/Abhiramrathod/astra/blob/main/LICENSE" target="_blank">LICENSE</a>
        file for details.
      </p>
    </>
  )
}
