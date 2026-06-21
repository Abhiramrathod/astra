package io.astra.sample.agentbus;

import io.astra.api.*;
import io.astra.api.agentbus.*;
import io.astra.agentbus.DefaultAgentBus;
import io.astra.event.DefaultEventBus;

/** Demonstrates {@link AgentBus} for inter-agent messaging. */
public class AgentBusDemo {
    public static void run() {
        System.out.println("\n=== AgentBus: Inter-Agent Messaging ===");
        DefaultAgentBus bus = new DefaultAgentBus(new DefaultEventBus());
        bus.subscribe("listener", msg -> System.out.println("  Subscriber received: " + msg.getPayload()));
        bus.broadcast("demo", "topic.hello", "Hello from AgentBus!");
        System.out.println("  AgentBus demo complete");
    }
}
