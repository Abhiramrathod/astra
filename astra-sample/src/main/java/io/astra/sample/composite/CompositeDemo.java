package io.astra.sample.composite;

import io.astra.api.*;
import io.astra.core.DefaultAstra;
import io.astra.core.composite.DefaultCompositeAgent;

/** Demonstrates {@link io.astra.api.composite.CompositeAgent} for routing queries across sub-agents. */
public class CompositeDemo {
    public static void run() {
        System.out.println("\n=== Composite: Multi-Agent Routing ===");
        DefaultCompositeAgent composite = new DefaultCompositeAgent(null, null, null);
        AgentBase search = new AgentBase() {{
            addAction("SearchAction", () -> System.out.println("  Search agent executing"));
        }};
        AgentBase math = new AgentBase() {{
            addAction("Calculate", () -> System.out.println("  Math agent executing"));
        }};
        composite.registerSubAgent("search", DefaultAstra.simple(search));
        composite.registerSubAgent("math", DefaultAstra.simple(math));
        System.out.println("  Sub-agents: " + composite.getSubAgentNames());
        System.out.println("  Composite demo complete");
    }
}
