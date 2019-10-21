package antrpc.client.zk.register;

import antrpc.commons.annotations.RpcMethod;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterBeanHelper {

    private static final ConcurrentHashMap<String, RegisterBean.RegisterBeanMethod> cache =
            new ConcurrentHashMap<>();

    public static RegisterBean.RegisterBeanMethod getRegisterBeanMethod(Method method) {
        return internalGetRegisterBeanMethod(method);
    }

    private static RegisterBean.RegisterBeanMethod internalGetRegisterBeanMethod(Method method) {
        String methodStr = method.toGenericString();
        if (!cache.containsKey(methodStr)) {
            synchronized (methodStr.intern()) {
                if (!cache.containsKey(methodStr)) {
                    RegisterBean.RegisterBeanMethod registerBeanMethod =
                            methodToRegisterBeanMethod(method);
                    cache.put(methodStr, registerBeanMethod);
                }
            }
        }
        return cache.get(methodStr);
    }

    private static RegisterBean.RegisterBeanMethod methodToRegisterBeanMethod(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<String> parameterTypeNames = new ArrayList<>(parameterTypes.length * 2);
        for (Class<?> parameterType : parameterTypes) {
            String parameterTypeName = parameterType.getName();
            parameterTypeNames.add(parameterTypeName);
        }
        RegisterBean.RegisterBeanMethod registerBeanMethod = new RegisterBean.RegisterBeanMethod();
        registerBeanMethod.setMethodName(methodName);
        registerBeanMethod.setParameterTypeNames(parameterTypeNames);
        RpcMethod rpcMethod = AnnotationUtils.findAnnotation(method, RpcMethod.class);
        if (null != rpcMethod && rpcMethod.rateLimitEnable()) {
            registerBeanMethod.setLimit(rpcMethod.limit());
            registerBeanMethod.setDurationInSeconds(rpcMethod.durationInSeconds());
        }
        return registerBeanMethod;
    }
}
