package io.github.wanggit.antrpc.client.spring;

public interface IRpcAutowiredProcessor {
    void init(BeanContainer beanContainer);

    boolean checkBeanHasRpcAutowire(Object bean);
}
