package io.astra.sample.structural;

import io.astra.api.*;
import io.astra.api.result.ExecutionResult;
import io.astra.core.DefaultAstra;

/** Demonstrates the structural planner with compound task decomposition. */
public class StructuralDemo {
    public static void run() {
        System.out.println("\n=== Structural: Compound Task Decomposition ===");
        Astra astra = DefaultAstra.builder()
            .register(new CookingAgent())
            .withPlanner(PlannerType.STRUCTURAL)
            .build();
        ExecutionResult r = astra.executeWithResult("MakeDinner",
            WorldStates.of("hasIngredients", "true", "hasKnife", "true", "hasPot", "true"));
        System.out.println("  Structural result: success=" + r.isSuccess() + ", steps=" + r.getActionRecords().size());
    }
}
