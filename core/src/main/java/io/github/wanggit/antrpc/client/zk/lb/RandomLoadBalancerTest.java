package io.github.wanggit.antrpc.client.zk.lb;

import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomLoadBalancerTest {

    @Test
    public void testChooseFrom() throws Exception {
        RandomLoadBalancer<NodeHostEntity> randomLoadBalancer = new RandomLoadBalancer<>();
        String ipPrefix = "192.168.0.";
        List<NodeHostEntity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            NodeHostEntity entity = new NodeHostEntity();
            entity.setIp(ipPrefix + i);
            entity.setPort(7000);
            entities.add(entity);
        }
        for (int i = 0; i < 200; i++) {
            NodeHostEntity entity = randomLoadBalancer.chooseFrom(entities);
            Assert.assertNotNull(entity);
        }
        Map<String, Long> snapshot = HostLogHolder.getInstance().snapshot();
        Map<String, Long> tmps = new HashMap<>();
        snapshot.forEach(
                (key, value) -> {
                    if (key.startsWith(ipPrefix)) {
                        tmps.put(key, value);
                    }
                });
        System.out.println(tmps);
        tmps.forEach(
                (key, value) -> {
                    Assert.assertTrue(value != 0);
                });
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
