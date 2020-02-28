package io.github.wanggit.antrpc.client.monitor;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;

public interface IRpcCallLogHolder {

    void log(RpcCallLog rpcCallLog);

    /**
     * @param name io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String)
     * @param rpcCallLogListener listener
     */
    void addListener(String name, IRpcCallLogListener rpcCallLogListener);

    /**
     * @param name io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String)
     * @param rpcCallLogListener listener
     */
    void removeListener(String name, IRpcCallLogListener rpcCallLogListener);
}
