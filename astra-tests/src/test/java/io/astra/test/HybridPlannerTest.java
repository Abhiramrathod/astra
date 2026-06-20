package io.astra.test;

import io.astra.api.*;
import io.astra.api.result.*;
import io.astra.core.DefaultAstra;
import io.astra.test.agent.HybridTestAgent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HybridPlannerTest {
    @Test
    void findsPlanToGoal() {
        Astra astra = DefaultAstra.builder()
            .register(new HybridTestAgent())
            .withPlanner(PlannerType.HYBRID)
            .build();

        Plan plan = astra.plan("Exit", WorldStates.of());

        assertTrue(plan.isExecutable());
        assertEquals(2, plan.getActions().size());
        assertEquals("GetKey", plan.getActions().get(0).getName());
        assertEquals("OpenDoor", plan.getActions().get(1).getName());
    }

    @Test
    void achievesGoal() {
        Astra astra = DefaultAstra.builder()
            .register(new HybridTestAgent())
            .withPlanner(PlannerType.HYBRID)
            .build();

        ExecutionResult r = astra.executeWithResult("Exit", WorldStates.of());

        assertTrue(r.isSuccess());
        assertEquals("true", r.getFinalState().get("doorOpen").orElse("false"));
    }
}
