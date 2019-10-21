package antrpc.client.zk.register;

import antrpc.IAntrpcContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZkRegisterHolder implements IZkRegisterHolder, Runnable {

    private List<RegisterBean> registerBeans = new ArrayList<>();

    private IAntrpcContext antrpcContext;

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    public ZkRegisterHolder(IAntrpcContext antrpcContext) {
        this.antrpcContext = antrpcContext;
        scheduledExecutorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void add(RegisterBean registerBean) {
        registerBeans.add(registerBean);
    }

    @Override
    public void allReRegister() {
        for (RegisterBean registerBean : registerBeans) {
            antrpcContext.getRegister().register(registerBean);
        }
    }

    @Override
    public void run() {
        CuratorFramework curator = antrpcContext.getZkClient().getCurator();
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
                        antrpcContext.getRegister().register(it);
                    }
                });
    }
}
