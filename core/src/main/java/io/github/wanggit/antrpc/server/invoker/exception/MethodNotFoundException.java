package io.github.wanggit.antrpc.server.invoker.exception;

public class MethodNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 6700028223959554539L;

    public MethodNotFoundException(String message) {
        super(message);
    }

    public MethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotFoundException(Throwable cause) {
        super(cause);
    }
}
