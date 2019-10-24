package io.github.wanggit.antrpc.client.zk.lb;

import io.github.wanggit.antrpc.client.Host;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RoundLoadBalancer<T extends Host> extends AbstractLoadBalancer<T> {

    private AtomicLong counter = new AtomicLong(0);

    @Override
    public T chooseFrom(List<T> hostEntities) {
        long cnt = counter.getAndIncrement();
        int size = hostEntities.size();
        int idx = (int) (cnt % size);
        T entity = hostEntities.get(idx);
        super.stats(entity);
        return entity;
    }
}
