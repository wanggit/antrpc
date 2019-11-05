package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

public class HeartBeatCounterTest {

    @Test
    public void testHeartBeatCounter() throws InterruptedException {
        HeartBeatCounter heartBeatCounter = new HeartBeatCounter();
        for (int i = 0; i < 1000; i++) {
            int rnd = RandomUtils.nextInt();
            heartBeatCounter.send(rnd);
            heartBeatCounter.receive(rnd);
        }
        Assert.assertFalse(heartBeatCounter.heartBeatWasContinuousLoss());

        HeartBeatCounter finalHeartBeatCounter = new HeartBeatCounter();
        for (int i = 0; i < 1000; i++) {
            new Thread() {
                @Override
                public void run() {
                    int rnd = RandomUtils.nextInt();
                    finalHeartBeatCounter.send(rnd);
                    try {
                        Thread.sleep(RandomUtils.nextInt(1, 20));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finalHeartBeatCounter.receive(rnd);
                }
            }.start();
        }
        WaitUtil.wait(10, 1);
        Assert.assertFalse(finalHeartBeatCounter.heartBeatWasContinuousLoss());

        heartBeatCounter = new HeartBeatCounter();
        for (int i = 0; i < 20; i++) {
            int rnd = RandomUtils.nextInt();
            heartBeatCounter.send(rnd);
            if (i % 2 == 0) {
                heartBeatCounter.receive(rnd);
            }
        }
        Assert.assertFalse(heartBeatCounter.heartBeatWasContinuousLoss());

        heartBeatCounter = new HeartBeatCounter();
        for (int i = 0; i < 20; i++) {
            int rnd = RandomUtils.nextInt();
            heartBeatCounter.send(rnd);
            if (i % 4 == 0) {
                heartBeatCounter.receive(rnd);
            }
        }
        Assert.assertFalse(heartBeatCounter.heartBeatWasContinuousLoss());

        heartBeatCounter = new HeartBeatCounter();
        for (int i = 0; i < 100; i++) {
            int rnd = RandomUtils.nextInt();
            heartBeatCounter.send(rnd);
            if (i % 6 == 0) {
                heartBeatCounter.receive(rnd);
            }
        }
        Assert.assertTrue(heartBeatCounter.heartBeatWasContinuousLoss());
    }
}
