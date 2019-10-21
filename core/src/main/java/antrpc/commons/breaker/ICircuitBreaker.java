package antrpc.commons.breaker;

import antrpc.commons.config.CircuitBreakerConfig;
import antrpc.commons.config.IConfiguration;

public interface ICircuitBreaker {

    CircuitBreakerConfig getInterfaceCircuitBreaker(String className);

    void init(IConfiguration configuration);

    boolean checkState(String className, String callLogKey);

    boolean increament(String key);
}
