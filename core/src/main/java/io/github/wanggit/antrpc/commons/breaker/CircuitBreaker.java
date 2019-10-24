package io.github.wanggit.antrpc.commons.breaker;

import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class CircuitBreaker implements ICircuitBreaker {

    private final ConcurrentHashMap<String, EventCountCircuitBreaker> breakerMap =
            new ConcurrentHashMap<>();

    private CircuitBreakerConfig globalConfig = null;
    private Map<String, CircuitBreakerConfig> interfaceConfigs = null;

    @Override
    public CircuitBreakerConfig getInterfaceCircuitBreaker(String className) {
        if (null != interfaceConfigs) {
            return interfaceConfigs.get(className);
        }
        return null;
    }

    @Override
    public void init(IConfiguration configuration) {
        if (null != configuration.getGlobalBreakerConfig()) {
            configuration.getGlobalBreakerConfig().checkSelf();
            this.globalConfig = configuration.getGlobalBreakerConfig();
        }
        if (null != configuration.getInterfaceBreakerConfigs()) {
            configuration
                    .getInterfaceBreakerConfigs()
                    .forEach(
                            (key, value) -> {
                                value.checkSelf();
                            });
            this.interfaceConfigs = configuration.getInterfaceBreakerConfigs();
        }
    }

    @Override
    public boolean checkState(String className, String callLogKey) {
        if (null == interfaceConfigs && null == globalConfig) {
            return true;
        }
        CircuitBreakerConfig breakerConfig = interfaceConfigs.get(className);
        if (null == breakerConfig) {
            if (null == globalConfig) {
                return true;
            } else {
                breakerConfig = globalConfig;
            }
        }
        if (null == breakerMap.get(callLogKey)) {
            synchronized (callLogKey.intern()) {
                if (null == breakerMap.get(callLogKey)) {
                    int threshold = breakerConfig.getThreshold();
                    long checkInterval = breakerConfig.getCheckIntervalSeconds();
                    if (threshold <= 0 || checkInterval <= 0) {
                        throw new IllegalArgumentException(
                                "The circuit breaker of "
                                        + className
                                        + " is not configured correctly. "
                                        + "threshold and checkInterval must be greater than 0 integer.");
                    }
                    // 如果 checkInterval 秒内出现 threshold 次错误，就开启熔断器
                    breakerMap.putIfAbsent(
                            callLogKey,
                            new EventCountCircuitBreaker(
                                    threshold, checkInterval, TimeUnit.SECONDS));
                }
            }
        }
        EventCountCircuitBreaker breaker = breakerMap.get(callLogKey);
        return breaker.checkState();
    }

    @Override
    public boolean increament(String key) {
        if (null == key) {
            throw new IllegalArgumentException("key cannot be null.");
        }
        if (null == breakerMap.get(key)) {
            return true;
        }
        return breakerMap.get(key).incrementAndCheckState();
    }

    public static class BreakedException extends Exception {
        private static final long serialVersionUID = -4737162890769005737L;

        public BreakedException(String message) {
            super(message);
        }
    }
}
