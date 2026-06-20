package io.astra.test;

import io.astra.api.*;
import io.astra.api.result.*;
import io.astra.core.DefaultAstra;
import io.astra.test.agent.CostBasedTestAgent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CostBasedPlannerTest {
    @Test
    void findsCheapestPlan() {
        Astra astra = DefaultAstra.builder()
            .register(new CostBasedTestAgent())
            .withPlanner(PlannerType.COST_BASED)
            .build();

        Plan plan = astra.plan("ReachA", WorldStates.of("a", "false"));

        assertTrue(plan.isExecutable());
        assertEquals(1, plan.getActions().size());
        assertEquals("CheapAction", plan.getActions().get(0).getName());
        assertEquals(1.0, plan.getTotalCost());
    }

    @Test
    void achievesGoal() {
        Astra astra = DefaultAstra.builder()
            .register(new CostBasedTestAgent())
            .withPlanner(PlannerType.COST_BASED)
            .build();

        ExecutionResult r = astra.executeWithResult("ReachA", WorldStates.of("a", "false"));

        assertTrue(r.isSuccess());
        assertEquals("true", r.getFinalState().get("a").orElse("false"));
    }

    @Test
    void returnsNonExecutablePlanWhenNoPath() {
        Astra astra = DefaultAstra.builder()
            .register(new CostBasedTestAgent())
            .withPlanner(PlannerType.COST_BASED)
            .build();

        Plan plan = astra.plan("ReachA", WorldStates.of());

        assertFalse(plan.isExecutable());
    }
}
