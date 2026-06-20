export default function Samples() {
  return (
    <>
      <h1>Sample Agents</h1>
      <p>
        The <code>astra-sample</code> module contains two demo agents that showcase
        different aspects of the framework.
      </p>

      <h2>CoffeeAgent</h2>
      <p>
        <strong>File:</strong> <code>astra-sample/src/main/java/io/astra/sample/agent/CoffeeAgent.java</code>
      </p>
      <p>
        A coffee-making agent demonstrating cost-based, utility-based, and hybrid planning.
        The agent starts with coffee beans and must go through four steps to serve coffee.
      </p>

      <h3>Actions</h3>
      <table>
        <thead><tr><th>Action</th><th>Preconditions</th><th>Effects</th><th>Cost</th><th>Utility</th></tr></thead>
        <tbody>
          <tr><td>GrindBeans</td><td><code>hasBeans=true</code></td><td><code>beansGround=true</code></td><td>1</td><td>2</td></tr>
          <tr><td>BoilWater</td><td><code>beansGround=true</code></td><td><code>waterBoiled=true</code></td><td>1</td><td>3</td></tr>
          <tr><td>BrewCoffee</td><td><code>beansGround=true</code>, <code>waterBoiled=true</code></td><td><code>coffeeBrewed=true</code></td><td>1</td><td>4 (dynamic)</td></tr>
          <tr><td>ServeCoffee</td><td><code>coffeeBrewed=true</code></td><td><code>coffeeServed=true</code></td><td>1</td><td>5</td></tr>
        </tbody>
      </table>

      <h3>Dynamic utility demo</h3>
      <p>
        The <code>BrewCoffee</code> action uses <code>utilityMethod = "getBrewUtility"</code>
        to compute its utility dynamically from the world state:
      </p>
      <pre><code>{`public double getBrewUtility(WorldState state) {
    boolean hasBeans = state.get("hasBeans")
        .map("true"::equals).orElse(false);
    boolean waterReady = state.get("waterBoiled")
        .map("true"::equals).orElse(false);
    double u;
    if (hasBeans && waterReady) u = 10.0;
    else if (hasBeans) u = 6.0;
    else u = 3.0;
    log.debug("Dynamic utility for BrewCoffee: u={}", u);
    return u;
}`}</code></pre>

      <h3>Goal</h3>
      <table>
        <thead><tr><th>Goal</th><th>Condition</th></tr></thead>
        <tbody>
          <tr><td>MakeCoffee</td><td><code>coffeeServed=true</code></td></tr>
        </tbody>
      </table>

      <h3>Planner behavior</h3>
      <ul>
        <li><strong>Cost-based:</strong> Finds the optimal path (all 4 actions, cost=4). Ignores utility values.</li>
        <li><strong>Utility-based:</strong> Picks actions greedily by utility (dynamic utility considered for BrewCoffee). Executes all actions regardless of goal.</li>
        <li><strong>Hybrid:</strong> Same greedy selection but stops early if goal is met.</li>
      </ul>

      <hr />

      <h2>CookingAgent</h2>
      <p>
        <strong>File:</strong> <code>astra-sample/src/main/java/io/astra/sample/agent/CookingAgent.java</code>
      </p>
      <p>
        A dinner-making agent demonstrating structural decomposition with compound tasks and
        conditional branching.
      </p>

      <h3>Primitive actions</h3>
      <table>
        <thead><tr><th>Action</th><th>Preconditions</th><th>Effects</th><th>Cost</th></tr></thead>
        <tbody>
          <tr><td>ChopVegetables</td><td><code>hasKnife=true</code>, <code>hasIngredients=true</code></td><td><code>veggiesChopped=true</code></td><td>1</td></tr>
          <tr><td>BoilWater</td><td><code>hasPot=true</code>, <code>veggiesChopped=true</code></td><td><code>waterBoiled=true</code></td><td>1</td></tr>
          <tr><td>CookPasta</td><td><code>waterBoiled=true</code></td><td><code>pastaCooked=true</code></td><td>1</td></tr>
          <tr><td>ServeMeal</td><td><code>pastaCooked=true</code></td><td><code>mealServed=true</code></td><td>1</td></tr>
          <tr><td>SetTable</td><td><code>pastaCooked=true</code></td><td><code>tableSet=true</code></td><td>1</td></tr>
        </tbody>
      </table>

      <h3>Compound task</h3>
      <pre><code>{`@CompoundTask(name = "MakeDinner",
    description = "Prepare a full dinner meal")
@Decomposition(name = "pasta",
    subtasks = {"ChopVegetables", "BoilWater",
                "CookPasta", "ServeMeal"})
@Decomposition(name = "quick",
    preconditions = @Fact(name = "preCooked", value = "true"),
    subtasks = {"BoilWater", "CookPasta", "ServeMeal"})
public void makeDinner() {}`}</code></pre>

      <h3>Decomposition behavior</h3>
      <ul>
        <li><strong>"pasta" path (default):</strong> When no preconditions are set, this path is the fallback. Requires all 4 steps: chop, boil, cook, serve.</li>
        <li><strong>"quick" path:</strong> When <code>preCooked=true</code>, the agent skips chopping and goes directly to boiling water, cooking pasta, and serving.</li>
      </ul>

      <h3>Goal</h3>
      <p>
        Unlike goal-driven agents, structural decomposition agents don't use <code>@Goal</code> annotations.
        The planner reads the root compound task name from the goal name passed to
        <code>astra.executeWithResult("MakeDinner", ...)</code>.
      </p>

      <h3>Running the decomposition demo</h3>
      <pre><code>{`Astra astra = DefaultAstra.builder()
    .register(new CookingAgent())
    .withPlanner(PlannerType.STRUCTURAL)
    .build();

// Uses the "pasta" decomposition (all 4 steps)
astra.executeWithResult("MakeDinner",
    WorldStates.of("hasIngredients", "true",
                   "hasKnife", "true", "hasPot", "true"));

// Uses the "quick" decomposition (3 steps, skips chopping)
astra.executeWithResult("MakeDinner",
    WorldStates.of("hasIngredients", "true",
                   "hasKnife", "true", "hasPot", "true",
                   "preCooked", "true"));`}</code></pre>

      <h2>Writing your own demos</h2>
      <p>
        To create a custom demo agent, follow the same pattern:
      </p>
      <ol>
        <li>Create a class annotated with <code>@Agent</code></li>
        <li>Add methods annotated with <code>@Action</code> for each primitive behavior</li>
        <li>Add a method annotated with <code>@Goal</code> to define what success looks like</li>
        <li>For decomposition demos, add methods annotated with <code>@CompoundTask</code> and <code>@Decomposition</code></li>
        <li>Build and run with <code>DefaultAstra.builder().register(new YourAgent()).withPlanner(...)</code></li>
      </ol>
    </>
  )
}
