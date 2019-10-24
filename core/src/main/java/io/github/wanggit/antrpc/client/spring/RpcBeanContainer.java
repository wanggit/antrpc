package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.IAntrpcContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RpcBeanContainer implements BeanContainer {

    private ConcurrentHashMap<String, Object> beans = new ConcurrentHashMap<>();

    private IAntrpcContext antrpcContext;

    private CglibMethodInterceptor cglibMethodInterceptor;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    @Override
    public void setAntrpcContext(IAntrpcContext antrpcContext) {
        if (atomicBoolean.compareAndSet(false, true)) {
            this.antrpcContext = antrpcContext;
            this.init();
        }
    }

    private void init() {
        this.cglibMethodInterceptor = new CglibMethodInterceptor(antrpcContext);
    }

    @Override
    public Object getOrCreateBean(Class clazz) {
        if (null == clazz) {
            throw new IllegalArgumentException("clazz cannot be null.");
        }
        if (null == antrpcContext) {
            throw new IllegalStateException("AntrpcContext has not been initialized.");
        }
        if (null == this.cglibMethodInterceptor) {
            throw new IllegalStateException("BeanContainer has not been initialized.");
        }
        String className = clazz.getName();
        if (!beans.containsKey(className)) {
            synchronized (className.intern()) {
                if (!beans.containsKey(className)) {
                    Object proxy =
                            CglibProxy.getInstance().getProxy(clazz, this.cglibMethodInterceptor);
                    beans.put(className, proxy);
                }
            }
        }
        return beans.get(className);
    }
}
