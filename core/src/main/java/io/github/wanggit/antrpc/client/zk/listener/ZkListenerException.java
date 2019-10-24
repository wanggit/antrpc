package io.github.wanggit.antrpc.client.zk.listener;

import org.springframework.beans.BeansException;

public class ZkListenerException extends BeansException {
    private static final long serialVersionUID = -203988152374926256L;

    public ZkListenerException(String msg) {
        super(msg);
    }

    public ZkListenerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
