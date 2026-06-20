export default function Planners() {
  return (
    <>
      <h1>Planners</h1>

      <h2>A* GOAP</h2>
      <p><strong>Goal-Oriented Action Planning</strong> using A* search. The planner explores the action space from the initial world state, expanding nodes in order of <code>costSoFar + heuristic(state, goal)</code>.</p>
      <ul>
        <li><strong>Heuristic</strong>: Count of unmatched goal conditions (admissible for single-effect actions)</li>
        <li><strong>Optimal</strong>: Always finds the lowest-cost plan when one exists</li>
        <li><strong>Max iterations</strong>: Configurable via <code>astra.planner.maxIterations</code> (default 10,000)</li>
        <li><strong>Best for</strong>: Deterministic worlds where optimality matters</li>
      </ul>
      <pre><code>{`// GOAP picks the cheapest plan
Astra astra = DefaultAstra.builder()
    .register(new Agent())
    .withPlanner(PlannerType.GOAP)
    .build();`}</code></pre>

      <h2>Utility AI</h2>
      <p>Greedy utility-based action selection. At each step the planner picks the applicable action with the highest utility.</p>
      <ul>
        <li><strong>Formula</strong>: <code>effectiveUtility = action.getCost() &gt; 0 ? utility / cost : utility</code></li>
        <li><strong>Dynamic utility</strong>: Actions can provide a <code>utilityMethod</code> annotated method that receives the current <code>WorldState</code> and returns a numeric utility value</li>
        <li><strong>No backtracking</strong>: Greedy — doesn't revisit past decisions</li>
        <li><strong>Best for</strong>: Real-time decisions where speed is critical</li>
      </ul>
      <pre><code>{`@Action(
    name = "BrewCoffee",
    utility = 4,
    utilityMethod = "getBrewUtility"
)
public void brewCoffee() { }

public double getBrewUtility(WorldState state) {
    if (state.get("waterBoiled").map("true"::equals).orElse(false))
        return 10.0;
    return 6.0;
}`}</code></pre>

      <h2>Hybrid (Utility + GOAP)</h2>
      <p>Combines the speed of utility selection with goal-awareness. Like Utility AI it picks actions greedily by utility, but after each step it checks whether the goal condition is satisfied.</p>
      <ul>
        <li><strong>Stops early</strong>: As soon as the goal is satisfied, planning ends</li>
        <li><strong>Utility-guided</strong>: Uses the same utility function as Utility AI</li>
        <li><strong>Best for</strong>: Balancing efficiency with goal-directed behavior</li>
      </ul>

      <h2>HTN (Hierarchical Task Network)</h2>
      <p>Recursive decomposition of compound tasks into primitive actions. Define compound tasks with multiple decomposition methods, each with its own preconditions and subtask list.</p>
      <ul>
        <li><strong>Decomposition order</strong>: Methods are tried in declaration order; the first whose preconditions match is used</li>
        <li><strong>Backtracking</strong>: If a subtask's preconditions aren't met, the planner backtracks and tries the next decomposition method</li>
        <li><strong>Primitives</strong>: Actions with no further decomposition — the leaves of the task hierarchy</li>
        <li><strong>Best for</strong>: Structured domains with known task hierarchies</li>
      </ul>
      <pre><code>{`@CompoundTask(name = "MakeMeal")
@Decomposition(name = "quick",
    preconditions = @Fact(name = "preCooked", value = "true"),
    subtasks = {"Reheat", "Serve"})
@Decomposition(name = "fresh",
    subtasks = {"Chop", "Heat", "Cook", "Serve"})
public void makeMeal() { }`}</code></pre>
    </>
  )
}
