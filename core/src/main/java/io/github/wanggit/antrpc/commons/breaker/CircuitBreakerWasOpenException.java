package io.github.wanggit.antrpc.commons.breaker;

public class CircuitBreakerWasOpenException extends RuntimeException {

    private static final long serialVersionUID = -2104757817206842611L;

    public CircuitBreakerWasOpenException(String message) {
        super(message);
    }

    public CircuitBreakerWasOpenException(String message, Throwable cause) {
        super(message, cause);
    }

    public CircuitBreakerWasOpenException(Throwable cause) {
        super(cause);
    }
}
