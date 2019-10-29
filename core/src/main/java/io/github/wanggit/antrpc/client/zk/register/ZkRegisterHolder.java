package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZkRegisterHolder implements IZkRegisterHolder, Runnable {

    private final List<RegisterBean> registerBeans = new ArrayList<>();

    private final Register register;
    private final IZkClient zkClient;
    private final IZkNodeBuilder zkNodeBuilder;

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    public ZkRegisterHolder(Register register, IZkNodeBuilder zkNodeBuilder, IZkClient zkClient) {
        this.register = register;
        this.zkClient = zkClient;
        this.zkNodeBuilder = zkNodeBuilder;
        scheduledExecutorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void add(RegisterBean registerBean) {
        registerBeans.add(registerBean);
    }

    @Override
    public void allReRegister() {
        for (RegisterBean registerBean : registerBeans) {
            register.register(registerBean, zkNodeBuilder);
        }
    }

    @Override
    public void run() {
        CuratorFramework curator = zkClient.getCurator();
        registerBeans.forEach(
                it -> {
                    String fullPath = it.getZookeeperFullPath();
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
                        register.register(it, zkNodeBuilder);
                    }
                });
    }
}
