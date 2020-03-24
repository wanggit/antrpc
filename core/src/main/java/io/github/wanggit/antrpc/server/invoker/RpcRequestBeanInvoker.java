package io.github.wanggit.antrpc.server.invoker;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.bean.error.RpcErrorCreator;
import io.github.wanggit.antrpc.server.invoker.exception.ClassNotLoadException;
import io.github.wanggit.antrpc.server.invoker.exception.MethodNotFoundException;
import io.github.wanggit.antrpc.server.utils.CacheClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcRequestBeanInvoker implements IRpcRequestBeanInvoker {

    private final BeanFactory springBeanFactory;

    private final Map<String, List<IRpcRequestBeanInvokeListener>> listeners = new HashMap<>();

    private final ThreadPoolExecutor listenerThreadPoolExecutor =
            new ThreadPoolExecutor(
                    2,
                    4,
                    2,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(10),
                    new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            if (log.isWarnEnabled()) {
                                log.warn(
                                        "The log queue is full and some listen will be discarded.");
                            }
                        }
                    });

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
        String fullName = getFullName(className, methodName, argumentTypes);
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
            Object[] realArgumentValues = new Object[argumentValues.length];
            for (int i = 0; i < argumentValues.length; i++) {
                Object argumentValue = argumentValues[i];
                if (!argTypes[i].isInstance(argumentValue) && argumentValue instanceof JSONObject) {
                    argumentValue = ((JSONObject) argumentValue).toJavaObject(argTypes[i]);
                }
                realArgumentValues[i] = argumentValue;
            }
            Object result = ReflectionUtils.invokeMethod(method, bean, realArgumentValues);
            RpcResponseBean responseBean = response(requestBean, result);
            asyncFireListener(fullName, responseBean, argumentValues);
            return responseBean;
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred ", throwable);
            }
            RpcResponseBean responseBean =
                    response(
                            requestBean,
                            RpcErrorCreator.create(
                                    throwable.getClass().getName(), throwable.getMessage()));
            asyncFireListener(fullName, responseBean, argumentValues);
            return responseBean;
        }
    }

    private String getFullName(String className, String methodName, List<String> argumentTypes) {
        return className
                + "#"
                + methodName
                + "("
                + (null == argumentTypes ? "" : StringUtils.join(argumentTypes, ","))
                + ")";
    }

    private void asyncFireListener(String name, Object result, Object[] arguments) {
        listenerThreadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        List<IRpcRequestBeanInvokeListener> iRpcRequestBeanInvokeListeners =
                                listeners.get(name);
                        if (null != iRpcRequestBeanInvokeListeners) {
                            iRpcRequestBeanInvokeListeners.forEach(
                                    it -> {
                                        it.listen(result, arguments);
                                    });
                        }
                    }
                });
    }

    @Override
    public void addListener(
            String name, IRpcRequestBeanInvokeListener rpcRequestBeanInvokeListener) {
        if (!listeners.containsKey(name)) {
            listeners.put(name, new ArrayList<>());
        }
        listeners.get(name).add(rpcRequestBeanInvokeListener);
    }

    @Override
    public void removeListener(
            String name, IRpcRequestBeanInvokeListener rpcRequestBeanInvokeListener) {
        List<IRpcRequestBeanInvokeListener> iRpcRequestBeanInvokeListeners = listeners.get(name);
        if (null != iRpcRequestBeanInvokeListeners) {
            iRpcRequestBeanInvokeListeners.remove(rpcRequestBeanInvokeListener);
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
