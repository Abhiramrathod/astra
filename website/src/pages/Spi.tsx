export default function Spi() {
  return (
    <>
      <h1>SPI & Custom Planners</h1>
      <p>
        Astra uses Java's <code>ServiceLoader</code> (SPI) to discover planners at runtime.
        You can add a completely custom planner without modifying Astra's source code
        — just implement the interfaces, register via SPI, and drop your jar on the classpath.
      </p>

      <h2>Architecture</h2>
      <p>The SPI layer involves three contracts:</p>
      <table>
        <thead><tr><th>Interface</th><th>Role</th></tr></thead>
        <tbody>
          <tr><td><code>Planner</code></td><td>Your planning algorithm. Receives the current state, goal, and available actions; returns a <code>Plan</code>.</td></tr>
          <tr><td><code>PlannerProvider</code></td><td>Factory that creates your <code>Planner</code> for a given <code>PlannerType</code>. Discovered by <code>ServiceLoader</code>.</td></tr>
          <tr><td><code>PlannerType</code></td><td>Enum constant that identifies which planner to use. Select via <code>Builder.withPlanner(PlannerType)</code>.</td></tr>
        </tbody>
      </table>

      <h2>Step-by-Step Guide</h2>

      <h3>1. Implement the Planner</h3>
      <p>Create a class that implements <code>io.astra.api.Planner</code>:</p>
      <pre><code>{`package com.myapp.planner;

import io.astra.api.*;
import java.util.List;

public class MyCustomPlanner implements Planner {
    @Override
    public Plan plan(WorldState currentState,
                     GoalInfo goal,
                     List<ActionInfo> actions) {
        // Your planning logic here.
        // Return a DefaultPlan with the selected actions and total cost.
        return new DefaultPlan(actions, actions.stream()
            .mapToDouble(ActionInfo::getCost).sum());
    }
}`}</code></pre>

      <h3>2. Implement the Provider</h3>
      <p>Create a factory that maps a <code>PlannerType</code> to your planner:</p>
      <pre><code>{`package com.myapp.planner;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import java.util.List;
import java.util.Map;

public class MyPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() {
        return PlannerType.COST_BASED;
    }

    @Override
    public Planner create(AstraConfig config,
                          Map<String, CompoundTaskDef> compoundTasks,
                          List<ActionInfo> actions) {
        return new MyCustomPlanner();
    }
}`}</code></pre>
      <blockquote>
        <strong>Important:</strong> The <code>PlannerType</code> you return determines
        which built-in planner your custom one replaces. If you return
        <code>PlannerType.COST_BASED</code>, your planner will be used whenever
        the builder is configured with <code>.withPlanner(PlannerType.COST_BASED)</code>.
      </blockquote>

      <h3>3. Register via ServiceLoader</h3>
      <p>Create the SPI descriptor file at:</p>
      <p><code>src/main/resources/META-INF/services/io.astra.api.PlannerProvider</code></p>
      <p>With the fully qualified class name of your provider:</p>
      <pre><code>{`com.myapp.planner.MyPlannerProvider`}</code></pre>

      <h3>4. Add to classpath</h3>
      <p>When your jar is on the runtime classpath, <code>DefaultAstra</code> discovers it automatically:</p>
      <pre><code>{`Astra astra = DefaultAstra.builder()
    .register(new MyAgent())
    .withPlanner(PlannerType.COST_BASED)
    .build();`}</code></pre>

      <h2>Custom PlannerType Values</h2>
      <p>
        The <code>PlannerType</code> enum is closed (you cannot add new constants).
        If you want a truly distinct planner type without replacing a built-in one,
        you have two options:
      </p>

      <h3>Option A: Replace a built-in slot</h3>
      <p>
        Return an existing <code>PlannerType</code> from your provider. Your planner
        replaces the built-in one for that type. This is the simplest approach and
        works well when you only need one custom planner.
      </p>

      <h3>Option B: Use the planner directly</h3>
      <p>
        Bypass the SPI entirely and construct your planner manually, then pass it
        to a custom <code>Astra</code> implementation or use it standalone:
      </p>
      <pre><code>{`Planner myPlanner = new MyCustomPlanner();
Plan result = myPlanner.plan(state, goal, actions);`}</code></pre>

      <h2>Full Example</h2>
      <p>A random-action planner that picks applicable actions at random:</p>
      <pre><code>{`public class RandomPlanner implements Planner {
    private final Random rand = new Random();

    @Override
    public Plan plan(WorldState state, GoalInfo goal,
                     List<ActionInfo> actions) {
        List<ActionInfo> plan = new ArrayList<>();
        WorldState current = state;
        for (int i = 0; i < 100; i++) {
            List<ActionInfo> applicable = actions.stream()
                .filter(a -> current.matches(a.getPreconditions()))
                .toList();
            if (applicable.isEmpty()) break;
            ActionInfo chosen = applicable.get(rand.nextInt(applicable.size()));
            plan.add(chosen);
            for (var e : chosen.getEffects().entrySet())
                current = current.set(e.getKey(), e.getValue());
            if (current.matches(goal.getCondition())) break;
        }
        double cost = plan.stream().mapToDouble(ActionInfo::getCost).sum();
        return new DefaultPlan(plan, cost);
    }
}`}</code></pre>

      <h2>Testing Your Custom Planner</h2>
      <p>Unit-test your planner like any other Java class:</p>
      <pre><code>{`class RandomPlannerTest {
    @Test
    void testPlanning() {
        Planner planner = new RandomPlanner();
        List<ActionInfo> actions = List.of(/* ... */);
        GoalInfo goal = /* ... */;
        Plan plan = planner.plan(initialState, goal, actions);
        assertNotNull(plan);
    }
}`}</code></pre>

      <h2>Built-in Provider Reference</h2>
      <p>Astra ships with four providers registered in <code>META-INF/services/io.astra.api.PlannerProvider</code>:</p>
      <table>
        <thead><tr><th>Provider</th><th>PlannerType</th><th>Planner</th></tr></thead>
        <tbody>
          <tr><td><code>CostBasedPlannerProvider</code></td><td><code>COST_BASED</code></td><td><code>CostBasedPlanner</code></td></tr>
          <tr><td><code>UtilityBasedPlannerProvider</code></td><td><code>UTILITY_BASED</code></td><td><code>UtilityBasedPlanner</code></td></tr>
          <tr><td><code>HybridPlannerProvider</code></td><td><code>HYBRID</code></td><td><code>HybridPlanner</code></td></tr>
          <tr><td><code>StructuralPlannerProvider</code></td><td><code>STRUCTURAL</code></td><td><code>StructuralPlanner</code></td></tr>
        </tbody>
      </table>
    </>
  )
}