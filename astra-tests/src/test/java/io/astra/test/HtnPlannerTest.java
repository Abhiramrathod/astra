package io.astra.test;

import io.astra.api.*;
import io.astra.api.result.*;
import io.astra.core.DefaultAstra;
import io.astra.test.agent.HtnTestAgent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HtnPlannerTest {
    @Test
    void decomposesCompoundTask() {
        Astra astra = DefaultAstra.builder()
            .register(new HtnTestAgent())
            .withPlanner(PlannerType.HTN)
            .build();

        Plan plan = astra.plan("MakeMeal", WorldStates.of("hasKnife", "true"));

        assertTrue(plan.isExecutable());
        assertEquals(4, plan.getActions().size());
        assertEquals("Chop", plan.getActions().get(0).getName());
    }

    @Test
    void usesQuickDecompositionWhenPreconditionMet() {
        Astra astra = DefaultAstra.builder()
            .register(new HtnTestAgent())
            .withPlanner(PlannerType.HTN)
            .build();

        Plan plan = astra.plan("MakeMeal",
            WorldStates.of("preCooked", "true", "hasKnife", "true"));

        assertTrue(plan.isExecutable());
        assertEquals(2, plan.getActions().size());
        assertEquals("Reheat", plan.getActions().get(0).getName());
    }

    @Test
    void achievesGoalWithFreshDecomposition() {
        Astra astra = DefaultAstra.builder()
            .register(new HtnTestAgent())
            .withPlanner(PlannerType.HTN)
            .build();

        ExecutionResult r = astra.executeWithResult("MakeMeal",
            WorldStates.of("hasKnife", "true"));

        assertTrue(r.isSuccess());
        assertEquals("true", r.getFinalState().get("served").orElse("false"));
    }

    @Test
    void achievesGoalWithQuickDecomposition() {
        Astra astra = DefaultAstra.builder()
            .register(new HtnTestAgent())
            .withPlanner(PlannerType.HTN)
            .build();

        ExecutionResult r = astra.executeWithResult("MakeMeal",
            WorldStates.of("preCooked", "true", "hasKnife", "true"));

        assertTrue(r.isSuccess());
        assertEquals("true", r.getFinalState().get("served").orElse("false"));
    }

    @Test
    void returnsNonExecutablePlanWhenUnknownTask() {
        Astra astra = DefaultAstra.builder()
            .register(new HtnTestAgent())
            .withPlanner(PlannerType.HTN)
            .build();

        assertThrows(Exception.class,
            () -> astra.plan("UnknownTask", WorldStates.of()));
    }
}
