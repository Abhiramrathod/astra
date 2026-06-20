package io.astra.sample.agent;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.goal.Goal;
import io.astra.api.WorldState;
import io.astra.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent
public class CoffeeAgent implements LifecycleAware {
    private static final Logger log = LoggerFactory.getLogger(CoffeeAgent.class);

    @Override
    public void onInit() {
        log.info("CoffeeAgent initializing - stocking supplies");
    }

    @Override
    public void onDestroy() {
        log.info("CoffeeAgent shutting down - cleaning machine");
    }

    @Action(
        name = "GrindBeans",
        description = "Grind whole coffee beans into fine grounds",
        preconditions = @Fact(name = "hasBeans", value = "true"),
        effects = @Fact(name = "beansGround", value = "true"),
        utility = 2
    )
    public void grindBeans() {
        log.info("Grinding coffee beans...");
    }

    @Action(
        name = "BoilWater",
        description = "Heat water to brewing temperature",
        preconditions = @Fact(name = "beansGround", value = "true"),
        effects = @Fact(name = "waterBoiled", value = "true"),
        utility = 3
    )
    public void boilWater() {
        log.info("Boiling water...");
    }

    @Action(
        name = "BrewCoffee",
        description = "Brew coffee using ground beans and hot water",
        preconditions = {
            @Fact(name = "beansGround", value = "true"),
            @Fact(name = "waterBoiled", value = "true")
        },
        effects = @Fact(name = "coffeeBrewed", value = "true"),
        utility = 4,
        utilityMethod = "getBrewUtility"
    )
    public void brewCoffee() {
        log.info("Brewing coffee...");
    }

    public double getBrewUtility(WorldState state) {
        boolean hasBeans = state.get("hasBeans").map("true"::equals).orElse(false);
        boolean waterReady = state.get("waterBoiled").map("true"::equals).orElse(false);
        double u;
        if (hasBeans && waterReady) u = 10.0;
        else if (hasBeans) u = 6.0;
        else u = 3.0;
        log.debug("Dynamic utility for BrewCoffee: u={}, hasBeans={}, waterReady={}", u, hasBeans, waterReady);
        return u;
    }

    @Action(
        name = "ServeCoffee",
        description = "Pour the brewed coffee into a cup and serve",
        preconditions = @Fact(name = "coffeeBrewed", value = "true"),
        effects = @Fact(name = "coffeeServed", value = "true"),
        utility = 5
    )
    public void serveCoffee() {
        log.info("Serving coffee!");
    }

    @Goal(
        name = "MakeCoffee",
        description = "Brew and serve a fresh cup of coffee",
        condition = @Fact(name = "coffeeServed", value = "true")
    )
    public void coffeeReady() {
    }
}
