export default function Spring() {
  return (
    <>
      <h1>Spring Boot Integration</h1>
      <p>
        Astra provides first-class Spring Boot support via the <code>astra-spring</code> module.
        Add the dependency, annotate your application with <code>@EnableAstra</code>, and an
        <code>Astra</code> bean is automatically configured in the application context.
      </p>

      <h2>Setup</h2>

      <h3>1. Add the dependency</h3>
      <pre><code>{`<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-spring</artifactId>
    <version>0.1.0</version>
</dependency>`}</code></pre>

      <h3>2. Enable Astra</h3>
      <p>Annotate your main application class or any configuration class:</p>
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
      <p>The <code>Astra</code> instance is available for injection anywhere in your application:</p>
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

      <h2>Configuration</h2>

      <h3>Application Properties</h3>
      <p>Planner settings can be configured via <code>application.properties</code> or <code>application.yml</code>:</p>
      <pre><code>{`# application.properties
astra.planner.type=COST_BASED
astra.planner.maxIterations=5000
astra.planner.heuristic=unmatched`}</code></pre>

      <p>Or with YAML:</p>
      <pre><code>{`# application.yml
astra:
  planner:
    type: COST_BASED
    maxIterations: 5000
    heuristic: unmatched`}</code></pre>

      <h3>Planner Selection</h3>
      <p>Set the planner type via property. The <code>@EnableAstra</code> auto-configuration reads this value:</p>
      <table>
        <thead><tr><th>Property</th><th>Default</th><th>Values</th></tr></thead>
        <tbody>
          <tr><td><code>astra.planner.type</code></td><td><code>COST_BASED</code></td><td><code>COST_BASED</code>, <code>UTILITY_BASED</code>, <code>HYBRID</code>, <code>STRUCTURAL</code></td></tr>
        </tbody>
      </table>

      <h3>Custom Bean Configuration</h3>
      <p>If you need full control, define the <code>Astra</code> bean yourself instead of using <code>@EnableAstra</code>:</p>
      <pre><code>{`import io.astra.api.*;
import io.astra.core.DefaultAstra;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AstraConfig {
    @Bean
    public Astra astra() {
        return DefaultAstra.builder()
            .register(new CoffeeAgent())
            .register(new CookingAgent())
            .withPlanner(PlannerType.HYBRID)
            .withConfig("astra.planner.maxIterations", "3000")
            .build();
    }
}`}</code></pre>

      <h2>Registering Agents</h2>

      <h3>As Spring Beans</h3>
      <p>You can also make your agents Spring beans and inject them into the builder:</p>
      <pre><code>{`import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.goal.Goal;
import org.springframework.stereotype.Component;

@Component
@Agent
public class ShippingAgent {
    @Action(name = "ShipOrder",
        preconditions = @Fact(name = "packageReady", value = "true"),
        effects = @Fact(name = "orderShipped", value = "true"))
    public void ship() { /* ... */ }

    @Goal(name = "FulfillOrder",
        condition = @Fact(name = "orderShipped", value = "true"))
    public void fulfill() {}
}`}</code></pre>

      <pre><code>{`import io.astra.api.*;
import io.astra.core.DefaultAstra;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AstraConfig {
    @Bean
    public Astra astra(ShippingAgent shippingAgent, InvoiceAgent invoiceAgent) {
        return DefaultAstra.builder()
            .register(shippingAgent)
            .register(invoiceAgent)
            .withPlanner(PlannerType.COST_BASED)
            .build();
    }
}`}</code></pre>

      <h2>Using Events with Spring</h2>
      <p>The Astra event bus can bridge to Spring's application event system:</p>
      <pre><code>{`import io.astra.api.event.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class AstraEventBridge {
    private final Astra astra;
    private final ApplicationEventPublisher springEvents;

    public AstraEventBridge(Astra astra, ApplicationEventPublisher springEvents) {
        this.astra = astra;
        this.springEvents = springEvents;
    }

    @PostConstruct
    public void bridge() {
        astra.getEventBus().subscribe(event ->
            springEvents.publishEvent(event));
    }
}`}</code></pre>

      <h2>Async Execution</h2>
      <p>Combine Astra's async execution with Spring's <code>@Async</code>:</p>
      <pre><code>{`import io.astra.api.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class PlanningService {
    private final Astra astra;

    public PlanningService(Astra astra) {
        this.astra = astra;
    }

    @Async
    public CompletableFuture<ExecutionResult> planAsync(String goal, WorldState state) {
        return astra.executeAsync(goal, state);
    }
}`}</code></pre>

      <h2>Full Application Example</h2>
      <pre><code>{`@SpringBootApplication
@EnableAstra
public class OrderApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class, args);
    }
}

@Component
@Agent
class OrderAgent {
    @Action(name = "ValidatePayment",
        preconditions = @Fact(name = "orderReceived", value = "true"),
        effects = @Fact(name = "paymentValidated", value = "true"))
    void validate() { /* ... */ }

    @Action(name = "PickItems",
        preconditions = @Fact(name = "paymentValidated", value = "true"),
        effects = @Fact(name = "itemsPicked", value = "true"))
    void pick() { /* ... */ }

    @Action(name = "ShipOrder",
        preconditions = @Fact(name = "itemsPicked", value = "true"),
        effects = @Fact(name = "orderShipped", value = "true"))
    void ship() { /* ... */ }

    @Goal(name = "ProcessOrder",
        condition = @Fact(name = "orderShipped", value = "true"))
    void process() {}
}

@Service
class OrderService {
    private final Astra astra;

    OrderService(Astra astra) {
        this.astra = astra;
    }

    ExecutionResult process(String orderId) {
        return astra.executeWithResult("ProcessOrder",
            WorldStates.of("orderReceived", "true"));
    }
}`}</code></pre>
    </>
  )
}