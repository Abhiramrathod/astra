package io.astra.api.validation;

import io.astra.api.WorldState;
import java.util.List;

/**
 * Validates world state for a given action before execution.
 */
public interface Validator {
    List<String> validate(WorldState state, String actionName);
}
