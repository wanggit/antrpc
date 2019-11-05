package io.github.wanggit.antrpc.client.connections;

public interface IHeartBeatCounter {

    void send(int cmdId);

    void receive(int cmdId);

    boolean heartBeatWasContinuousLoss();
}
