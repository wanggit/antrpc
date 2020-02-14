package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.commons.annotations.LinkMonitor;
import io.github.wanggit.antrpc.commons.bean.SerialNumberThreadLocal;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class CallerProxyMethodInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy)
            throws Throwable {
        try {
            return methodProxy.invokeSuper(o, objects);
        } finally {
            LinkMonitor linkMonitor = AnnotationUtils.findAnnotation(method, LinkMonitor.class);
            if (null != linkMonitor) {
                SerialNumberThreadLocal.clean();
            }
        }
    }
}
