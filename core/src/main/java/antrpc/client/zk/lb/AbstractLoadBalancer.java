package antrpc.client.zk.lb;

import antrpc.client.Host;

abstract class AbstractLoadBalancer<T extends Host> implements ILoadBalancer<T> {

    /**
     * 统计IP的使用频率
     *
     * @param entity
     */
    void stats(T entity) {
        String hostInfo = entity.getHostInfo();
        HostLogHolder.getInstance().log(hostInfo);
    }
}
