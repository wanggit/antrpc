package antrpc.server.exception;

public class NoBeansException extends Exception {
    private static final long serialVersionUID = -5862303195638912696L;

    public NoBeansException(String msg) {
        super(msg);
    }

    public NoBeansException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
