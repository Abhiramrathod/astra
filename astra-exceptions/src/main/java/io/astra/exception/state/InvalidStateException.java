package io.astra.exception.state;

import io.astra.exception.AstraException;

public class InvalidStateException extends AstraException {
    public InvalidStateException(String message) {
        super("INVALID_STATE", message);
    }
}
