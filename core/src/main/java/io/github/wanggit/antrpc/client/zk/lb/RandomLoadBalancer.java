package io.github.wanggit.antrpc.client.zk.lb;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.bean.Host;
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
        if (log.isDebugEnabled()) {
            log.debug("Choosed " + JSONObject.toJSONString(entity));
        }
        super.stats(entity);
        return entity;
    }
}
