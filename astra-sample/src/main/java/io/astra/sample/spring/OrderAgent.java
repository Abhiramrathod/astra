package io.astra.sample.spring;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;
import org.springframework.stereotype.Component;

@Component
@Agent
public class OrderAgent {
    @Action(name = "ValidatePayment",
        preconditions = @Fact(name = "orderReceived", value = "true"),
        effects = @Fact(name = "paymentValidated", value = "true"))
    void validate() {}

    @Action(name = "PickItems",
        preconditions = @Fact(name = "paymentValidated", value = "true"),
        effects = @Fact(name = "itemsPicked", value = "true"))
    void pick() {}

    @Action(name = "ShipOrder",
        preconditions = @Fact(name = "itemsPicked", value = "true"),
        effects = @Fact(name = "orderShipped", value = "true"))
    void ship() {}

    @Goal(name = "ProcessOrder",
        condition = @Fact(name = "orderShipped", value = "true"))
    void process() {}
}
