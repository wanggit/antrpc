package io.github.wanggit.antrpc.client.spring.exception;

public class ResultWasNullException extends RuntimeException {
    private static final long serialVersionUID = -214665875632092594L;

    public ResultWasNullException(String message) {
        super(message);
    }

    public ResultWasNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultWasNullException(Throwable cause) {
        super(cause);
    }
}
