package io.astra.exception;

/** Base exception for all Astra framework errors. */
public class AstraException extends RuntimeException {
    private final String errorCode;

    public AstraException(String message) {
        super(message);
        this.errorCode = "ASTRA_ERR";
    }

    public AstraException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AstraException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ASTRA_ERR";
    }

    public AstraException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
