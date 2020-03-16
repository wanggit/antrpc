package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.client.zk.zknode.IReportSubscriber;
import io.github.wanggit.antrpc.commons.config.IConfiguration;

import java.util.Set;

public interface IRpcAutowiredProcessor {
    void init(
            BeanContainer beanContainer,
            IReportSubscriber reportSubscriber,
            IConfiguration configuration);

    boolean checkBeanHasRpcAutowire(Object bean);

    Set<String> snapshot();
}
