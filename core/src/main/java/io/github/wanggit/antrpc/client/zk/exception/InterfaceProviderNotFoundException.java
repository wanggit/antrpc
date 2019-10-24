package io.github.wanggit.antrpc.client.zk.exception;

public class InterfaceProviderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -3389332450056292099L;

    public InterfaceProviderNotFoundException() {}

    public InterfaceProviderNotFoundException(String message) {
        super(message);
    }

    public InterfaceProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterfaceProviderNotFoundException(Throwable cause) {
        super(cause);
    }
}
