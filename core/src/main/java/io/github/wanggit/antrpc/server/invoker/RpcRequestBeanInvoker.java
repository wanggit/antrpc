package io.github.wanggit.antrpc.server.invoker;

import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.bean.error.RpcError;
import io.github.wanggit.antrpc.commons.bean.error.RpcErrorCreator;
import io.github.wanggit.antrpc.server.utils.CacheClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
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
        Class clazz = internalGetClass(className);
        if (null == clazz) {
            return response(requestBean, RpcErrorCreator.create(RpcError.CLASS_NOT_FOUND));
        }
        Class[] argTypes = new Class[argumentTypes.size()];
        for (int i = 0; i < argumentTypes.size(); i++) {
            String argTypeStr = argumentTypes.get(i);
            Class argType = internalGetClass(argTypeStr);
            if (null == argType) {
                return response(
                        requestBean, RpcErrorCreator.create(RpcError.ARGUMENT_CLASS_NOT_FOUND));
            }
            argTypes[i] = argType;
        }

        Object bean = null;
        try {
            bean = springBeanFactory.getBean(clazz);
        } catch (NoUniqueBeanDefinitionException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return response(requestBean, RpcErrorCreator.create(RpcError.MANY_BEANS));
        } catch (NoSuchBeanDefinitionException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return response(requestBean, RpcErrorCreator.create(RpcError.NO_BEANS));
        }
        if (null == bean) {
            return response(requestBean, RpcErrorCreator.create(RpcError.NO_BEANS));
        }
        Method method = ReflectionUtils.findMethod(bean.getClass(), methodName, argTypes);
        if (null == method) {
            if (log.isErrorEnabled()) {
                log.error("No " + methodName + " method is found in class " + className);
            }
            return response(requestBean, RpcErrorCreator.create(RpcError.METHOD_NOT_FOUND));
        }
        Object result = ReflectionUtils.invokeMethod(method, bean, argumentValues);
        return response(requestBean, result);
    }

    private Class internalGetClass(String className) {
        Class clazz = null;
        try {
            clazz = CacheClassUtil.getInstance().getCacheClass(className);
        } catch (ClassNotFoundException e) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred when " + className + " was parsed to Class.", e);
            }
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
