package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ZkRegisterHolder implements IZkRegisterHolder, Runnable {

    private final List<RegisterBean> registerBeans = new ArrayList<>();
    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    private final IRegister register;
    private final IZkClient zkClient;
    private final IZkNodeBuilder zkNodeBuilder;
    private final IConfiguration configuration;

    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();

    public ZkRegisterHolder(
            IRegister register,
            IZkNodeBuilder zkNodeBuilder,
            IZkClient zkClient,
            IConfiguration configuration) {
        this.register = register;
        this.zkClient = zkClient;
        this.zkNodeBuilder = zkNodeBuilder;
        this.configuration = configuration;
        init();
    }

    private void init() {
        if (atomicBoolean.compareAndSet(false, true)) {
            executorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
        }
    }

    @Override
    public void add(RegisterBean registerBean) {
        registerBeans.add(registerBean);
    }

    @Override
    public void allReRegister() {
        if (log.isInfoEnabled()) {
            log.info("All services will be re-registered.");
        }
        for (RegisterBean registerBean : registerBeans) {
            register.register(registerBean, zkNodeBuilder, configuration.getExposeIp());
            if (log.isInfoEnabled()) {
                log.info(
                        "The "
                                + registerBean.getZookeeperFullPath(configuration.getExposeIp())
                                + " service has been re-registered.");
            }
        }
    }

    @Override
    public void run() {
        if (log.isInfoEnabled()) {
            log.info("Periodically checking to see if any services need to be re-registered.");
        }
        CuratorFramework curator = zkClient.getCurator();
        registerBeans.forEach(
                it -> {
                    String fullPath = it.getZookeeperFullPath(configuration.getExposeIp());
                    if (log.isInfoEnabled()) {
                        log.info(
                                "Checking the registration status of "
                                        + fullPath
                                        + " node periodically.");
                    }
                    try {
                        curator.getData().forPath(fullPath);
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error(
                                    "Failed to get the "
                                            + fullPath
                                            + " node data and attempted to re-register the node");
                        }
                        internalRunRegister(it);
                    }
                });
    }

    private void internalRunRegister(RegisterBean it) {
        try {
            register.register(it, zkNodeBuilder, configuration.getExposeIp());
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(
                        "An exception occurred while re-registering the service. The connection to Zookeeper may have been broken.",
                        e);
            }
        }
    }
}
