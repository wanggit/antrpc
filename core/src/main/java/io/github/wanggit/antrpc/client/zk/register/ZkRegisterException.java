package io.github.wanggit.antrpc.client.zk.register;

import org.springframework.beans.BeansException;

public class ZkRegisterException extends BeansException {
    private static final long serialVersionUID = -8883886703318947372L;

    public ZkRegisterException(String msg) {
        super(msg);
    }

    public ZkRegisterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
