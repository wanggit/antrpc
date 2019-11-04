package io.github.wanggit.antrpc.server.invoker;

import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.bean.error.RpcErrorCreator;
import io.github.wanggit.antrpc.server.invoker.exception.ClassNotLoadException;
import io.github.wanggit.antrpc.server.invoker.exception.MethodNotFoundException;
import io.github.wanggit.antrpc.server.utils.CacheClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class RpcRequestBeanInvoker implements IRpcRequestBeanInvoker {

    private final BeanFactory springBeanFactory;

    public RpcRequestBeanInvoker(BeanFactory beanFactory) {
        this.springBeanFactory = beanFactory;
    }

    @Override
    public RpcResponseBean invoke(RpcRequestBean requestBean) {
        if (null == requestBean) {
            throw new IllegalArgumentException("requestBean cannot be null.");
        }
        String className = requestBean.getFullClassName();
        String methodName = requestBean.getMethodName();
        List<String> argumentTypes = requestBean.getArgumentTypes();
        Object[] argumentValues = requestBean.getArgumentValues();
        try {
            Class clazz = internalGetClass(className);
            Class[] argTypes = new Class[argumentTypes.size()];
            for (int i = 0; i < argumentTypes.size(); i++) {
                String argTypeStr = argumentTypes.get(i);
                argTypes[i] = internalGetClass(argTypeStr);
            }
            Object bean = springBeanFactory.getBean(clazz);
            Method method = ReflectionUtils.findMethod(bean.getClass(), methodName, argTypes);
            if (null == method) {
                throw new MethodNotFoundException(
                        "No " + methodName + " method is found in class " + className);
            }
            Object result = ReflectionUtils.invokeMethod(method, bean, argumentValues);
            return response(requestBean, result);
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred ", throwable);
            }
            return response(
                    requestBean,
                    RpcErrorCreator.create(throwable.getClass().getName(), throwable.getMessage()));
        }
    }

    private Class internalGetClass(String className) {
        Class clazz = null;
        try {
            clazz = CacheClassUtil.getInstance().getCacheClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassNotLoadException(
                    "An exception occurred when " + className + " was parsed to Class.", e);
        }
        return clazz;
    }

    private RpcResponseBean response(RpcRequestBean requestBean, Object result) {
        RpcResponseBean rpcResponseBean = new RpcResponseBean();
        rpcResponseBean.setId(requestBean.getId());
        rpcResponseBean.setReqTs(requestBean.getTs());
        rpcResponseBean.setTs(System.currentTimeMillis());
        rpcResponseBean.setResult(result);
        return rpcResponseBean;
    }
}
