package io.github.wanggit.antrpc.client.spring;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.RpcClient;
import io.github.wanggit.antrpc.client.connections.ConnectionNotActiveException;
import io.github.wanggit.antrpc.client.future.ReadClientFuture;
import io.github.wanggit.antrpc.client.zk.exception.InterfaceProviderNotFoundException;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.bean.*;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.kryo.KryoSerializer;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Slf4j
public class CglibMethodInterceptor implements MethodInterceptor {

    private IAntrpcContext antrpcContext;

    CglibMethodInterceptor(IAntrpcContext antrpcContext) {
        this.antrpcContext = antrpcContext;
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
        RegisterBean.RegisterBeanMethod registerBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(method);
        // 频控限制，超过频控限制就返回null.
        if (!antrpcContext.getRateLimiting().allowAccess(registerBeanMethod)) {
            String errorMessage =
                    registerBeanMethod.toString()
                            + " interface calls reach frequency control limit, "
                            + registerBeanMethod.getLimit()
                            + " times within "
                            + registerBeanMethod.getDurationInSeconds()
                            + " minutes at most.";
            if (log.isErrorEnabled()) {
                log.error(errorMessage);
            }
            RpcCallLog rpcCallLog = initRpcCallLog(registerBeanMethod, className);
            rpcCallLog.setErrorMessage(errorMessage);
            antrpcContext.getRpcCallLogHolder().log(rpcCallLog);
            return antrpcContext.getOnFailHolder().doOnFail(anInterface, method, args);
        }
        RpcRequestBean rpcRequestBean = createRpcRequestBean(className, registerBeanMethod, args);
        RpcProtocol rpcProtocol = createRpcProtocol(rpcRequestBean);
        // 调用日志 RpcCallLog
        NodeHostEntity hostEntity = chooseNodeHostEntity(className, registerBeanMethod);

        ICircuitBreaker circuitBreaker = antrpcContext.getCircuitBreaker();
        // 熔断器判断
        RpcCallLog rpcCallLog = initRpcCallLog(registerBeanMethod, className);
        rpcCallLog.setIp(hostEntity.getIp());
        rpcCallLog.setPort(hostEntity.getPort());
        rpcCallLog.setRequestId(rpcRequestBean.getId());
        if (circuitBreaker.checkState(rpcCallLog.getCallLogKey(), rpcCallLog.getClassName())) {
            ReadClientFuture future = null;
            try {
                future =
                        internalSend(
                                rpcProtocol,
                                hostEntity,
                                circuitBreaker,
                                registerBeanMethod,
                                className,
                                rpcRequestBean);
            } catch (ConnectionNotActiveException cnae) {
                // 默认重试一次
                hostEntity = chooseNodeHostEntity(className, registerBeanMethod);
                future =
                        internalSend(
                                rpcProtocol,
                                hostEntity,
                                circuitBreaker,
                                registerBeanMethod,
                                className,
                                rpcRequestBean);
            }
            RpcResponseBean rpcResponseBean =
                    getRpcResponseBean(
                            circuitBreaker,
                            future,
                            registerBeanMethod,
                            className,
                            hostEntity,
                            rpcRequestBean);
            Object result = rpcResponseBean.getResult();
            rpcCallLog.setEnd(System.currentTimeMillis());
            rpcCallLog.setRt(rpcCallLog.getEnd() - rpcCallLog.getStart());
            rpcCallLog.setRequestArgs(rpcRequestBean.getArgumentValues());
            antrpcContext.getRpcCallLogHolder().log(rpcCallLog);
            return result;
        } else {
            // 熔断器被打开，记录日志，返回null.
            CircuitBreakerConfig breaker =
                    antrpcContext
                            .getCircuitBreaker()
                            .getInterfaceCircuitBreaker(rpcCallLog.getClassName());
            String message = null;
            if (null == breaker) {
                message =
                        rpcCallLog.getClassName()
                                + " does not have a circuit breaker configured. "
                                + "but it triggered. Please check the status of the circuit breaker container.";
            } else {
                message = JSONObject.toJSONString(breaker);
            }
            String errorMessage =
                    rpcCallLog.getCallLogKey() + " circuit breaker is open!!! " + message;
            if (log.isErrorEnabled()) {
                log.error(errorMessage);
            }
            RpcCallLog newRpcCallLog = initRpcCallLog(registerBeanMethod, className);
            newRpcCallLog.setIp(hostEntity.getIp());
            newRpcCallLog.setPort(hostEntity.getPort());
            newRpcCallLog.setErrorMessage(errorMessage);
            newRpcCallLog.setRequestId(rpcRequestBean.getId());
            newRpcCallLog.setRequestArgs(rpcRequestBean.getArgumentValues());
            antrpcContext.getRpcCallLogHolder().log(newRpcCallLog);
            return antrpcContext.getOnFailHolder().doOnFail(anInterface, method, args);
        }
    }

    private RpcResponseBean getRpcResponseBean(
            ICircuitBreaker circuitBreaker,
            ReadClientFuture future,
            RegisterBean.RegisterBeanMethod registerBeanMethod,
            String className,
            NodeHostEntity nodeHostEntity,
            RpcRequestBean rpcRequestBean) {
        RpcResponseBean rpcResponseBean = future.get();
        if (null == rpcResponseBean) {
            RpcCallLog rpcCallLog = initRpcCallLog(registerBeanMethod, className);
            rpcCallLog.setIp(nodeHostEntity.getIp());
            rpcCallLog.setPort(nodeHostEntity.getPort());
            rpcCallLog.setRequestId(rpcRequestBean.getId());
            circuitBreaker.increament(rpcCallLog.getCallLogKey());
            String errorMessage =
                    "The response result is null, either the service provider is unavailable, "
                            + "or the service provider responds to timeout. "
                            + rpcCallLog.getCallLogKey();
            rpcCallLog.setErrorMessage(errorMessage);
            rpcCallLog.setRequestArgs(rpcRequestBean.getArgumentValues());
            antrpcContext.getRpcCallLogHolder().log(rpcCallLog);
            throw new NullPointerException(errorMessage);
        }
        return rpcResponseBean;
    }

    private ReadClientFuture internalSend(
            RpcProtocol rpcProtocol,
            NodeHostEntity hostEntity,
            ICircuitBreaker circuitBreaker,
            RegisterBean.RegisterBeanMethod registerBeanMethod,
            String className,
            RpcRequestBean rpcRequestBean) {
        RpcClient rpcClient = antrpcContext.getRpcClients().getRpcClient(hostEntity);
        ReadClientFuture future = null;
        try {
            future = rpcClient.send(rpcProtocol);
        } catch (ConnectionNotActiveException cnae) {
            RpcCallLog rpcCallLog = initRpcCallLog(registerBeanMethod, className);
            rpcCallLog.setPort(hostEntity.getPort());
            rpcCallLog.setIp(hostEntity.getIp());
            rpcCallLog.setRequestId(rpcRequestBean.getId());
            circuitBreaker.increament(rpcCallLog.getCallLogKey());
            String errorMessage =
                    "Connection not alive. more info is " + rpcCallLog.getCallLogKey();
            rpcCallLog.setErrorMessage(errorMessage);
            rpcCallLog.setRequestArgs(rpcRequestBean.getArgumentValues());
            antrpcContext.getRpcCallLogHolder().log(rpcCallLog);
            throw new ConnectionNotActiveException(errorMessage, cnae);
        }
        return future;
    }

    private NodeHostEntity chooseNodeHostEntity(
            String className, RegisterBean.RegisterBeanMethod registerBeanMethod) {
        NodeHostEntity hostEntity;
        RpcCallLog rpcCallLog = initRpcCallLog(registerBeanMethod, className);
        try {
            hostEntity = antrpcContext.getNodeHostContainer().choose(className);
        } catch (InterfaceProviderNotFoundException e) {
            rpcCallLog.setErrorMessage(e.getMessage());
            antrpcContext.getRpcCallLogHolder().log(rpcCallLog);
            throw e;
        }
        return hostEntity;
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
        rpcProtocol.setData(KryoSerializer.getInstance().serialize(requestBean));
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
