package io.astra.test.agent;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;

@Agent
public class GoapTestAgent {
    @Action(name = "CheapAction", cost = 1,
        preconditions = @Fact(name = "a", value = "false"),
        effects = @Fact(name = "a", value = "true"))
    public void cheap() {}

    @Action(name = "ExpensiveAction", cost = 10,
        preconditions = @Fact(name = "a", value = "false"),
        effects = @Fact(name = "a", value = "true"))
    public void expensive() {}

    @Goal(name = "ReachA",
        condition = @Fact(name = "a", value = "true"))
    public void reachA() {}
}
