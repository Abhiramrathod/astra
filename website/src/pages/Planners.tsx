export default function Planners() {
  return (
    <>
      <h1>Planners</h1>
      <p>
        Astra ships with four built-in planners, each implementing the <code>GoapPlanner</code>
        interface. They are discovered at runtime via Java's <code>ServiceLoader</code> (SPI),
        registered by the <code>PlannerProvider</code> implementations in the
        <code>astra-planners</code> module.
      </p>

      <h2>A* GOAP (Goal-Oriented Action Planning)</h2>
      <p><strong>Strategy:</strong> Optimal A* search from the initial state toward the goal condition.</p>

      <h3>How it works</h3>
      <ol>
        <li>The planner starts with the initial world state as the root node.</li>
        <li>It expands nodes using A* search: at each step, it evaluates all applicable actions and generates successor world states.</li>
        <li>Each node is scored as <code>f(n) = g(n) + h(n)</code> where <code>g(n)</code> is the cumulative cost and <code>h(n)</code> is the heuristic estimate of remaining cost.</li>
        <li>A priority queue ensures the most promising node is expanded first.</li>
        <li>When a node whose state matches the goal condition is popped, the path from root to that node is the optimal plan.</li>
      </ol>

      <h3>Heuristic</h3>
      <p>The default heuristic counts the number of goal conditions not yet satisfied in the current state. This is <strong>admissible</strong> (never overestimates) when each action modifies exactly one fact, guaranteeing an optimal plan.</p>
      <p>Configure via <code>astra.planner.heuristic</code>:</p>
      <ul>
        <li><code>unmatched</code> (default) — count of unmatched goal conditions</li>
        <li><code>zero</code> — no heuristic (degenerates to Dijkstra's algorithm)</li>
      </ul>

      <h3>Configuration</h3>
      <pre><code>{`DefaultAstra.builder()
    .withConfig("astra.planner.maxIterations", "10000")
    .withConfig("astra.planner.heuristic", "unmatched")
    .withPlanner(PlannerType.GOAP)`}</code></pre>

      <table>
        <thead><tr><th>Property</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra.planner.maxIterations</code></td><td><code>10000</code></td><td>Maximum A* iterations before giving up</td></tr>
          <tr><td><code>astra.planner.heuristic</code></td><td><code>unmatched</code></td><td>Heuristic mode: <code>unmatched</code> or <code>zero</code></td></tr>
        </tbody>
      </table>

      <h3>Best for</h3>
      <p>Deterministic environments where plan optimality matters and the action space is not prohibitively large. Examples: manufacturing workflows, NPC decision-making, resource management.</p>

      <h3>Limitations</h3>
      <ul>
        <li>Slower than greedy approaches for large action spaces (exponential worst-case)</li>
        <li>All actions must have deterministic effects</li>
        <li>Does not natively support concurrent or durative actions</li>
      </ul>

      <pre><code>{`// GOAP automatically finds the cheapest path
Astra astra = DefaultAstra.builder()
    .register(new DeliveryAgent())
    .withPlanner(PlannerType.GOAP)
    .build();

// If two routes exist to "Delivered", GOAP picks the cheaper one
ExecutionResult r = astra.executeWithResult("Delivered",
    WorldStates.of("packageReady", "true"));`}</code></pre>

      <hr />

      <h2>Utility AI</h2>
      <p><strong>Strategy:</strong> Greedy best-first selection based on action utility.</p>

      <h3>How it works</h3>
      <ol>
        <li>At each step, the planner evaluates all applicable actions (preconditions met, not already used).</li>
        <li>Each action's effective utility is computed as: <code>effectiveUtility = cost &gt; 0 ? utility / cost : utility</code></li>
        <li>The action with the highest effective utility is selected and executed.</li>
        <li>The planner repeats until no applicable actions remain or the iteration limit is reached.</li>
      </ol>

      <h3>Static vs Dynamic Utility</h3>
      <p>Actions can declare a fixed utility value via the <code>utility</code> annotation field:</p>
      <pre><code>{`@Action(
    name = "HighPriorityTask",
    utility = 10.0f,
    cost = 1.0f
)`}</code></pre>
      <p>Or use a dynamic utility method that receives the current world state and returns a numeric value:</p>
      <pre><code>{`@Action(
    name = "BrewCoffee",
    utility = 4,
    utilityMethod = "getBrewUtility"
)
public void brewCoffee() { }

public double getBrewUtility(WorldState state) {
    boolean waterReady = state.get("waterBoiled")
        .map("true"::equals).orElse(false);
    return waterReady ? 10.0 : 6.0;
}`}</code></pre>
      <p>The utility method must be <code>public</code>, take a single <code>WorldState</code> parameter, and return any numeric type (<code>int</code>, <code>float</code>, <code>double</code>, etc.).</p>

      <h3>Configuration</h3>
      <pre><code>{`DefaultAstra.builder()
    .withConfig("astra.planner.maxIterations", "20")
    .withPlanner(PlannerType.UTILITY)`}</code></pre>

      <table>
        <thead><tr><th>Property</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra.planner.maxIterations</code></td><td><code>20</code></td><td>Maximum steps before stopping</td></tr>
        </tbody>
      </table>

      <h3>Best for</h3>
      <p>Real-time systems where decisions must be made in milliseconds. Games, reactive agents, and any domain where "good enough now" beats "optimal later."</p>

      <h3>Limitations</h3>
      <ul>
        <li>Greedy — does not backtrack or reconsider past decisions</li>
        <li>May get stuck in local optima</li>
        <li>Does not explicitly plan toward a goal (unless the goal emerges from the action effects)</li>
      </ul>

      <hr />

      <h2>Hybrid (Utility + GOAP)</h2>
      <p><strong>Strategy:</strong> Utility-guided greedy selection that checks goal satisfaction at every step.</p>

      <h3>How it works</h3>
      <ol>
        <li>Same greedy selection as Utility AI — picks the highest-utility applicable action at each step.</li>
        <li>After each action, checks whether the goal condition is now satisfied.</li>
        <li>If the goal is met, planning stops immediately (potentially fewer steps than a full plan).</li>
        <li>Reuses utility functions including <code>utilityMethod</code> for dynamic computation.</li>
      </ol>

      <h3>When to use Hybrid over Utility</h3>
      <p>Use the Hybrid planner when actions naturally lead toward a goal but you want the speed of greedy selection. Unlike pure Utility AI, Hybrid knows when to stop (goal reached) and won't waste steps on unnecessary actions.</p>

      <h3>Configuration</h3>
      <pre><code>{`DefaultAstra.builder()
    .withConfig("astra.planner.maxIterations", "100")
    .withPlanner(PlannerType.HYBRID)`}</code></pre>

      <table>
        <thead><tr><th>Property</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra.planner.maxIterations</code></td><td><code>100</code></td><td>Maximum steps before giving up</td></tr>
        </tbody>
      </table>

      <h3>Best for</h3>
      <p>Domains where actions have utility values and a clear goal exists, but optimal planning is too expensive.</p>

      <hr />

      <h2>HTN (Hierarchical Task Network)</h2>
      <p><strong>Strategy:</strong> Recursive decomposition of compound tasks into primitive actions.</p>

      <h3>How it works</h3>
      <ol>
        <li>The planner receives a compound task name (via <code>GoalInfo.getName()</code>).</li>
        <li>If the task name matches a primitive action, it checks preconditions and returns the action (base case).</li>
        <li>If it matches a compound task, the planner iterates through its decomposition methods in declaration order.</li>
        <li>For each decomposition, if preconditions are met, it recursively decomposes each subtask.</li>
        <li>If a subtask fails (preconditions not met), it backtracks to the next decomposition method.</li>
        <li>Returns the flattened list of primitive actions, or <code>null</code> if no decomposition succeeds.</li>
      </ol>

      <h3>Defining compound tasks</h3>
      <pre><code>{`@CompoundTask(name = "MakeMeal")
@Decomposition(name = "quick",
    preconditions = @Fact(name = "preCooked", value = "true"),
    subtasks = {"Reheat", "Serve"})
@Decomposition(name = "fresh",
    subtasks = {"Chop", "Heat", "Cook", "Serve"})
public void makeMeal() {}`}</code></pre>

      <p>Key points:</p>
      <ul>
        <li><code>@CompoundTask</code> defines a named compound task on a method</li>
        <li><code>@Decomposition</code> defines one way to decompose the task (repeatable)</li>
        <li>Decompositions are tried in the order they appear in the source code</li>
        <li>The first decomposition whose preconditions match the current state is used</li>
        <li>Subtasks can reference primitive action names or other compound task names</li>
      </ul>

      <h3>Decomposition fields</h3>
      <table>
        <thead><tr><th>Field</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>name</code></td><td><code>"default"</code></td><td>Decomposition method name (for debugging)</td></tr>
          <tr><td><code>description</code></td><td><code>""</code></td><td>Human-readable description</td></tr>
          <tr><td><code>preconditions</code></td><td><code>{}</code></td><td>World state facts that must be true for this decomposition to be applicable</td></tr>
          <tr><td><code>subtasks</code></td><td>(required)</td><td>Ordered list of subtask names (primitives or compound tasks)</td></tr>
        </tbody>
      </table>

      <h3>Building the HTN planner</h3>
      <pre><code>{`DefaultAstra.builder()
    .register(new CookingAgent())
    .withPlanner(PlannerType.HTN)
    .build();

// HTN reads the goal name as the root compound task name
ExecutionResult r = astra.executeWithResult("MakeDinner",
    WorldStates.of("hasIngredients", "true", "hasKnife", "true"));`}</code></pre>

      <h3>Configuration</h3>
      <table>
        <thead><tr><th>Property</th><th>Default</th><th>Description</th></tr></thead>
        <tbody>
          <tr><td><code>astra.planner.maxIterations</code></td><td><code>200</code></td><td>Maximum decomposition depth</td></tr>
        </tbody>
      </table>

      <h3>Best for</h3>
      <p>Structured domains with well-defined task hierarchies: cooking recipes, manufacturing processes, game boss AI phases, business process automation.</p>

      <hr />

      <h2>Comparison</h2>
      <table>
        <thead>
          <tr><th>Property</th><th>A* GOAP</th><th>Utility AI</th><th>Hybrid</th><th>HTN</th></tr>
        </thead>
        <tbody>
          <tr><td>Optimality</td><td>Optimal (lowest cost)</td><td>Greedy (local optima)</td><td>Greedy + goal-aware</td><td>First matching decomposition</td></tr>
          <tr><td>Speed</td><td>Slowest (exponential worst-case)</td><td>Fastest (linear)</td><td>Fast (linear, early exit)</td><td>Fast (polynomial in practice)</td></tr>
          <tr><td>Goal-aware</td><td>Yes</td><td>No</td><td>Yes</td><td>Yes (task-driven)</td></tr>
          <tr><td>Planning strategy</td><td>Backward/forward A*</td><td>Forward greedy</td><td>Forward greedy</td><td>Top-down decomposition</td></tr>
          <tr><td>Best for</td><td>Optimal path finding</td><td>Real-time decisions</td><td>Goal-directed speed</td><td>Structured hierarchies</td></tr>
        </tbody>
      </table>

      <h2>Selecting a Planner</h2>
      <p>The planner is selected at build time via <code>Builder.withPlanner(PlannerType)</code> and cannot be changed after the <code>Astra</code> instance is created. For applications that need multiple planning strategies, create separate <code>Astra</code> instances.</p>
    </>
  )
}
