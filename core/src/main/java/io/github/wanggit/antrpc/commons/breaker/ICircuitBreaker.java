package io.github.wanggit.antrpc.commons.breaker;

import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;

public interface ICircuitBreaker {

    CircuitBreakerConfig getInterfaceCircuitBreaker(String className);

    void init(IConfiguration configuration);

    boolean checkState(String className, String callLogKey);

    boolean increament(String key);
}
