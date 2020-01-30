package io.github.wanggit.antrpc.client.connections;

public class ConnectionNotBorrowedException extends RuntimeException {
    private static final long serialVersionUID = 1190060195111477559L;

    public ConnectionNotBorrowedException() {}

    public ConnectionNotBorrowedException(String message) {
        super(message);
    }

    public ConnectionNotBorrowedException(String message, Throwable cause) {
        super(message, cause);
    }

    ConnectionNotBorrowedException(Throwable cause) {
        super(cause);
    }
}
