package io.astra.validation;

import io.astra.api.ActionInfo;
import io.astra.api.WorldState;
import io.astra.api.interceptor.ActionInterceptor;
import io.astra.api.validation.Validator;
import io.astra.api.event.AstraEvent;
import io.astra.api.event.AstraEventType;
import io.astra.api.event.EventBus;
import java.util.List;

/**
 * Interceptor that validates world state before an action executes.
 */
public class ValidationInterceptor implements ActionInterceptor {
    private final Validator validator;
    private final EventBus eventBus;

    public ValidationInterceptor(Validator validator, EventBus eventBus) {
        this.validator = validator;
        this.eventBus = eventBus;
    }

    @Override
    public void beforeAction(ActionInfo action, WorldState state) {
        List<String> errors = validator.validate(state, action.getName());
        if (!errors.isEmpty()) {
            eventBus.publish(AstraEvent.of(AstraEventType.VALIDATION_FAILED,
                "action", action.getName(),
                "errors", String.join(", ", errors)));
            throw new IllegalArgumentException(
                "Validation failed for " + action.getName() + ": " + String.join(", ", errors));
        }
    }
}
