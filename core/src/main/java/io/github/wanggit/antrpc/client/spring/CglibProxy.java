package io.github.wanggit.antrpc.client.spring;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class CglibProxy {

    private CglibProxy() {}

    private static final CglibProxy instance = new CglibProxy();

    public static CglibProxy getInstance() {
        return instance;
    }

    private Enhancer enhancer = new Enhancer();

    public Object getProxy(Class interfaceClazz, MethodInterceptor methodInterceptor) {
        enhancer.setInterfaces(new Class[] {interfaceClazz});
        enhancer.setCallback(methodInterceptor);
        return enhancer.create();
    }
}
