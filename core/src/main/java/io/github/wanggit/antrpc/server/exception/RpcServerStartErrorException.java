package io.github.wanggit.antrpc.server.exception;

public class RpcServerStartErrorException extends RuntimeException {
    private static final long serialVersionUID = -101727162035310743L;

    public RpcServerStartErrorException(String message, Throwable e) {
        super(message, e);
    }
}
