package io.github.wanggit.antrpc.client.spring;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

class CglibProxy {

    static Object createProxy(Class interfaceClazz, MethodInterceptor methodInterceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[] {interfaceClazz});
        enhancer.setCallback(methodInterceptor);
        return enhancer.create();
    }
}
