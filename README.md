<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Abhiramrathod/astra/main/website/public/logo-dark.svg">
  <img alt="Astra" src="https://raw.githubusercontent.com/Abhiramrathod/astra/main/website/public/logo-light.svg" width="300">
</picture>

# Astra — Multi-Planner Agent Framework for Java

[![Build](https://github.com/Abhiramrathod/astra/actions/workflows/ci.yml/badge.svg)](https://github.com/Abhiramrathod/astra/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.astra/astra-core)](https://central.sonatype.com/artifact/io.astra/astra-core)
[![Java 17+](https://img.shields.io/badge/java-17%2B-blue)](https://adoptium.net)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)

**Astra** is a production-grade, pure-Java agent framework with zero LLM dependency. It provides four built-in planning strategies — A\* GOAP, Utility AI, Hybrid, and HTN — all selectable via a single builder API.

```java
Astra astra = DefaultAstra.builder()
    .register(new CoffeeAgent())
    .withPlanner(PlannerType.GOAP)
    .build();

ExecutionResult r = astra.executeWithResult("MakeCoffee",
    WorldStates.of("hasBeans", "true"));
```

---

## Features

- **Four planners in one framework** — GOAP (A\*), Utility AI, Hybrid, HTN
- **Annotation-driven agent DSL** — `@Agent`, `@Action`, `@Goal`, `@CompoundTask`, `@Decomposition`, `@Fact`
- **Dynamic utility** — state-aware utility functions via `utilityMethod`
- **SPI plugin system** — third-party planners via `ServiceLoader`
- **Event bus** — publish/subscribe for lifecycle and planning events
- **Interceptor chain** — hook before/after every action
- **Lifecycle-aware agents** — `LifecycleAware` for `onInit`/`onDestroy`
- **AstraConfig** — configurable planner heuristics, iteration limits
- **JSON serialization** — `WorldStateSerializer` with Jackson (optional)
- **Spring Boot** — `@EnableAstra` auto-configuration module
- **Zero LLM dependency** — no external AI services required
- **Java 17+** — records, sealed classes, pattern matching

---

## Module Overview

```
astra-parent
├── astra-annotations   — @Agent, @Action, @Goal, @CompoundTask, @Decomposition, @Fact
├── astra-api           — Interfaces: Astra, GoapPlanner, ActionInfo, WorldState, EventBus, ...
├── astra-utils         — ClassPathScanner, WorldStateSerializer (Jackson)
├── astra-scanner       — AgentScanner (reflects annotations into API types)
├── astra-config        — MapConfigProvider, PropertiesFileConfigProvider
├── astra-exceptions    — Typed exception hierarchy (action, agent, goal, state)
├── astra-events        — DefaultEventBus implementation
├── astra-interceptors  — DefaultInterceptorChain implementation
├── astra-lifecycle     — LifecycleManager
├── astra-planners      — All 4 planner implementations + SPI providers
├── astra-core          — DefaultAstra builder + wiring
├── astra-spring        — Spring Boot auto-configuration
├── astra-sample        — CoffeeAgent, CookingAgent demos
└── astra-tests         — JUnit 5 test suite (12 tests)
```

**Build order** (enforced by dependency chain):
`annotations → api → utils + scanner + config + exceptions + events + interceptors + lifecycle + planners → core → spring → sample → tests`

---

## Getting Started

### Prerequisites

- Java 17+
- Maven (or use the bundled Maven wrapper `./mvnw`)

### Add to your project

Replace `VERSION` with the latest release.

```xml
<dependency>
    <groupId>io.astra</groupId>
    <artifactId>astra-core</artifactId>
    <version>VERSION</version>
</dependency>
```

Maven automatically pulls in `astra-planners`, `astra-api`, `astra-scanner`, and all other required modules.

### Write your first agent

```java
import io.astra.annotation.Agent;
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
}
```

### Run it

```java
import io.astra.api.*;
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
    }
}
```

---

## Planners

### A\* GOAP (Goal-Oriented Action Planning)

- **Strategy**: A\* search over the action space. Finds the lowest-cost (optimal) sequence of actions.
- **Heuristic**: Count of unmatched goal conditions (admissible for single-effect actions).
- **Configurable**: `astra.planner.heuristic` (unmatched / zero), `astra.planner.maxIterations`.
- **Best for**: Deterministic worlds where optimality matters more than speed.

### Utility AI

- **Strategy**: Greedy selection — at each step, pick the highest-utility applicable action.
- **Utility = value / cost**: Normalizes utility by cost for apples-to-apples comparison.
- **Configurable**: `astra.planner.maxIterations`.
- **Best for**: Real-time decisions where speed matters and near-optimal is acceptable.

### Hybrid (Utility + GOAP)

- **Strategy**: Greedy forward search guided by utility, but checks goal satisfaction after each step.
- **Combines**: Utility's fast selection with GOAP's goal-awareness.
- **Configurable**: `astra.planner.maxIterations`.
- **Best for**: Balancing efficiency with goal-directed behavior.

### HTN (Hierarchical Task Network)

- **Strategy**: Recursive decomposition of compound tasks into primitive actions.
- **Declarative**: Define tasks, methods (decompositions), and preconditions.
- **Backtracking**: Falls back to the next decomposition method if subtask preconditions aren't met.
- **Best for**: Structured domains with known task hierarchies (e.g., cooking, manufacturing).

---

## Dynamic Utility

Annotate an action with `utilityMethod` to compute utility from the current world state at planning time:

```java
@Action(
    name = "BrewCoffee",
    utility = 4,
    utilityMethod = "getBrewUtility"
)
public void brewCoffee() { ... }

public double getBrewUtility(WorldState state) {
    boolean hasBeans = state.get("hasBeans")
        .map("true"::equals).orElse(false);
    boolean waterReady = state.get("waterBoiled")
        .map("true"::equals).orElse(false);
    if (hasBeans && waterReady) return 10.0;
    if (hasBeans) return 6.0;
    return 3.0;
}
```

The method must be `public`, take a single `WorldState` parameter, and return a numeric type (`int`, `float`, `double`, etc.).

---

## SPI Plugin System

To add a custom planner:

1. Implement `GoapPlanner` and `PlannerProvider`
2. Register the provider in `META-INF/services/io.astra.api.PlannerProvider`
3. Add your module to the classpath

```java
public class MyPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.GOAP; }

    @Override
    public GoapPlanner create(AstraConfig config,
                              Map<String, CompoundTaskDef> compoundTasks,
                              List<ActionInfo> actions) {
        return new MyPlanner(config);
    }
}
```

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `astra.planner.maxIterations` | 10000 (GOAP), 20 (Utility), 100 (Hybrid), 200 (HTN) | Max search iterations |
| `astra.planner.heuristic` | `unmatched` | GOAP heuristic mode (`unmatched` / `zero`) |

Set via the builder:

```java
DefaultAstra.builder()
    .withConfig("astra.planner.maxIterations", "5000")
    .withConfig("astra.planner.heuristic", "unmatched")
    ...
```

---

## Building from Source

```bash
# Clone the repo
git clone https://github.com/Abhiramrathod/astra.git
cd astra

# Build all modules (skip tests for speed)
./mvnw clean install -DskipTests

# Run tests
./mvnw test -pl astra-tests

# Run the sample application
./mvnw exec:java -pl astra-sample \
    -Dexec.mainClass="io.astra.sample.Main"
```

Requires JDK 17+. The Maven wrapper (`mvnw`) handles the rest automatically.

---

## Running Tests

```bash
./mvnw test -pl astra-tests
```

Tests cover all four planners:
- **GoapPlannerTest** (3 tests) — cheapest path, goal achievement, no-path case
- **UtilityPlannerTest** (2 tests) — highest utility selection, goal achievement
- **HybridPlannerTest** (2 tests) — sequential plan, execution
- **HtnPlannerTest** (5 tests) — full decomposition, conditional branching, execution, unknown task

---

## License

```
Copyright 2026 Abhiram Rathod

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
