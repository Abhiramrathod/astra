package io.astra.sample;

import io.astra.api.*;
import io.astra.api.event.*;
import io.astra.api.interceptor.*;
import io.astra.api.result.*;
import io.astra.api.WorldStates;
import io.astra.core.DefaultAstra;
import io.astra.sample.agent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        demoCostBased();
        demoUtility();
        demoHybrid();
        demoStructural();
    }

    static void demoCostBased() {
        System.out.println("\n========== Cost-Based Planner Demo ==========");
        Astra astra = DefaultAstra.builder()
            .register(new CoffeeAgent())
            .withPlanner(PlannerType.COST_BASED)
            .build();
        Plan plan = astra.plan("MakeCoffee", WorldStates.of("hasBeans", "true"));
        System.out.println("Plan steps:");
        for (ActionInfo a : plan.getActions()) {
            System.out.println("  " + a.getName() + " - " + a.getDescription());
        }
        ExecutionResult r = astra.executeWithResult("MakeCoffee", WorldStates.of("hasBeans", "true"));
        System.out.println("CostBased result: success=" + r.isSuccess() + ", steps=" + r.getActionRecords().size());
    }

    static void demoUtility() {
        System.out.println("\n========== Utility AI Planner Demo ==========");
        Astra astra = DefaultAstra.builder()
            .register(new CoffeeAgent())
            .withPlanner(PlannerType.UTILITY_BASED)
            .withConfig("astra.planner.maxIterations", "10")
            .build();
        ExecutionResult r = astra.executeWithResult("MakeCoffee", WorldStates.of("hasBeans", "true"));
        System.out.println("UtilityBased result: success=" + r.isSuccess() + ", steps=" + r.getActionRecords().size());
    }

    static void demoHybrid() {
        System.out.println("\n========== Hybrid Planner Demo ==========");
        Astra astra = DefaultAstra.builder()
            .register(new CoffeeAgent())
            .withPlanner(PlannerType.HYBRID)
            .withConfig("astra.planner.maxIterations", "20")
            .build();
        ExecutionResult r = astra.executeWithResult("MakeCoffee", WorldStates.of("hasBeans", "true"));
        System.out.println("Hybrid result: success=" + r.isSuccess() + ", steps=" + r.getActionRecords().size());
    }

    static void demoStructural() {
        System.out.println("\n========== Structural Planner Demo ==========");
        Astra astra = DefaultAstra.builder()
            .register(new CookingAgent())
            .withPlanner(PlannerType.STRUCTURAL)
            .build();
        ExecutionResult r = astra.executeWithResult("MakeDinner", WorldStates.of("hasIngredients", "true", "hasKnife", "true", "hasPot", "true"));
        System.out.println("Structural result: success=" + r.isSuccess() + ", steps=" + r.getActionRecords().size());
    }
}
