package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.rate.IRateLimiting;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;

import java.util.concurrent.ConcurrentHashMap;

public class RpcBeanContainer implements BeanContainer {

    private final ConcurrentHashMap<String, Object> beans = new ConcurrentHashMap<>();

    private final CglibMethodInterceptor cglibMethodInterceptor;

    public RpcBeanContainer(
            IRateLimiting rateLimiting,
            IRpcCallLogHolder rpcCallLogHolder,
            IOnFailHolder onFailHolder,
            ICircuitBreaker circuitBreaker,
            IRpcClients rpcClients,
            ISerializerHolder serializerHolder,
            INodeHostContainer nodeHostContainer) {
        this.cglibMethodInterceptor =
                new CglibMethodInterceptor(
                        rateLimiting,
                        rpcCallLogHolder,
                        onFailHolder,
                        circuitBreaker,
                        rpcClients,
                        serializerHolder,
                        nodeHostContainer);
    }

    @Override
    public Object getOrCreateBean(Class clazz) {
        if (null == clazz) {
            throw new IllegalArgumentException("clazz cannot be null.");
        }
        if (null == this.cglibMethodInterceptor) {
            throw new IllegalStateException("BeanContainer has not been initialized.");
        }
        String className = clazz.getName();
        if (!beans.containsKey(className)) {
            synchronized (className.intern()) {
                if (!beans.containsKey(className)) {
                    Object proxy = CglibProxy.createProxy(clazz, this.cglibMethodInterceptor);
                    beans.put(className, proxy);
                }
            }
        }
        return beans.get(className);
    }
}
