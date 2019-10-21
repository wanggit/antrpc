package antrpc.client.zk.lb;

import antrpc.client.Host;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

@Slf4j
public class RandomLoadBalancer<T extends Host> extends AbstractLoadBalancer<T> {
    @Override
    public T chooseFrom(List<T> hostEntities) {
        if (null == hostEntities || hostEntities.isEmpty()) {
            throw new IllegalArgumentException("hostEntities cannot be null or empty.");
        }
        int size = hostEntities.size();
        int idx = RandomUtils.nextInt(0, size);
        T entity = hostEntities.get(idx);
        super.stats(entity);
        return entity;
    }
}
