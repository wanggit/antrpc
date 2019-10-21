package antrpc.client.zk.lb;

import antrpc.client.zk.zknode.NodeHostEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RoundLoadBalancerTest {

    @Test
    public void testChooseFrom() throws Exception {
        RoundLoadBalancer<NodeHostEntity> roundLoadBalancer = new RoundLoadBalancer<>();
        String ipPrefix = "192.168.1.";
        List<NodeHostEntity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            NodeHostEntity entity = new NodeHostEntity();
            entity.setIp(ipPrefix + i);
            entity.setPort(7000);
            entities.add(entity);
        }
        for (int i = 0; i < 200; i++) {
            NodeHostEntity entity = roundLoadBalancer.chooseFrom(entities);
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
        Long max = Collections.max(tmps.values());
        Long min = Collections.min(tmps.values());
        System.out.println("max=" + max + ", min=" + min);
        Assert.assertEquals(max.longValue(), min.longValue());
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
