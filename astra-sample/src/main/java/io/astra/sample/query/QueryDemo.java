package io.astra.sample.query;

import io.astra.api.*;
import io.astra.core.DefaultAstra;
import java.util.Map;

/** Demonstrates auto-routing of natural-language queries to matching goals. */
public class QueryDemo {
    public static void run() {
        System.out.println("\n=== Query: Auto-Routing ===");
        AgentBase agent = new AgentBase() {{
            addAction("SearchWeather", () -> System.out.println("  Searching weather..."),
                Map.of(), Map.of("weather", "fetched"));
            addAction("CalculateSum", () -> System.out.println("  Calculating sum..."),
                Map.of(), Map.of("sum", "done"));
            addGoal("WeatherGoal", "Get weather information", Map.of("weather", "fetched"));
            addGoal("MathGoal", "Perform calculations", Map.of("sum", "done"));
        }};
        Astra astra = DefaultAstra.simple(agent);
        astra.executeQuery("what is the weather today");
        System.out.println("  Query demo complete");
    }
}
