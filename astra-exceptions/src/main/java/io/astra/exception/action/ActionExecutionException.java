package io.astra.exception.action;

import io.astra.api.ActionInfo;
import io.astra.exception.AstraException;

public class ActionExecutionException extends AstraException {
    private final ActionInfo action;

    public ActionExecutionException(ActionInfo action, Throwable cause) {
        super("ACTION_FAILED", "Failed to execute action: " + action.getName(), cause);
        this.action = action;
    }

    public ActionInfo getAction() { return action; }
}
