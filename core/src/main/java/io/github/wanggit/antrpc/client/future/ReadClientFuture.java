package io.github.wanggit.antrpc.client.future;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReadClientFuture {

    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private final int timeout = 10;
    private RpcProtocol response;
    private ISerializer serializer;

    ReadClientFuture(ISerializer serializer) {
        this.serializer = serializer;
    }

    private void clear() {
        this.response = null;
        this.serializer = null;
    }

    public RpcResponseBean get() {
        RpcProtocol result = null;
        try {
            lock.lock();
            if (response != null) {
                result = response;
            } else {
                done.await(timeout, TimeUnit.SECONDS);
                result = response;
            }
            if (null != result) {
                return (RpcResponseBean) serializer.deserialize(result.getData());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
            clear();
        }
        return null;
    }

    void receive(RpcProtocol data) {
        try {
            lock.lock();
            response = data;
            done.signal();
        } finally {
            lock.unlock();
        }
    }
}
