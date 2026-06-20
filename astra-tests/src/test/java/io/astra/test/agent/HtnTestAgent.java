package io.astra.test.agent;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.htn.CompoundTask;
import io.astra.annotation.htn.Decomposition;
import io.astra.annotation.fact.Fact;

@Agent
public class HtnTestAgent {
    @Action(name = "Chop", cost = 2,
        preconditions = @Fact(name = "hasKnife", value = "true"),
        effects = @Fact(name = "chopped", value = "true"))
    public void chop() {}

    @Action(name = "Heat", cost = 1,
        preconditions = {},
        effects = @Fact(name = "heated", value = "true"))
    public void heat() {}

    @Action(name = "Cook", cost = 3,
        preconditions = {
            @Fact(name = "chopped", value = "true"),
            @Fact(name = "heated", value = "true")
        },
        effects = @Fact(name = "cooked", value = "true"))
    public void cook() {}

    @Action(name = "Reheat", cost = 1,
        preconditions = @Fact(name = "preCooked", value = "true"),
        effects = @Fact(name = "cooked", value = "true"))
    public void reheat() {}

    @Action(name = "Serve", cost = 1,
        preconditions = @Fact(name = "cooked", value = "true"),
        effects = @Fact(name = "served", value = "true"))
    public void serve() {}

    @CompoundTask(name = "MakeMeal")
    @Decomposition(name = "quick", preconditions = @Fact(name = "preCooked", value = "true"),
                  subtasks = {"Reheat", "Serve"})
    @Decomposition(name = "fresh", subtasks = {"Chop", "Heat", "Cook", "Serve"})
    public void makeMeal() {}
}
