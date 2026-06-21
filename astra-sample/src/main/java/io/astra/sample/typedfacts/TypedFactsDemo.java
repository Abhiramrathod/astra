package io.astra.sample.typedfacts;

import io.astra.api.*;
import io.astra.api.fact.Fact;
import java.util.Optional;

/** Demonstrates typed facts ({@link Fact}<T>) and typed getters/setters on {@link WorldState}. */
public class TypedFactsDemo {
    public static void run() {
        System.out.println("\n=== TypedFacts: Fact<T> and Typed WorldState ===");
        Fact<String> name = new Fact<>("name", "Astra", String.class);
        Fact<Integer> version = Fact.deserialize("version", "21", Integer.class);
        System.out.println("  Fact<String>: " + name.getValue() + " (" + name.getType().getSimpleName() + ")");
        System.out.println("  Fact<Integer> deserialized: " + version.getValue());

        WorldState ws = WorldStates.empty().setTyped("count", 42, Integer.class);
        Optional<Integer> count = ws.getTyped("count", Integer.class);
        System.out.println("  Typed get: " + count.orElse(-1) + " (type=" + count.map(Object::getClass).map(Class::getSimpleName).orElse("N/A") + ")");
    }
}
