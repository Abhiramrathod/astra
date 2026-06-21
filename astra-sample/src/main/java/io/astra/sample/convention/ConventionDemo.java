package io.astra.sample.convention;

import io.astra.api.*;
import io.astra.core.DefaultAstra;

/**
 * Demonstrates the simplified DX: {@link AgentBase}, convention actions
 * (public void no-arg methods auto-detected), and {@link DefaultAstra#simple(Object)}.
 */
public class ConventionDemo {
    public static void run() {
        System.out.println("\n=== Convention: AgentBase + Astra.simple() ===");
        AgentBase agent = new AgentBase() {
            public void greet() { System.out.println("  Hello from convention action!"); }
            public void farewell() { System.out.println("  Goodbye!"); }
        };
        Astra astra = DefaultAstra.simple(agent);
        astra.execute("_run_all", WorldStates.empty());
        System.out.println("  Convention demo complete — 2 actions executed");
    }
}
