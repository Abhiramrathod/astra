package io.astra.sample.basic;

import io.astra.api.*;
import io.astra.api.result.ExecutionResult;
import io.astra.core.DefaultAstra;

/** Demonstrates the annotation-based agent DSL with all four planner types. */
public class BasicDemo {
    public static void run() {
        System.out.println("\n=== Basic: Annotation-Based Agent ===");
        for (PlannerType pt : new PlannerType[]{PlannerType.COST_BASED, PlannerType.UTILITY_BASED, PlannerType.HYBRID}) {
            Astra astra = DefaultAstra.builder().register(new CoffeeAgent()).withPlanner(pt).build();
            ExecutionResult r = astra.executeWithResult("MakeCoffee", WorldStates.of("hasBeans", "true"));
            System.out.println("  " + pt + ": success=" + r.isSuccess() + ", steps=" + r.getActionRecords().size());
        }
    }
}
