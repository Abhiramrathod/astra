package io.astra.sample;

import io.astra.sample.basic.BasicDemo;
import io.astra.sample.convention.ConventionDemo;
import io.astra.sample.typedfacts.TypedFactsDemo;
import io.astra.sample.validation.ValidationDemo;
import io.astra.sample.policy.PolicyDemo;
import io.astra.sample.store.StoreDemo;
import io.astra.sample.cache.CacheDemo;
import io.astra.sample.agentbus.AgentBusDemo;
import io.astra.sample.skill.SkillDemo;
import io.astra.sample.composite.CompositeDemo;
import io.astra.sample.rollback.RollbackDemo;
import io.astra.sample.query.QueryDemo;
import io.astra.sample.structural.StructuralDemo;

/** Entry point that runs all feature demos. */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Astra Feature Demos ===\n");
        long start = System.currentTimeMillis();

        BasicDemo.run();
        ConventionDemo.run();
        TypedFactsDemo.run();
        StoreDemo.run();
        CacheDemo.run();
        PolicyDemo.run();
        ValidationDemo.run();
        AgentBusDemo.run();
        SkillDemo.run();
        CompositeDemo.run();
        RollbackDemo.run();
        QueryDemo.run();
        StructuralDemo.run();

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("\n=== All demos completed in " + elapsed + "ms ===");
    }
}
