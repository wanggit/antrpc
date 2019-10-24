package io.github.wanggit.antrpc.commons.config;

import lombok.Data;

@Data
public class CircuitBreakerConfig {

    private int threshold;
    private long checkIntervalSeconds;

    public CircuitBreakerConfig(int threshold, long checkIntervalSeconds) {
        this.threshold = threshold;
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public CircuitBreakerConfig() {}

    public void checkSelf() {
        if (this.getThreshold() <= 0 || this.getCheckIntervalSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "All configuration parameters of the Circuit Breaker must be greater than 0.");
        }
    }
}
