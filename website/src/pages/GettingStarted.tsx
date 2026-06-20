export default function GettingStarted() {
  return (
    <>
      <h1>Getting Started</h1>

      <h2>Prerequisites</h2>
      <ul>
        <li>Java 17 or higher</li>
        <li>Maven 3.8+ (or the bundled <code>./mvnw</code> wrapper)</li>
      </ul>

      <h2>Add to your project</h2>
      <p>Replace <code>VERSION</code> with the latest release:</p>
      <pre><code>{`<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-core</artifactId>
    <version>VERSION</version>
</dependency>`}</code></pre>
      <p>This pulls in every module you need: <code>astra-api</code>, <code>astra-planners</code>, <code>astra-scanner</code>, etc.</p>

      <h2>Write your first agent</h2>
      <p>Create a class annotated with <code>@Agent</code>, add action methods with <code>@Action</code>, and define goals with <code>@Goal</code>:</p>
      <pre><code>{`import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;

@Agent
public class CoffeeAgent {

    @Action(
        name = "GrindBeans",
        description = "Grind whole coffee beans into fine grounds",
        preconditions = @Fact(name = "hasBeans", value = "true"),
        effects = @Fact(name = "beansGround", value = "true"),
        utility = 2
    )
    public void grindBeans() {
        System.out.println("Grinding coffee beans...");
    }

    @Action(
        name = "BrewCoffee",
        description = "Brew coffee using ground beans and hot water",
        preconditions = {
            @Fact(name = "beansGround", value = "true"),
            @Fact(name = "waterBoiled", value = "true")
        },
        effects = @Fact(name = "coffeeBrewed", value = "true"),
        utility = 4,
        utilityMethod = "getBrewUtility"
    )
    public void brewCoffee() {
        System.out.println("Brewing coffee...");
    }

    public double getBrewUtility(WorldState state) {
        // Dynamic utility based on world state
        return state.get("waterBoiled")
            .map(v -> 10.0).orElse(6.0);
    }

    @Action(
        name = "ServeCoffee",
        description = "Pour the brewed coffee into a cup and serve",
        preconditions = @Fact(name = "coffeeBrewed", value = "true"),
        effects = @Fact(name = "coffeeServed", value = "true")
    )
    public void serveCoffee() {
        System.out.println("Serving coffee!");
    }

    @Goal(
        name = "MakeCoffee",
        description = "Brew and serve a fresh cup of coffee",
        condition = @Fact(name = "coffeeServed", value = "true")
    )
    public void coffeeGoal() {}
}`}</code></pre>

      <h2>Run it</h2>
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

      <h2>Try different planners</h2>
      <p>Change the planner with a single line:</p>
      <pre><code>{`.withPlanner(PlannerType.UTILITY)  // Utility AI
.withPlanner(PlannerType.HYBRID)   // Hybrid
.withPlanner(PlannerType.HTN)      // HTN
.withPlanner(PlannerType.GOAP)     // A* GOAP (default)`}</code></pre>
    </>
  )
}
