package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

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
        RegisterBean.RegisterBeanMethod testDirectHostMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(NodeHostContainerTest.class, "testDirectHost"));
        NodeHostEntity choosed =
                nodeHostContainer.choose(
                        NodeHostContainerTest.class.getName(), testDirectHostMethod.toString());
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
                    NodeHostEntity entity =
                            new NodeHostEntity("127.0.1." + getIdx(), 1000 * getIdx());
                    entity.setClassName(NodeHostContainerTest.class.getName());
                    Map<String, RegisterBean.RegisterBeanMethod> methodMap = new HashMap<>();
                    RegisterBean.RegisterBeanMethod testDirectHostMethod =
                            RegisterBeanHelper.getRegisterBeanMethod(
                                    ReflectionUtils.findMethod(
                                            NodeHostContainerTest.class, "testDirectHost"));
                    RegisterBean.RegisterBeanMethod testChooseMethod =
                            RegisterBeanHelper.getRegisterBeanMethod(
                                    ReflectionUtils.findMethod(
                                            NodeHostContainerTest.class, "testChoose"));
                    methodMap.put(testDirectHostMethod.toString(), testDirectHostMethod);
                    methodMap.put(testChooseMethod.toString(), testChooseMethod);
                    List<String> methodStrs = new ArrayList<>();
                    methodStrs.add(testDirectHostMethod.toString());
                    methodStrs.add(testChooseMethod.toString());
                    entity.setMethodStrs(methodStrs);
                    entity.setMethodMap(methodMap);
                    entity.setRegisterTs(System.currentTimeMillis());
                    entity.setRefreshTs(System.currentTimeMillis());
                    nodeHostContainer.add(NodeHostContainerTest.class.getName(), entity);
                }
            }.start();
        }
        WaitUtil.wait(1, 1, false);
        /*List<NodeHostEntity> entities =
                nodeHostContainer.getHostEntities(NodeHostContainerTest.class.getName());
        Assert.assertEquals(14, entities.size());*/

        RegisterBean.RegisterBeanMethod testChooseMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(NodeHostContainerTest.class, "testChoose"));
        Vector<String> hostInfos = new Vector<>();
        int times = 100;
        int max = 14 * times;
        for (int i = 0; i < max; i++) {
            new MyThread(i) {
                @Override
                public void run() {
                    NodeHostEntity choosed =
                            nodeHostContainer.choose(
                                    NodeHostContainerTest.class.getName(),
                                    testChooseMethod.toString());
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
        Assert.assertEquals(times * 2, counts.iterator().next().intValue());
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
