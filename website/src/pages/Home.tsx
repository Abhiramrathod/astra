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
        </p>
      </div>

      <div className="features">
        <div className="feature-card">
          <h3>A* GOAP</h3>
          <p>Optimal planning via A* search over action space. Finds the lowest-cost sequence to reach any goal.</p>
        </div>
        <div className="feature-card">
          <h3>Utility AI</h3>
          <p>Greedy utility-based action selection. Fast, real-time decisions with dynamic utility functions.</p>
        </div>
        <div className="feature-card">
          <h3>Hybrid</h3>
          <p>Combines utility selection with goal-awareness. Efficient yet directed toward a goal state.</p>
        </div>
        <div className="feature-card">
          <h3>HTN</h3>
          <p>Hierarchical Task Networks. Recursive decomposition of compound tasks with precondition-based branching.</p>
        </div>
        <div className="feature-card">
          <h3>Annotation DSL</h3>
          <p>Define agents, actions, goals, and decompositions with plain Java annotations.</p>
        </div>
        <div className="feature-card">
          <h3>SPI Plugin System</h3>
          <p>Third-party planners discovered at runtime via Java ServiceLoader. No core changes needed.</p>
        </div>
      </div>

      <h2>Quick Start</h2>
      <p>Add the dependency and write your first agent in minutes:</p>
      <pre><code>{`<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-core</artifactId>
    <version>0.1.0</version>
</dependency>`}</code></pre>

      <pre><code>{`@Agent
public class CoffeeAgent {
    @Action(
        name = "GrindBeans",
        preconditions = @Fact(name = "hasBeans", value = "true"),
        effects = @Fact(name = "beansGround", value = "true")
    )
    public void grindBeans() { }

    @Goal(name = "MakeCoffee",
        condition = @Fact(name = "coffeeServed", value = "true"))
    public void coffeeGoal() { }
}`}</code></pre>

      <pre><code>{`Astra astra = DefaultAstra.builder()
    .register(new CoffeeAgent())
    .withPlanner(PlannerType.GOAP)
    .build();

ExecutionResult r = astra.executeWithResult(
    "MakeCoffee", WorldStates.of("hasBeans", "true"));`}</code></pre>
    </>
  )
}
