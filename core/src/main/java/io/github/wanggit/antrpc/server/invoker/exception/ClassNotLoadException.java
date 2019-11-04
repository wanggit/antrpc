package io.github.wanggit.antrpc.server.invoker.exception;

public class ClassNotLoadException extends RuntimeException {

    private static final long serialVersionUID = -2884531471432275152L;

    public ClassNotLoadException(String message) {
        super(message);
    }

    public ClassNotLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassNotLoadException(Throwable cause) {
        super(cause);
    }
}
