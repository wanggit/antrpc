package io.github.wanggit.antrpc.client.rate;

import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiting implements IRateLimiting {

    private final ConcurrentHashMap<String, EventCountCircuitBreaker> cache =
            new ConcurrentHashMap<>();

    @Override
    public boolean allowAccess(
            RegisterBean.RegisterBeanMethod registerBeanMethod, NodeHostEntity hostEntity) {
        if (null == registerBeanMethod || null == hostEntity) {
            throw new IllegalArgumentException("Argument cannot be null.");
        }
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = hostEntity.getMethodMap();
        if (null != methodMap) {
            String methodFullName = registerBeanMethod.toString();
            RegisterBean.RegisterBeanMethod beanMethod = methodMap.get(methodFullName);
            if (null == beanMethod
                    || beanMethod.getLimit() <= 0
                    || beanMethod.getDurationInSeconds() <= 0) {
                return true;
            }
            registerBeanMethod.setLimit(beanMethod.getLimit());
            registerBeanMethod.setDurationInSeconds(beanMethod.getDurationInSeconds());
            EventCountCircuitBreaker circuitBreaker =
                    findEventCountCircuitBreaker(methodFullName, beanMethod);
            if (null == circuitBreaker) {
                throw new IllegalStateException(
                        methodFullName
                                + " cannot find the corresponding frequency control manager.");
            }
            return circuitBreaker.checkState();
        }
        return true;
    }

    private EventCountCircuitBreaker findEventCountCircuitBreaker(
            String methodFullName, RegisterBean.RegisterBeanMethod registerBeanMethod) {
        if (!cache.containsKey(methodFullName)) {
            synchronized (methodFullName.intern()) {
                if (!cache.containsKey(methodFullName)) {
                    EventCountCircuitBreaker countCircuitBreaker =
                            new EventCountCircuitBreaker(
                                    registerBeanMethod.getLimit(),
                                    registerBeanMethod.getDurationInSeconds(),
                                    TimeUnit.SECONDS);
                    cache.put(methodFullName, countCircuitBreaker);
                }
            }
        }
        EventCountCircuitBreaker countCircuitBreaker = cache.get(methodFullName);
        if (countCircuitBreaker.checkState()) {
            countCircuitBreaker.incrementAndCheckState();
        }
        return countCircuitBreaker;
    }
}
