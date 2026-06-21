package io.astra.sample.store;

import io.astra.api.*;
import io.astra.core.DefaultAstra;
import io.astra.store.InMemoryWorldStateStore;
import java.util.Map;

/** Demonstrates {@link io.astra.api.store.WorldStateStore} for persisting and reloading world state. */
public class StoreDemo {
    public static void run() {
        System.out.println("\n=== Store: WorldStateStore Persistence ===");
        InMemoryWorldStateStore store = new InMemoryWorldStateStore();
        AgentBase agent = new AgentBase() {{
            addAction("LogState", () -> System.out.println("  State captured"),
                Map.of(), Map.of("logged", "true"));
        }};
        DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().register(agent).withStore(store).build();

        WorldState ws = WorldStates.of("session", "demo1", "progress", "50");
        store.save("demo1", ws);
        System.out.println("  Saved state: " + ws);

        store.load("demo1").ifPresent(loaded -> {
            System.out.println("  Loaded state: " + loaded);
            System.out.println("  Match: " + ws.equals(loaded));
        });
    }
}
