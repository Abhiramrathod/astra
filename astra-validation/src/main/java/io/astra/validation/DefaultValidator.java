package io.astra.validation;

import io.astra.api.WorldState;
import io.astra.api.validation.Validator;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link Validator} using a list of {@link ValidationRule}s.
 */
public class DefaultValidator implements Validator {
    private final List<ValidationRule> rules = new ArrayList<>();

    public DefaultValidator() {
        rules.add(new NotNullRule());
        rules.add(new NotEmptyRule());
    }

    public void addRule(ValidationRule rule) {
        rules.add(rule);
    }

    @Override
    public List<String> validate(WorldState state, String actionName) {
        List<String> errors = new ArrayList<>();
        for (ValidationRule rule : rules) {
            errors.addAll(rule.validate(state, actionName));
        }
        return errors;
    }

    public interface ValidationRule {
        List<String> validate(WorldState state, String actionName);
    }

    private static class NotNullRule implements ValidationRule {
        @Override
        public List<String> validate(WorldState state, String actionName) {
            List<String> errs = new ArrayList<>();
            for (var entry : state.asMap().entrySet()) {
                if (entry.getValue() == null) {
                    errs.add(actionName + ": fact '" + entry.getKey() + "' is null");
                }
            }
            return errs;
        }
    }

    private static class NotEmptyRule implements ValidationRule {
        @Override
        public List<String> validate(WorldState state, String actionName) {
            List<String> errs = new ArrayList<>();
            for (var entry : state.asMap().entrySet()) {
                if (entry.getValue() != null && entry.getValue().trim().isEmpty()) {
                    errs.add(actionName + ": fact '" + entry.getKey() + "' is empty");
                }
            }
            return errs;
        }
    }
}
