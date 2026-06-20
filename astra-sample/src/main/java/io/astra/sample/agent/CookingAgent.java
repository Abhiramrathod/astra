package io.astra.sample.agent;

import io.astra.annotation.Agent;
import io.astra.annotation.action.Action;
import io.astra.annotation.fact.Fact;
import io.astra.annotation.htn.CompoundTask;
import io.astra.annotation.htn.Decomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent
public class CookingAgent {
    private static final Logger log = LoggerFactory.getLogger(CookingAgent.class);

    @CompoundTask(name = "MakeDinner", description = "Prepare and serve a full dinner meal")
    @Decomposition(name = "pasta", description = "Cook a pasta dish from scratch",
        preconditions = @Fact(name = "hasIngredients", value = "true"),
        subtasks = {"ChopVegetables", "BoilWater", "CookPasta", "ServeMeal"})
    @Decomposition(name = "quick", description = "Heat a pre-made frozen meal",
        preconditions = @Fact(name = "hasMicrowaveMeal", value = "true"),
        subtasks = {"MicrowaveMeal", "ServeMeal"})
    public void makeDinner() {}

    @Action(
        name = "ChopVegetables",
        description = "Chop fresh vegetables for the pasta dish",
        preconditions = @Fact(name = "hasKnife", value = "true"),
        effects = @Fact(name = "veggiesChopped", value = "true")
    )
    public void ChopVegetables() {
        log.info("Chopping vegetables...");
    }

    @Action(
        name = "BoilWater",
        description = "Bring a pot of water to a rolling boil",
        preconditions = @Fact(name = "hasPot", value = "true"),
        effects = @Fact(name = "waterBoiled", value = "true")
    )
    public void BoilWater() {
        log.info("Boiling water...");
    }

    @Action(
        name = "CookPasta",
        description = "Cook pasta in boiling water with vegetables",
        preconditions = {
            @Fact(name = "veggiesChopped", value = "true"),
            @Fact(name = "waterBoiled", value = "true")
        },
        effects = @Fact(name = "pastaCooked", value = "true")
    )
    public void CookPasta() {
        log.info("Cooking pasta...");
    }

    @Action(
        name = "ServeMeal",
        description = "Plate and serve the finished meal",
        preconditions = @Fact(name = "pastaCooked", value = "true"),
        effects = @Fact(name = "mealServed", value = "true")
    )
    public void ServeMeal() {
        log.info("Serving the meal!");
    }

    @Action(
        name = "MicrowaveMeal",
        description = "Heat a pre-made frozen meal in the microwave",
        preconditions = @Fact(name = "hasMicrowaveMeal", value = "true"),
        effects = @Fact(name = "mealServed", value = "true")
    )
    public void MicrowaveMeal() {
        log.info("Beeping microwave... meal ready!");
    }
}
