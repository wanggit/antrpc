package io.github.wanggit.antrpc.commons.future;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.bean.RpcResponseBean;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;
import io.github.wanggit.antrpc.commons.codec.serialize.SerializerFactory;
import io.github.wanggit.antrpc.commons.codec.serialize.json.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
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
                Object object = serializer.deserialize(result.getData());
                try {
                    return (RpcResponseBean) object;
                } catch (ClassCastException e) {
                    if (log.isErrorEnabled()) {
                        log.error(
                                "Class Cast Error. Expected: "
                                        + RpcResponseBean.class
                                        + ", Actual: "
                                        + object.getClass()
                                        + ", Actual Value: "
                                        + object);
                    }
                    throw e;
                }
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
            ISerializer newSerializerByByteCmd =
                    SerializerFactory.getInstance()
                            .createNewSerializerByByteCmd(data.getSerializer());
            if (null != newSerializerByByteCmd) {
                if (newSerializerByByteCmd instanceof JsonSerializer) {
                    Map<String, String> configs = new HashMap<>();
                    configs.put(JsonSerializer.TARGET_KEY, RpcResponseBean.class.getName());
                    newSerializerByByteCmd.setConfigs(configs);
                }
                this.serializer = newSerializerByByteCmd;
            }
            response = data;
            done.signal();
        } finally {
            lock.unlock();
        }
    }
}
