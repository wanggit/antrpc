package io.github.wanggit.antrpc.server.invoker;

public interface IRpcRequestBeanInvokeListener {

    void listen(Object result, Object[] argumentValues);
}
