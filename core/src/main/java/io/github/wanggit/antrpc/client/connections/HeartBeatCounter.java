package io.github.wanggit.antrpc.client.connections;

import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeatCounter implements IHeartBeatCounter {

    private int[] container = new int[20];
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public void send(int cmdId) {
        int idx = index.getAndIncrement();
        container[idx] = cmdId;
        if (index.get() >= container.length) {
            synchronized (this) {
                int[] tmp = new int[container.length];
                System.arraycopy(container, 10, tmp, 0, 10);
                container = tmp;
                index.set(10);
            }
        }
    }

    @Override
    public void receive(int cmdId) {
        for (int i = 0; i < container.length; i++) {
            if (container[i] == cmdId) {
                container[i] = -1;
            }
        }
    }

    @Override
    public boolean heartBeatWasContinuousLoss() {
        int count = 0;
        for (int i = 0; i < container.length; i++) {
            if (container[i] > 0) {
                count++;
            } else {
                count = 0;
            }
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }
}
