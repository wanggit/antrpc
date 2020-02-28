package io.github.wanggit.antrpc.client.spring;

import java.util.Set;

public interface IRpcAutowiredProcessor {
    void init(BeanContainer beanContainer);

    boolean checkBeanHasRpcAutowire(Object bean);

    Set<String> snapshot();
}
