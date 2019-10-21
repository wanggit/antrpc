package antrpc.commons.config;

import lombok.Data;

@Data
public class RpcClientsConfig {

    private int connectionTimeoutSeconds = 3;
    private int maxTotal = 20;
    private int maxIdle = 8;
    private int minIdle = 4;
    private int minEvictableIdleTimeMillis = 5000;
}
