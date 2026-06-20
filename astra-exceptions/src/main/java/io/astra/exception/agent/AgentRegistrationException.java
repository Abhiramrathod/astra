package io.astra.exception.agent;

import io.astra.exception.AstraException;

public class AgentRegistrationException extends AstraException {
    public AgentRegistrationException(String message, Throwable cause) {
        super("AGENT_REG_ERR", message, cause);
    }
}
