package io.github.wanggit.antrpc.client.spring.exception;

public class ServiceProviderOccurredException extends RuntimeException {

    private static final long serialVersionUID = 7912222619968557350L;

    public ServiceProviderOccurredException(String message) {
        super(message);
    }

    public ServiceProviderOccurredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceProviderOccurredException(Throwable cause) {
        super(cause);
    }
}
