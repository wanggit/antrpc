package io.github.wanggit.antrpc.client.spring;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.client.RpcClient;
import io.github.wanggit.antrpc.client.future.ReadClientFuture;
import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.rate.IRateLimiting;
import io.github.wanggit.antrpc.client.rate.RateLimitingException;
import io.github.wanggit.antrpc.client.spring.exception.ResultWasNullException;
import io.github.wanggit.antrpc.client.spring.exception.ServiceProviderOccurredException;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.bean.*;
import io.github.wanggit.antrpc.commons.bean.error.RpcResponseError;
import io.github.wanggit.antrpc.commons.breaker.CircuitBreakerWasOpenException;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Slf4j
public class CglibMethodInterceptor implements MethodInterceptor {

    private final IRateLimiting rateLimiting;
    private final IRpcCallLogHolder rpcCallLogHolder;
    private final IOnFailHolder onFailHolder;
    private final ICircuitBreaker circuitBreaker;
    private final IRpcClients rpcClients;
    private final ISerializerHolder serializerHolder;
    private final INodeHostContainer nodeHostContainer;

    CglibMethodInterceptor(
            IRateLimiting rateLimiting,
            IRpcCallLogHolder rpcCallLogHolder,
            IOnFailHolder onFailHolder,
            ICircuitBreaker circuitBreaker,
            IRpcClients rpcClients,
            ISerializerHolder serializerHolder,
            INodeHostContainer nodeHostContainer) {
        this.rateLimiting = rateLimiting;
        this.rpcCallLogHolder = rpcCallLogHolder;
        this.onFailHolder = onFailHolder;
        this.circuitBreaker = circuitBreaker;
        this.rpcClients = rpcClients;
        this.serializerHolder = serializerHolder;
        this.nodeHostContainer = nodeHostContainer;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return proxy.invokeSuper(obj, args);
        }
        Class<?>[] interfaces = obj.getClass().getInterfaces();
        if (interfaces.length == 0) {
            String message =
                    "The "
                            + obj.getClass().getName()
                            + " object must implement at least one interface.";
            if (log.isErrorEnabled()) {
                log.error(message);
            }
            throw new IllegalStateException(message);
        }
        Class<?> anInterface = interfaces[0];
        String className = anInterface.getName();
        if (log.isInfoEnabled()) {
            log.info(className + "#" + method.getName());
        }
        NodeHostEntity hostEntity = null;
        RpcRequestBean rpcRequestBean = null;
        RpcCallLog rpcCallLog = null;
        try {
            RegisterBean.RegisterBeanMethod registerBeanMethod =
                    RegisterBeanHelper.getRegisterBeanMethod(method);
            rpcCallLog = initRpcCallLog(registerBeanMethod, className);
            if (!rateLimiting.allowAccess(registerBeanMethod)) {
                String errorMessage =
                        registerBeanMethod.toString()
                                + " interface calls reach frequency control limit, "
                                + registerBeanMethod.getLimit()
                                + " times within "
                                + registerBeanMethod.getDurationInSeconds()
                                + " minutes at most.";
                throw new RateLimitingException(errorMessage);
            }
            rpcRequestBean = createRpcRequestBean(className, registerBeanMethod, args);
            RpcProtocol rpcProtocol = createRpcProtocol(rpcRequestBean);
            // 调用日志 RpcCallLog
            hostEntity = chooseNodeHostEntity(className);
            String key = getCallLogKey(rpcRequestBean, hostEntity);
            if (circuitBreaker.checkState(className, key)) {
                return doInternalSendWhenCircuitBreakerClosed(
                        hostEntity, rpcRequestBean, rpcCallLog, rpcProtocol);
            } else {
                if (circuitBreaker.checkNearBy(key)) {
                    try {
                        Object result =
                                doInternalSendWhenCircuitBreakerClosed(
                                        hostEntity, rpcRequestBean, rpcCallLog, rpcProtocol);
                        circuitBreaker.close(key);
                        return result;
                    } catch (Exception e) {
                        circuitBreaker.open(key);
                        throw e;
                    }
                }

                CircuitBreakerConfig breaker = circuitBreaker.getInterfaceCircuitBreaker(className);
                String message = null;
                if (null == breaker) {
                    message =
                            className
                                    + " does not have a circuit breaker configured. "
                                    + "but it triggered. Please heartBeatWasContinuousLoss the status of the circuit breaker container.";
                } else {
                    message = JSONObject.toJSONString(breaker);
                }
                String errorMessage =
                        getCallLogKey(rpcRequestBean, hostEntity)
                                + " circuit breaker is open!!! "
                                + message;
                throw new CircuitBreakerWasOpenException(errorMessage);
            }
        } catch (Throwable throwable) {
            if (null != rpcCallLog) {
                if (null != hostEntity) {
                    rpcCallLog.setIp(hostEntity.getIp());
                    rpcCallLog.setPort(hostEntity.getPort());
                }
                rpcCallLog.setEnd(System.currentTimeMillis());
                rpcCallLog.setRt(rpcCallLog.getEnd() - rpcCallLog.getStart());
                rpcCallLog.setErrorMessage(throwable.getMessage());
                rpcCallLogHolder.log(rpcCallLog);
            }
            if (throwable instanceof RateLimitingException
                    || throwable instanceof CircuitBreakerWasOpenException) {
                return onFailHolder.doOnFail(anInterface, method, args);
            }
            if (throwable instanceof ServiceProviderOccurredException) {
                // 服务提供者发生了异常
                circuitBreaker.increament(getCallLogKey(rpcRequestBean, hostEntity));
                if (log.isErrorEnabled()) {
                    log.error(
                            "An exception occurred from the service provider. ["
                                    + throwable.getMessage()
                                    + "]",
                            throwable);
                }
            }
        }
        return null;
    }

    private Object doInternalSendWhenCircuitBreakerClosed(
            NodeHostEntity hostEntity,
            RpcRequestBean rpcRequestBean,
            RpcCallLog rpcCallLog,
            RpcProtocol rpcProtocol) {
        ReadClientFuture future = internalSend(rpcProtocol, hostEntity);
        RpcResponseBean rpcResponseBean =
                getRpcResponseBean(circuitBreaker, future, hostEntity, rpcRequestBean);
        if (null == rpcResponseBean) {
            throw new ResultWasNullException("result is null.");
        }
        Object result = rpcResponseBean.getResult();
        if (result instanceof RpcResponseError) {
            throw new ServiceProviderOccurredException(((RpcResponseError) result).getMessage());
        }
        rpcCallLog.setIp(hostEntity.getIp());
        rpcCallLog.setPort(hostEntity.getPort());
        rpcCallLog.setRequestId(rpcRequestBean.getId());
        rpcCallLog.setEnd(System.currentTimeMillis());
        rpcCallLog.setRt(rpcCallLog.getEnd() - rpcCallLog.getStart());
        rpcCallLogHolder.log(rpcCallLog);
        return rpcResponseBean.getResult();
    }

    private String getCallLogKey(RpcRequestBean rpcRequestBean, NodeHostEntity hostEntity) {
        return rpcRequestBean.getFullClassName()
                + "."
                + rpcRequestBean.getMethodName()
                + "@"
                + hostEntity.getIp()
                + ":"
                + hostEntity.getPort();
    }

    private RpcResponseBean getRpcResponseBean(
            ICircuitBreaker circuitBreaker,
            ReadClientFuture future,
            NodeHostEntity nodeHostEntity,
            RpcRequestBean rpcRequestBean) {
        RpcResponseBean rpcResponseBean = future.get();
        if (null == rpcResponseBean) {
            circuitBreaker.increament(getCallLogKey(rpcRequestBean, nodeHostEntity));
        }
        return rpcResponseBean;
    }

    private ReadClientFuture internalSend(RpcProtocol rpcProtocol, NodeHostEntity hostEntity) {
        RpcClient rpcClient = rpcClients.getRpcClient(hostEntity);
        return rpcClient.send(rpcProtocol);
    }

    private NodeHostEntity chooseNodeHostEntity(String className) {
        return nodeHostContainer.choose(className);
    }

    private RpcCallLog initRpcCallLog(
            RegisterBean.RegisterBeanMethod registerBeanMethod, String className) {
        RpcCallLog rpcCallLog = new RpcCallLog();
        rpcCallLog.setClassName(className);
        rpcCallLog.setMethodName(registerBeanMethod.toString());
        SerialNumberThreadLocal.TraceEntity traceEntity = SerialNumberThreadLocal.get();
        rpcCallLog.setSerialNumber(traceEntity.getSerialNumber());
        rpcCallLog.setCaller(traceEntity.getCaller());
        rpcCallLog.setThreadId(Thread.currentThread().getId());
        rpcCallLog.setStart(System.currentTimeMillis());
        return rpcCallLog;
    }

    private RpcProtocol createRpcProtocol(RpcRequestBean requestBean) {
        // 封装 RpcProtocol
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setType(ConstantValues.BIZ_TYPE);
        rpcProtocol.setCmdId(IdGenHelper.getInstance().getId());
        rpcProtocol.setData(serializerHolder.getSerializer().serialize(requestBean));
        return rpcProtocol;
    }

    private RpcRequestBean createRpcRequestBean(
            String className, RegisterBean.RegisterBeanMethod registerBeanMethod, Object[] args) {
        // 封装 RpcRequestBean
        RpcRequestBean requestBean = new RpcRequestBean();
        requestBean.setFullClassName(className);
        requestBean.setMethodName(registerBeanMethod.getMethodName());
        requestBean.setArgumentTypes(registerBeanMethod.getParameterTypeNames());
        requestBean.setTs(System.currentTimeMillis());
        requestBean.setArgumentValues(args);
        requestBean.setId(IdGenHelper.getInstance().getUUID());
        SerialNumberThreadLocal.TraceEntity traceEntity = SerialNumberThreadLocal.get();
        requestBean.setSerialNumber(traceEntity.getSerialNumber());
        requestBean.setCaller(traceEntity.getCaller());
        return requestBean;
    }
}
