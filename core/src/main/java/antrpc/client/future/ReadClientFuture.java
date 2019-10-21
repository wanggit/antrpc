package antrpc.client.future;

import antrpc.commons.bean.RpcProtocol;
import antrpc.commons.bean.RpcResponseBean;
import antrpc.commons.codec.kryo.KryoSerializer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReadClientFuture {

    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private final int timeout = 10;
    private RpcProtocol response;

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

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        if (null != result) {
            return (RpcResponseBean) KryoSerializer.getInstance().deserialize(result.getData());
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
