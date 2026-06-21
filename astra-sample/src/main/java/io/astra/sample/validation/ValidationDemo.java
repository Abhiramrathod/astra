package io.astra.sample.validation;

import io.astra.api.*;
import io.astra.core.DefaultAstra;
import io.astra.validation.DefaultValidator;
import io.astra.validation.DefaultValidator.ValidationRule;
import java.util.*;

/** Demonstrates {@link io.astra.api.validation.Validator} with custom validation rules. */
public class ValidationDemo {
    public static void run() {
        System.out.println("\n=== Validation: Validator + ValidationInterceptor ===");
        DefaultValidator validator = new DefaultValidator();
        validator.addRule((state, actionName) -> {
            if ("Greet".equals(actionName) && state.get("name").map(String::isBlank).orElse(true)) {
                return List.of("name must not be blank");
            }
            return List.of();
        });
        AgentBase agent = new AgentBase() {{
            addAction("Greet", () -> System.out.println("  Hello!"),
                Map.of(), Map.of("greeted", "true"));
        }};
        Astra astra = DefaultAstra.builder().register(agent).withValidator(validator).build();
        WorldState valid = WorldStates.of("name", "Astra");
        WorldState invalid = WorldStates.of("name", "");
        System.out.println("  Valid state errors: " + validator.validate(valid, "Greet"));
        System.out.println("  Invalid state errors: " + validator.validate(invalid, "Greet"));
        astra.execute("_run_all", valid);
        System.out.println("  Validation demo complete");
    }
}
