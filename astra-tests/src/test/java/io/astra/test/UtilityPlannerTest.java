package io.astra.test;

import io.astra.api.*;
import io.astra.api.result.*;
import io.astra.core.DefaultAstra;
import io.astra.test.agent.UtilityTestAgent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilityPlannerTest {
    @Test
    void picksHighUtilityAction() {
        Astra astra = DefaultAstra.builder()
            .register(new UtilityTestAgent())
            .withPlanner(PlannerType.UTILITY)
            .build();

        Plan plan = astra.plan("Finish", WorldStates.of("ready", "true"));

        assertTrue(plan.isExecutable());
        assertEquals("HighUtility", plan.getActions().get(0).getName());
    }

    @Test
    void achievesGoal() {
        Astra astra = DefaultAstra.builder()
            .register(new UtilityTestAgent())
            .withPlanner(PlannerType.UTILITY)
            .build();

        ExecutionResult r = astra.executeWithResult("Finish", WorldStates.of("ready", "true"));

        assertTrue(r.isSuccess());
    }
}
