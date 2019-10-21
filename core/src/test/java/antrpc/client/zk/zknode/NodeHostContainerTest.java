package antrpc.client.zk.zknode;

import antrpc.client.Host;
import antrpc.client.zk.lb.LoadBalancerHelper;
import antrpc.commons.config.Configuration;
import antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeHostContainerTest {

    @Test
    public void testDirectHost() {
        Map<String, DirectNodeHostEntity> directNodeHostEntityMap = new HashMap<>();
        directNodeHostEntityMap.put(
                NodeHostContainerTest.class.getName(),
                DirectNodeHostEntity.from(new Host("localhost", 6060)));
        NodeHostContainer nodeHostContainer = createNodeHostContainer(directNodeHostEntityMap);
        NodeHostEntity choosed = nodeHostContainer.choose(NodeHostContainerTest.class.getName());
        Assert.assertNotNull(choosed);
        Assert.assertEquals("localhost", choosed.getIp());
        Assert.assertEquals(6060, choosed.getPort().intValue());
    }

    @Test
    public void testChoose() throws InterruptedException {
        Map<String, DirectNodeHostEntity> directNodeHostEntityMap = new HashMap<>();
        NodeHostContainer nodeHostContainer = createNodeHostContainer(directNodeHostEntityMap);
        for (int i = 1; i < 8; i++) {
            new MyThread(i) {
                @Override
                public void run() {
                    nodeHostContainer.add(
                            NodeHostContainerTest.class.getName(),
                            new NodeHostEntity("127.0.1." + getIdx(), 1000 * getIdx()));
                }
            }.start();
        }
        WaitUtil.wait(1, 1, false);
        List<NodeHostEntity> entities =
                nodeHostContainer.getHostEntities(NodeHostContainerTest.class.getName());
        Assert.assertEquals(7, entities.size());

        final Vector<String> hostInfos = new Vector<>();
        int times = 100;
        int max = entities.size() * times;
        for (int i = 0; i < max; i++) {
            new MyThread(i) {
                @Override
                public void run() {
                    NodeHostEntity choosed =
                            nodeHostContainer.choose(NodeHostContainerTest.class.getName());
                    hostInfos.add(choosed.getHostInfo());
                }
            }.start();
        }
        WaitUtil.wait(1, 1);
        Set<String> sets = new HashSet<>(hostInfos);
        System.out.println(sets);
        Assert.assertEquals(7, sets.size());
        Map<String, AtomicInteger> counter = new HashMap<>();
        for (String hostInfo : hostInfos) {
            if (!counter.containsKey(hostInfo)) {
                counter.put(hostInfo, new AtomicInteger(0));
            }
            counter.get(hostInfo).incrementAndGet();
        }
        System.out.println(counter);
        Set<Integer> counts = new HashSet<>();
        counter.values()
                .forEach(
                        it -> {
                            counts.add(it.intValue());
                        });
        Assert.assertEquals(1, counts.size());
        Assert.assertEquals(times, counts.iterator().next().intValue());
    }

    private NodeHostContainer createNodeHostContainer(
            Map<String, DirectNodeHostEntity> directNodeHostEntityMap) {
        Configuration configuration = new Configuration();
        LoadBalancerHelper loadBalancerHelper = new LoadBalancerHelper(configuration);
        return new NodeHostContainer(loadBalancerHelper, directNodeHostEntityMap);
    }

    static class MyThread extends Thread {

        final int idx;

        MyThread(int idx) {
            this.idx = idx;
        }

        int getIdx() {
            return idx;
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
