package io.github.wanggit.antrpc.server.invoker;

import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;

public interface IRpcRequestBeanInvoker {
    RpcResponseBean invoke(RpcRequestBean requestBean);

    /**
     * @param name io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String)
     * @param rpcRequestBeanInvokeListener listener
     */
    void addListener(String name, IRpcRequestBeanInvokeListener rpcRequestBeanInvokeListener);

    /**
     * @param name io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String)
     * @param rpcRequestBeanInvokeListener listener
     */
    void removeListener(String name, IRpcRequestBeanInvokeListener rpcRequestBeanInvokeListener);
}
