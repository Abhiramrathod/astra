package io.astra.test.agent;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;

@Agent
public class UtilityTestAgent {
    @Action(name = "HighUtility", cost = 1, utility = 10,
        preconditions = @Fact(name = "ready", value = "true"),
        effects = @Fact(name = "done", value = "high"))
    public void highUtility() {}

    @Action(name = "LowUtility", cost = 1, utility = 1,
        preconditions = @Fact(name = "ready", value = "true"),
        effects = @Fact(name = "done", value = "low"))
    public void lowUtility() {}

    @Goal(name = "Finish",
        condition = @Fact(name = "done", value = "high"))
    public void finish() {}
}
