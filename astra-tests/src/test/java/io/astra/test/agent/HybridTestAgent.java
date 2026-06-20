package io.astra.test.agent;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;

@Agent
public class HybridTestAgent {
    @Action(name = "GetKey", cost = 2, utility = 3,
        preconditions = {},
        effects = @Fact(name = "hasKey", value = "true"))
    public void getKey() {}

    @Action(name = "OpenDoor", cost = 1, utility = 5,
        preconditions = @Fact(name = "hasKey", value = "true"),
        effects = @Fact(name = "doorOpen", value = "true"))
    public void openDoor() {}

    @Goal(name = "Exit",
        condition = @Fact(name = "doorOpen", value = "true"))
    public void exit() {}
}
