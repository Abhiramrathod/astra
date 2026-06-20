package io.astra.test;

import io.astra.api.*;
import io.astra.api.result.*;
import io.astra.core.DefaultAstra;
import io.astra.test.agent.UtilityBasedTestAgent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilityBasedPlannerTest {
    @Test
    void picksHighUtilityAction() {
        Astra astra = DefaultAstra.builder()
            .register(new UtilityBasedTestAgent())
            .withPlanner(PlannerType.UTILITY_BASED)
            .build();

        Plan plan = astra.plan("Finish", WorldStates.of("ready", "true"));

        assertTrue(plan.isExecutable());
        assertEquals("HighUtility", plan.getActions().get(0).getName());
    }

    @Test
    void achievesGoal() {
        Astra astra = DefaultAstra.builder()
            .register(new UtilityBasedTestAgent())
            .withPlanner(PlannerType.UTILITY_BASED)
            .build();

        ExecutionResult r = astra.executeWithResult("Finish", WorldStates.of("ready", "true"));

        assertTrue(r.isSuccess());
    }
}
