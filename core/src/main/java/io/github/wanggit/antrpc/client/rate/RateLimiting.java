package io.github.wanggit.antrpc.client.rate;

import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiting implements IRateLimiting {

    private final ConcurrentHashMap<String, EventCountCircuitBreaker> cache =
            new ConcurrentHashMap<>();

    @Override
    public boolean allowAccess(RegisterBean.RegisterBeanMethod registerBeanMethod) {
        if (null == registerBeanMethod) {
            throw new IllegalArgumentException("Argument cannot be null.");
        }
        if (registerBeanMethod.getLimit() <= 0 || registerBeanMethod.getDurationInSeconds() <= 0) {
            return true;
        }
        String methodFullName = registerBeanMethod.toString();
        EventCountCircuitBreaker circuitBreaker =
                findEventCountCircuitBreaker(methodFullName, registerBeanMethod);
        if (null == circuitBreaker) {
            throw new IllegalStateException(
                    methodFullName + " cannot find the corresponding frequency control manager.");
        }
        return circuitBreaker.checkState();
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
