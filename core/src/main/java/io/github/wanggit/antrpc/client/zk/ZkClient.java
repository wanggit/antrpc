package io.github.wanggit.antrpc.client.zk;

import io.github.wanggit.antrpc.commons.config.IConfiguration;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZkClient implements IZkClient {

    private final CuratorFramework zkClient;

    public ZkClient(IConfiguration configuration) {
        RetryPolicy retryPolicy =
                new ExponentialBackoffRetry(
                        configuration.getZkConnectRetryBaseSleepMs(),
                        configuration.getZkConnectMaxRetries(),
                        configuration.getZkConnectRetryMaxSleepMs());
        zkClient =
                CuratorFrameworkFactory.builder()
                        .connectString(configuration.getZkIps())
                        .connectionTimeoutMs(configuration.getZkConnectionTimeoutMs())
                        .retryPolicy(retryPolicy)
                        .build();
        zkClient.start();
    }

    @Override
    public CuratorFramework getCurator() {
        return zkClient;
    }
}
