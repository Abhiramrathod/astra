package io.astra.sample.policy;

import io.astra.api.*;
import io.astra.core.DefaultAstra;
import io.astra.core.policy.DefaultPolicyChecker;

/** Demonstrates {@link io.astra.api.policy.PolicyChecker} for access control on actions. */
public class PolicyDemo {
    public static void run() {
        System.out.println("\n=== Policy: PolicyChecker ===");
        DefaultPolicyChecker checker = new DefaultPolicyChecker();
        checker.grant("PublicAction", "default");

        AgentBase agent = new AgentBase() {{
            addAction("PublicAction", () -> System.out.println("  Public action executed"));
            addAction("SecretAction", () -> System.out.println("  Secret action executed"));
        }};
        Astra astra = DefaultAstra.builder().register(agent).withPolicyChecker(checker).build();
        WorldState state = WorldStates.empty();
        System.out.println("  PublicAction allowed: " + checker.check("PublicAction", "default", state));
        System.out.println("  SecretAction allowed: " + checker.check("SecretAction", "default", state));
        astra.execute("_run_all", state);
        System.out.println("  Policy demo complete");
    }
}
