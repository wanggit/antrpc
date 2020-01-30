package io.github.wanggit.antrpc.commons.breaker;

import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;

public interface ICircuitBreaker {

    CircuitBreakerConfig getInterfaceCircuitBreaker(String className);

    boolean checkState(String className, String callLogKey);

    boolean increament(String key);

    boolean checkNearBy(String key);

    void close(String key);

    void open(String key);
}
