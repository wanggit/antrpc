package io.github.wanggit.antrpc.client.zk.lb;

import java.util.List;

public interface ILoadBalancer<T> {

    T chooseFrom(List<T> hostEntities);
}
